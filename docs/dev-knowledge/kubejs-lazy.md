# KubeJS Lazy 并发缓存修复

## 实现与适用范围

关联 [CDR #2309](https://github.com/Jasons-impart/Create-Delight-Remake/pull/2309) 与
[CDR #1736](https://github.com/Jasons-impart/Create-Delight-Remake/issues/1736)。
KubeJS `Lazy.get()` 读取缓存与 `forget()` 清空缓存可能交错，使有效 supplier 的调用方得到 null。
本补丁通过 `kubejs.LazyMixin` 触发 `CombatMixinPlugin.postApply`，精确匹配
`get()Ljava/lang/Object;`、`forget()V`，为两者添加 `ACC_SYNCHRONIZED`。
同一 Lazy 实例使用同一 JVM 监视器，保留方法体、缓存过期及异常传播行为。

Mixin 使用字符串目标、`@Pseudo`、`remap = false`，并按 `kubejs` 模组存在性门控，
不需要引入 KubeJS 编译依赖。方法缺失或变成 static/abstract/native 时插件抛出异常，
不部分修改目标；此检查不能识别所有上游语义变化，升级 KubeJS 时仍须复核。

原 HotAI 补丁针对 `2001.6.5-build.16`；本地实际运行包为 `2001.6.5-build.24`，
仍具有相同的未同步 get/forget 结构。本次自动回归使用 build.24，不能视为 build.16 或完整游戏回归。

## 可重复回归

在 CDC 仓库中使用 Java 17 执行：

```text
./gradlew build kubeJsLazyRegression -PkubejsLazyJar=/absolute/path/to/kubejs-forge-2001.6.5-build.24.jar --no-daemon
```

Windows 可使用 `gradlew.bat`；PowerShell 下也可直接运行 wrapper，避免 batch 层：

```powershell
& "$env:JAVA_HOME/bin/java.exe" -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain build kubeJsLazyRegression '-PkubejsLazyJar=../mods/kubejs-forge-2001.6.5-build.24.jar' --no-daemon
```

测试直接调用生产插件的 `postApply`，检查规范化 class 只改变两个访问标志、重复应用幂等、
目标方法缺失时不部分修改；随后用隔离 ClassLoader 加载原 class 和转换后 class，验证缓存命中、
清缓存、过期、supplier 异常后的跨线程重试，以及 8 线程各 25 万轮 get/forget。
原版空值数随调度变化，不要求固定复现次数；补丁版必须为零。
此测试没有启动 Forge/Mixin 转换管线，不覆盖模组门控和目标类加载时序。

## 后续游戏测试与迁移

使用存档副本部署本分支构建的非 `-all` reobf CDC JAR，并暂时移走对应
`hotai/dev/latvian/mods/kubejs/util/Lazy.badiff`，完整重启，避免旧补丁掩盖 CDC 未生效。
本 CDC PR 不修改父整合包 HotAI 文件、Packwiz、CDC 子模块指针或运行 JAR。

- 确认日志包含 `[CDCore][KubeJSLazy] Synchronized get() and forget()`。
- 在 Mixin 导出的 Lazy class 中确认两个方法的 `ACC_SYNCHRONIZED` 标志。
- 验证冷启动首次进档、退出重进、游戏内重载、保存后再次冷启动，以及饰品和背包内容。
- 检查 `GeneratedData` 空值、TACZ `GUN_DATA=null`、`Couldn't place player` 是否出现。
- 验证 TACZ 登录同步、JEI 注液配方/流体标签，以及独立服务器登录和重载。
- 若面向旧维护分支发布，使用 build.16 另跑独立回归与游戏测试。

只有 CDC 独立生效的游戏回归通过后，才在父整合包单独提交旧 HotAI 补丁移除与 CDC 升级。
回滚时恢复旧 CDC 与对应版本的原 HotAI 补丁并完整重启；不要删除玩家数据。
该修复不恢复已经丢失的物品，也不覆盖所有玩家数据异常。
