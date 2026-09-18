# CDC 模组兼容补丁

| 修复 | 外部触发条件 | 实现与证据 | 复核条件 | 状态 |
|---|---|---|---|---|
| 抗性中间乘法溢出 | Alex's Mobs 1.22.9 蟾蜍捕食传入 MAX；AttributesLib 1.3.7 使无抗性也执行原版 `damage * 25 / 25`，在 Forge 47.4.16 上已实测首个乘法溢出。 | `DamagePipelinePreparation` 在同一入口先执行 `ResistanceDamageTransformer` 再安装探针；[诊断与验证](../damage-diagnostics.md)、[CDC PR #126](https://github.com/Jasons-impart/Create-Delight-Core/pull/126)。 | Minecraft 抗性方法、AttributesLib 重定向或相关 Mixin 改写时复核唯一指令对；上游改为安全运算后移除对应重排。 | 组合入口修正版已真实命中，6 条非暴击样本后续有限；另一次 CDC 暴击溢出复现 BigDecimal/Neruina 链，仍待处理。 |
| 蟾蜍捕食极值与通用暴击冲突 | Alex's Mobs 1.22.9 特殊捕食使用 MAX，AttributesLib 1.3.7 通用暴击将其放大为 Infinity，TetraWear 1.0.0 随后触发 BigDecimal 异常。 | `alexsmobs/WarpedToadPredationDamageMixin` 将唯一捕食极值替换为 10,000；[诊断与验收](../damage-diagnostics.md)。 | Alex's Mobs 捕食分支、目标范围或极值常量变化时复核；上游改为合理有限伤害后评估移除。 | 用户已批准 10,000 测试值，保留诊断，真实捕食及最高阶段击杀验证待完成。 |
