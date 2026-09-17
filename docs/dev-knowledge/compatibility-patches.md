# CDC 模组兼容补丁

| 修复 | 外部触发条件 | 实现与证据 | 复核条件 | 状态 |
|---|---|---|---|---|
| 抗性中间乘法溢出 | Alex's Mobs 1.22.9 蟾蜍捕食传入 MAX；AttributesLib 1.3.7 使无抗性也执行原版 `damage * 25 / 25`，在 Forge 47.4.16 上已实测首个乘法溢出。 | `ResistanceDamageMixin` 与 `ResistanceDamageTransformer` 将抗性公式改为先算比例；[诊断与验证](../damage-diagnostics.md)、[CDC PR #126](https://github.com/Jasons-impart/Create-Delight-Core/pull/126)。 | Minecraft 抗性方法、AttributesLib 重定向或相关 Mixin 改写时复核唯一指令对；上游改为安全运算后移除对应重排。 | 源码与字节码回归通过，本地测试部署；真实修复回归及原下界 BigDecimal 异常闭环待完成。 |
