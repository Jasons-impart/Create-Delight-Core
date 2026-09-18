# 模组兼容补丁

| 问题 | 外部触发条件 | 实现 | 验证与复核 |
| --- | --- | --- | --- |
| 蟾蜍捕食极值伤害溢出 | Alex's Mobs 1.22.9 对绯红蚊捕食传入 `Float.MAX_VALUE`；AttributesLib 1.3.7 通用暴击放大后变成 Infinity，TetraWear 1.0.0 护甲研磨随后调用 BigDecimal 抛错。 | `mixin/alexsmobs/WarpedToadPredationDamageMixin` 将 tick 中绯红蚊分支唯一极值改为 10,000；保留普通攻击、伤害源及事件/死亡流程，模组缺失时跳过。 | 同一捕食补丁已在带诊断的测试包验证 10,000 普通捕食与目标生命归零；暴击和最高阶段组合没有完整实测。正式分支只含本补丁，不含临时探针或抗性重排。升级 Alex's Mobs 后复核 named/SRG tick 与唯一常量；上游改为安全有限伤害后评估移除。调查见 [CDC #126](https://github.com/Jasons-impart/Create-Delight-Core/pull/126) / [CDR #2298](https://github.com/Jasons-impart/Create-Delight-Remake/pull/2298)。 |
