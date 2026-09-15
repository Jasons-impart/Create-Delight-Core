# 非有限伤害运行时诊断

此功能用于定位伤害链中的 Infinity / NaN，不修正伤害、不取消事件，也不改变 MMT 的适用实体范围。原有异常仍会传播给 Neruina。

## 本地测试

1. 在实例 `config/createdelightcore-common.toml` 设置 `logNonFiniteDamage = true`，重启当前实例。默认值为 false；也可用 JVM 参数 `-Dcreatedelightcore.damageDiagnostics=true` 开启。
2. 在 `logs/latest.log` 搜索 `[DamageDiagnostics] Instrumented`：当前 MMT 2.4.15 应为 10 次运算，TetraWear 1.0.0 应为 1 次。数量不符需先检查版本或其他 Mixin，不能默认探针完整。
3. 复现下界蟾蜍捕食绯红蚊场景，保留完整日志。已被 Neruina 暂停的实体不会自行再次执行攻击，需要恢复该实体或在测试场景重新触发。
4. 搜索 `[CDCore][DamageTrace #`，按编号读取上下文、操作及异常栈。测试结束后关闭配置；若使用 JVM 参数，还需移除该参数并重启。

本次测试构建只覆盖本地 `mods/Create-Delight-Core-1.20.1-dev.jar`，没有修改 Packwiz 发布载荷或哈希。后续资源同步可能恢复基线 JAR。替换前的 JAR 与配置保存在父实例 `tmp-opencode/damage-diagnostics-runtime-backup/`；退出游戏后可恢复。不要将临时开启的配置作为发行默认值。

## 日志含义与范围

| 标记 | 含义 |
| --- | --- |
| `FIRST_NONFINITE_OBSERVED` | 当前跟踪首次观察到非有限数；可能是中间值或效果参数，不一定已写入事件 |
| `ACTUAL_FLOAT_OP` | MMT `hurt` / `onLivingDamage`、TetraWear `onLivingHurt` 实际执行的浮点运算，含原操作数、结果、类、方法、源码行及操作编号 |
| `EVENT_FINITE_TO_NONFINITE` | `LivingHurtEvent` / `LivingDamageEvent.setAmount` 的实际值从有限变为非有限，含调用者 |
| `MMT_EFFECT` | 效果监听器写入的固定伤害、普通倍率或追加的独立倍率，含调用者；追加倍率不是伤害结果 |
| `ORIGINAL_EXCEPTION` / `FINAL_TRACE` | 原始异常或 Forge 阶段退出时的记录，含输入、返回值及实体上下文 |

MMT 的部分保护分支可能丢弃已溢出的中间结果，最终写入有限伤害。应结合事件实际写入与 AttributesLib `getAValue` 入口检查点归因。

每个 Forge hurt/damage 阶段单独编号；嵌套调用保留父级关联，但编号不是贯穿整次攻击的全局 ID。每条记录保留最近 48 项并单独保留首次非有限项，每分钟最多接纳 20 条异常或极大值跟踪。未获接纳的记录会被限流。

任意监听器通过上述两个事件的 `setAmount` 写入异常值，均可定位到调用者；逐次算术探针仅覆盖表中指定的 MMT / TetraWear 方法。直接改字段、其他方法的内部运算，以及 ForgeHooks 作用域外的手动事件可能需要追加探针。此功能不能保证定位所有模组内部的第一条异常算术指令。

## 验证状态

已通过 Java 17 完整 Gradle build；`damageDiagnosticsTest` 验证实际字节码替换的五种 float 运算、边界值及随机输入、有限保护结果、历史保留和防重复插桩。使用 `-PdamageDiagnosticTargets=../mods` 核对当前安装 JAR 的目标运算数量。

尚未完成真实 Forge 客户端启动、Mixin 应用及游戏内复现，尚不能宣布现场根因已定位。
