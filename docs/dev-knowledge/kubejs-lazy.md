# KubeJS Lazy 并发缓存修复

## 实现与并发语义

关联 [CDR #2309](https://github.com/Jasons-impart/Create-Delight-Remake/pull/2309)、[CDR #1736](https://github.com/Jasons-impart/Create-Delight-Remake/issues/1736) 与 [CDC #131](https://github.com/Jasons-impart/Create-Delight-Core/pull/131)。
KubeJS `Lazy.get()` 与 `forget()` 并发读写普通 cached/value 字段，可能把清空后的 null 返回给调用方。

`kubejs.LazyMixin` 触发 `CombatMixinPlugin.postApply`，校验构造器、字段和方法描述符后，为每个 Lazy 实例添加独立的 `KubeJsLazyCache`；构造器初始化该字段，get/forget 改为调用 helper。
保留上游 factory 和绝对过期时间；旧私有 cached/value 字段保留但不再作为缓存状态。字符串目标、`@Pseudo`、`remap = false` 和模组存在性门控避免 KubeJS 编译依赖。
此方案替换两个方法体，不能再称为只修改访问标志；上游升级或其他模组改写这些方法时必须复核。

- `AtomicReference<Snapshot>` 将 cached 与 value 一起发布，合法 null 仍可缓存。
- 未命中时不持缓存锁、不等待内部 Future，直接调用 factory；成功后只尝试一次 CAS，当前调用返回自己的局部计算结果。
- 并发未命中仍可能重复执行 factory（原版也可能如此），不提供“恰好计算一次”保证；同一快照下先 CAS 成功者填充缓存。
- `forget()` 总是发布新的空快照，不复用旧身份。失效前开始的计算可返回给其原调用方，但不能回填缓存或覆盖失效后生成的新值。
- 过期仍使用构造时固定的绝对时间；factory 异常原样传播，不改变当前快照。
- helper 不持监视器、不等待其他计算，避免新增 Lazy 锁与回调外部锁的反向依赖；回调自身的死锁、递归或阻塞仍可能存在。

## 死锁审计（2026-09-18）

上游 2001 分支基准：`ba142541dcc1d230383f4a55e38dd92ff10d1029`。

| 路径 | factory 与线程关系 | 结论 |
| --- | --- | --- |
| `GeneratedData.get()` / `GeneratedResourcePack.getGenerated()` | KubeJS 自身创建文件 Lazy，factory 调用 `Files.readAllBytes`；读取后可立即 forget。 | 即使没有外部模组使用 Lazy，也存在原空缓存竞态；未发现这些方法自身持有外部锁。 |
| `ResourceGenerator.add()` / `AssetJsonGenerator.stencil()` | 接受任意 `Supplier<byte[]>` 或调用插件图像生成逻辑。 | 不能假定 factory 永远是纯文件读取或无锁操作。 |
| `UtilsWrapper.lazy/expiringLazy` 与 `runAsync/supplyAsync` | 任意脚本 supplier 可与后台线程一起使用。 | 无法对所有插件/脚本回调建立统一锁顺序。 |
| `BlockEntityAttachmentType.ALL` | factory 执行插件 `registerBlockEntityAttachments` 回调。 | 本地已知实现只注册内置 inventory，未发现真实反向取锁。 |
| 本地 388 个运行 JAR | 排除 KubeJS 自身后，仅 ProbeJS 6.0.1 的 `BlockEntityInfoDocument` 直接引用 Lazy；dump 在 daemon 编译线程运行。 | 该结论仅限外部直接引用，不包含 KubeJS 自身大量使用，也不排除反射/动态生成代码；外部调用自身未见 wait/join 或额外锁。 |
| 本整合包 `kubejs/` 脚本 | 未找到直接使用 `Utils.lazy/expiringLazy`、`runAsync/supplyAsync` 或该 Lazy 类。 | 没有当前脚本的直接锁环证据，不能证明整个模组环境无死锁。 |

在真实 Lazy class 上构造 `externalLock -> Lazy.get()` 与 `Lazy.get() -> factory -> externalLock` 夹具：原版与原子缓存版均完成；整方法 synchronized 版被 `ThreadMXBean.findMonitorDeadlockedThreads()` 确认为两个线程的监视器环。
这是确定性的补丁风险复现，不是实际游戏死锁复现；#1736 的具体资源和竞争线程也尚未定位。

## 可重复回归

在 CDC 仓库使用 Java 17（Windows 可直接运行 wrapper main）：

```powershell
& "$env:JAVA_HOME/bin/java.exe" -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain build kubeJsLazyRegression kubeJsLazyLockOrderProbe '-PkubejsLazyJar=../mods/kubejs-forge-2001.6.5-build.24.jar' --no-daemon
```

测试直接调用生产插件转换真实 JAR 的 Lazy.class，在隔离 ClassLoader 中执行；helper 来自 CDC 主源码。
覆盖改写范围、幂等、结构变化时不部分修改、缓存命中、合法 null、过期、异常后跨线程重试、计算中连续 forget、有/无新值回填、两个并发 miss，以及 factory 等待另一线程 forget。
并发压力为 8 线程各 25 万轮 get/forget，补丁版空值必须为零。锁顺序探针用 daemon 线程保留旧同步版的对照死锁，打印诊断后随独立测试 JVM 退出，不挂起 Gradle。

已通过 Java 17 完整构建、build.16/build.24/build.26 独立回归及锁顺序探针。build.16/build.26 原版分别出现 38,888/98,332 次空值，新版均为 0；原版计数随调度变化。
Modrinth 和官方 Maven 最新 1.20.1 Forge 为 build.26，Lazy.class 与 build.24 的 SHA-256 相同；原 HotAI 补丁针对 build.16。
build.16 使用 Modrinth 原件，SHA-256 `3de6b7267d3aab981848ed54d3afe7edf20532fa4060631fd6fffcd99ac5f3d5`，与 #2309 输入一致；官方 Maven 同版本 JAR 字节不同，不能仅凭版本号判断二进制测试样本一致。
上述回归未启动完整 Forge/Mixin 管线，不代表游戏加载时序已经验证。

## 后续游戏测试与迁移

在存档副本部署非 `-all` reobf CDC JAR，暂时移走 `hotai/dev/latvian/mods/kubejs/util/Lazy.badiff` 后完整重启。
不要叠加旧二进制差分：目标 class 已改变，旧整方法锁也可能掩盖测试结果。本 CDC PR 不修改父整合包 HotAI、Packwiz、子模块指针或运行 JAR。

- 确认日志包含 `[CDCore][KubeJSLazy] Applied atomic cache`。
- 检查导出 class：独立 cache 字段、构造器初始化及 get/forget helper 调用；两个方法没有 `ACC_SYNCHRONIZED`。
- 冷启动首次进档、退出重进、连续重载、保存后再次冷启动；检查饰品/背包、TACZ 同步、JEI 注液配方/流体标签。
- 观察重载耗时与停顿；长时间无进展时采集至少两份间隔数秒的 `jcmd <pid> Thread.print -l`，检查锁持有者及等待环，不将短暂排队当作死锁。
- 检查 `GeneratedData` 空值、TACZ `GUN_DATA=null`、`Couldn't place player`；再验证独立服务器登录与重载。
- 本轮客户端未运行，当前会话没有 Minecraft MCP 工具；完整冷启动/重载验证仍待环境接入，不将独立回归写成游戏回归。

游戏回归通过后，再单独提交父整合包 CDC 升级和旧 HotAI 移除。回滚恢复旧 CDC 与对应版本的原补丁后重启，不要删除玩家数据。
此修复不恢复已丢失物品，也不承诺其他模组不会死锁或零性能损失。
