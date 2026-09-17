# 非有限伤害运行时诊断

诊断探针用于定位伤害链中的 Infinity / NaN，本身不修改伤害或取消事件。当前分支另含已授权的抗性公式定点修复：先算比例，再乘伤害；MMT 适用实体范围不变，其他异常仍按原流程传播。

## 当前修复：抗性先算比例

原表达式 `damage * factor / 25.0F` 改为 `damage * (factor / 25.0F)`。`factor = 25 - 5 * (amplifier + 1)` 保持不变，不使用整数除法。无抗性时系数为 25，MAX 乘 1 仍有限；抗性 I～V 对应比例 0.8～0，有限非负输入不会在这一步溢出。

实现由 `LivingDamagePipelineArithmeticMixin` 的单个 postApply 调用 `compat/combat/DamagePipelinePreparation`：先执行 `ResistanceDamageTransformer`，再安装管线探针，顺序由同一方法保证，不依赖两个 Mixin 的相对优先级。补丁匹配 LivingEntity 抗性方法中唯一的“乘法临时值/除以25”指令对，将浮点除法移到乘法前，移除原后置除法；保留原始伤害副本、伤害标签旁路、Sundering、统计、附魔保护与后续流程。玩家继承此方法。修复独立于诊断开关生效。启动应出现 `[CDCore][ResistanceDamage] Applied ratio-first ... before probes`；匹配不是恰好一处时抛出明确错误。当前 Mixin 配置为非 required，失败可能只报警后继续启动，因此进入游戏不等于补丁生效，必须核对成功标记与运算日志。

用户已接受运算顺序改变造成的浮点舍入差异，不承诺普通结果逐位等同旧公式。5000 个 0～10000 普通伤害采样中，相对旧公式的最大相对舍入差为约 `1.59E-7`（约 0.000016%）；这不是所有 float 输入的误差上界。测试覆盖 MAX、零/负值、非有限输入、抗性等级、旁路分支和 Sundering，直接在 JVM 执行改写后的 fixture，并验证实际 mapped/SRG 与游戏合并类的匹配及栈正确性。

修复不钳制已有 Infinity/NaN，也不改 Sundering 后续增伤或异常等级。Sundering 与 MAX 组合仍可能在后续运算溢出，因此继续保留全管线探针。下一轮以新的绯红蚊重复捕食，确认日志先出现 `25 / 25 = 1`、再出现 `MAX * 1 = MAX`，并检查 Damage 入参、吸收与生命处理是否又出现非有限值。源码/构建测试通过后仍需真实重启验证补丁命中及后续行为。

## 2026-09-18 修复版启动失败与执行顺序修正

00:09:14 启动日志先记录 LivingEntity 管线探针，随后 `ResistanceDamageMixin` 失败：`Expected exactly one resistance damage*factor/25 sequence ... found 0`。00:13:10—00:13:24 的 7 个独立父 Trace（#1、#4、#7、#10、#13、#16、#19）仍首次出现 `MAX * 25 = Infinity`；这是旧公式仍在执行，不能归为新发现的后续溢出。没有 `ORIGINAL_EXCEPTION` 或 BigDecimal `NumberFormatException`。失败版本源码为 `cc8bdc6`，JAR SHA-256 为 `20f1d7fd7ab9f038c577d70a958bad465384227e2bee8b3308714e3b1739a641`。

原实现假定两个 marker 的 priority 2/1 保证修复先于插桩，实际 postApply 顺序未满足此假定。探针已消耗原 FMUL/FDIV，因此匹配为零。旧测试先移除观察器，再单独执行补丁，遗漏了入口组合错误。现移除独立 `ResistanceDamageMixin`，由同一个入口顺序执行修复和观察；测试复现“先观察后修复”必然失败，并直接 JVM 执行生产组合入口生成的 fixture，真实类校验也使用该入口。还原导出类的观察器仅用于生成测试输入，不是运行时去除探针。

本地失败日志与导出类保存在 `tmp-opencode/damage-diagnostics-repro-20260918-0013/`，不随 Git 分发。修正后的真实启动与捕食验证仍待完成；下一轮必须同时检查成功标记、先除后乘与后续数值。

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
