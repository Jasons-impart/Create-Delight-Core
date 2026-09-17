# 非有限伤害运行时诊断

此功能用于定位伤害链中的 Infinity / NaN，不修正伤害、不取消事件，也不改变 MMT 的适用实体范围。原有异常仍会传播给 Neruina。

## 本地测试

1. 在实例 `config/createdelightcore-common.toml` 设置 `logNonFiniteDamage = true`，重启当前实例。默认值为 false；也可用 JVM 参数 `-Dcreatedelightcore.damageDiagnostics=true` 开启。
2. 在 `logs/latest.log` 搜索 `[DamageDiagnostics] Instrumented`：当前 MMT 2.4.15 应为 10 次运算，TetraWear 1.0.0 应为 1 次。另外必须看到 `[DamageDiagnostics] Pipeline` 对 `LivingEntity`、`Player`、`CombatRules`、AttributesLib `ALCombatRules` 的覆盖清单，`roots` 和 `probes` 应非零；类首次加载时才输出。清单包含方法描述符和可取得的 Mixin 来源。数量依赖其他模组注入，不能用旧计数假定新实例覆盖完整。
3. 复现下界蟾蜍捕食绯红蚊场景，保留完整日志。已被 Neruina 暂停的实体不会自行再次执行攻击，需要恢复该实体或在测试场景重新触发。
4. 搜索 `[CDCore][DamageTrace #`，按编号读取上下文、操作及异常栈。测试结束后关闭配置；若使用 JVM 参数，还需移除该参数并重启。

本次测试构建只覆盖本地 `mods/Create-Delight-Core-1.20.1-dev.jar`，没有修改 Packwiz 发布载荷或哈希。后续资源同步可能恢复基线 JAR。替换前的 JAR 与配置保存在父实例 `tmp-opencode/damage-diagnostics-runtime-backup/`；退出游戏后可恢复。不要将临时开启的配置作为发行默认值。

## 日志含义与范围

| 标记 | 含义 |
| --- | --- |
| `FIRST_NONFINITE_OBSERVED` | 当前跟踪首次观察到非有限数；可能是中间值或效果参数，不一定已写入事件 |
| `ACTUAL_FLOAT_OP` | 实际执行的 float 运算，含原操作数、结果、类、方法、源码行及指令编号 |
| `ACTUAL_DOUBLE_OP` / `ACTUAL_D2F` | 管线中的 double 运算和 double 转 float；后者可能将有限 double 变成无穷 float |
| `PIPELINE_VALUE` | 管线方法返回值或调用其他方法后实际收到的返回值；外部 helper 返回异常时用于缩小下一层调查范围 |
| `EVENT_FINITE_TO_NONFINITE` | `LivingHurtEvent` / `LivingDamageEvent.setAmount` 的实际值从有限变为非有限，含调用者 |
| `MMT_EFFECT` | 效果监听器写入的固定伤害、普通倍率或追加的独立倍率，含调用者；追加倍率不是伤害结果 |
| `ORIGINAL_EXCEPTION` / `FINAL_TRACE` | 原始异常或 Forge/actuallyHurt 作用域退出时的记录，含阶段、实体上下文与已观察数值；退出时包含正常返回、提前退出和原异常重抛 |

MMT 的部分保护分支可能丢弃已溢出的中间结果，最终写入有限伤害。应结合事件实际写入与 AttributesLib `getAValue` 入口检查点归因。

`LivingEntity` / `Player.actuallyHurt` 现在建立跨越两个 Forge 事件的父作用域，覆盖护甲、抗性/附魔保护、吸收和生命写入阶段。Forge hurt/damage 子记录通过 `parentTrace` 关联；嵌套调用在 finally 中恢复外层作用域。手动发布事件或覆写 actuallyHurt 且不调用基类的第三方实体，不保证拥有该父作用域。每条记录保留最近 48 项并单独保留首次非有限项，每分钟最多接纳 20 条异常或极大值跟踪。未获接纳的记录会被限流。

除原有 MMT / TetraWear 观察外，低优先级 postApply 探针从合并类中的 `ForgeHooks.onLivingHurt/onLivingDamage` 调用定位根方法，再遍历同类可达方法和 lambda，包括已合并的 Mixin 辅助方法；另覆盖 `CombatRules` 与 `ALCombatRules` 的计算方法。没有伤害作用域时，管线探针直接返回原运算结果，不构造诊断文本。原始算术和类型转换结果保持不变。

任意监听器通过上述两个事件的 `setAmount` 写入异常值，均可定位到调用者。没有被合并进这些类的外部 helper、接口运行时分派、晚于探针的字节码改写、第三方覆写及直接字段修改仍可能需要追加观察；外部数值返回值会记录调用位置。这里不是全 JVM 指令跟踪，不能保证所有模组内部的每次运算都已覆盖。

## 验证状态

已通过 Java 17 完整 Gradle build；`damageDiagnosticsTest` 验证五种 float/double 运算、double 转 float、边界/随机值、有限保护结果、提前返回、异常传播、lambda、Mixin 来源、历史保留和防重复插桩。fixture 实际由 JVM 加载执行，未通过自动重算 maxStack 掩盖栈错误。

可用 `-PdamageDiagnosticTargets=../mods` 检查 MMT/TetraWear/AttributesLib 实际 JAR；在此基础上加 `-PdamageDiagnosticMinecraft=<Forge mapped 或 srg jar>` 检查真实 LivingEntity/Player/CombatRules，再加 `-PdamageDiagnosticMerged=<导出的 LivingEntity.class>` 检查游戏已经合并过的类。真实类执行 ASM BasicVerifier 栈/类型校验，不能代替完整客户端启动。

2026-09-17 已通过 mapped、SRG 与 2026-09-16 游戏导出类验证：基础 LivingEntity 42 个探针、Player 18、CombatRules 15；该次导出的 LivingEntity 为 62，覆盖抗性方法与 AttributesLib 重定向辅助方法。ALCombatRules 为 46。新启动的最终数量可能变化，应检查日志清单。

## 2026-09-16 实测与待确认候选

第一版诊断已完成真实客户端启动，MMT/TetraWear 插桩数量为 10/1。22:29:15 的 #7/#8 与 22:29:25 的 #9/#10 均为主世界同一蟾蜍捕食不同绯红蚊：

```text
LivingHurtEvent: MAX + 0; 0 + 1; MAX * 1; TetraWear MAX - MAX = 0
AttributesLib.getAValue input = Float.MAX_VALUE
ForgeHooks.onLivingHurt returned = Float.MAX_VALUE
ForgeHooks.onLivingDamage input / returned = Infinity
```

两次都没有重现原报告中的 BigDecimal 异常；只能证明两个事件之间溢出。当前补充探针正是为这个遗漏区间增加运行证据。

同次启动导出的 LivingEntity 字节码显示：AttributesLib 的 `apoth_sunderingHasEffect` 恒为 true，空抗性效果的 amplifier 返回 -1，导致原抗性公式仍执行 `damage * 25 / 25`。`Float.MAX_VALUE * 25` 是很强的候选生产者，但两次旧日志没有记录该指令，须在补充探针下再次捕食确认，而不是据此宣布原下界 BigDecimal 崩溃根因已闭环。

## 2026-09-17 补充版实测：首个溢出操作已确认

补充版（源码 `5fde0d2`，运行 JAR SHA-256 `ecc403739126e4e7f96202d9ed2dba5ecc76565b088b509e8587fa5d7d2b5507`）已经真实启动并完成多次主世界捕食测试。新日志覆盖清单为 LivingEntity 59、Player 18、CombatRules 6、ALCombatRules 46 个探针；后两类在首次战斗使用时加载。它们与未合并/旧合并类计数不同，不能机械要求完全相同。

23:07:33—23:07:48 的父 Trace #1、#4、#7、#10、#13、#16、#19 均记录同一首个非有限操作：

```text
phase=LivingEntity.actuallyHurt
getDamageAfterArmorAbsorb / m_21161_ returned = 3.4028235E38 [0x7f7fffff]
getDamageAfterMagicAbsorb / m_6515_ (LivingEntity.java:1587), insn=39:
3.4028235E38 [0x7f7fffff] * 25.0 [0x41c80000] = Infinity [0x7f800000]
同方法 LivingEntity.java:1589, insn=45:
Infinity [0x7f800000] / 25.0 [0x41c80000] = Infinity [0x7f800000]
```

该操作现在有真实运行证据，不再只是静态候选。结合上节的 AttributesLib 重定向字节码，当前捕食的故障机制是：极值伤害进入始终执行的抗性分支，中间乘法先溢出，后续除法无法恢复有限值。MMT、TetraWear 和护甲阶段在这些记录中均不是首次溢出点。

后续实际日志还出现 `Infinity - Infinity = NaN`，吸收值 getter 返回 NaN；生命减法得到 -Infinity 后被原有健康值 clamp 到 0。因此目标死亡不能作为伤害链数值安全的证明。

父作用域与子事件的关联已经运行验证。仍没有本轮 `ORIGINAL_EXCEPTION` / BigDecimal `NumberFormatException` 证据，不能把主世界抗性溢出当作原下界 Hurt 阶段 BigDecimal 崩溃的完整复现。下一步评审并定点修复已确认的抗性算术，同时保留对原报告差异的追踪；尚未改伤害规则或发布 Packwiz 产物。

作者本地完整日志与导出类保存在 `tmp-opencode/damage-diagnostics-repro-20260917-2307/`，不随 Git 分发。本页保留可复核的精简数值证据；报告限流可能省略更多捕食记录，不以日志条数推断总攻击次数。
