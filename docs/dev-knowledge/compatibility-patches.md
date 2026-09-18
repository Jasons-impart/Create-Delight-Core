# 模组兼容补丁

| 问题 | 外部触发条件 | 实现 | 验证与复核 |
| --- | --- | --- | --- |
| KubeJS Lazy 并发空缓存 | KubeJS `get()` / `forget()` 并发读写分离的 cached/value；整方法加锁又在任意 factory 回调中引入锁顺序风险。 | `mixin/kubejs/LazyMixin` 通过 `CombatMixinPlugin.postApply` 桥接到 `util/KubeJsLazyCache` 原子快照，factory 不持缓存锁，失效前的计算不能回填。 | build.16/build.24/build.26 独立回归及锁顺序探针通过，Forge 冷启动、连续重载和服务器回归待测。升级 KubeJS 后复核结构与语义，上游修复后评估移除。来源 [CDC #131](https://github.com/Jasons-impart/Create-Delight-Core/pull/131)；审计和迁移见 [专题](kubejs-lazy.md)。 |
| 蟾蜍捕食极值伤害溢出 | Alex's Mobs 1.22.9 对绯红蚊捕食传入 `Float.MAX_VALUE`；AttributesLib 1.3.7 通用暴击放大后变成 Infinity，TetraWear 1.0.0 护甲研磨随后调用 BigDecimal 抛错。 | `mixin/alexsmobs/WarpedToadPredationDamageMixin` 将 tick 中绯红蚊分支唯一极值改为 10,000；保留普通攻击、伤害源及事件/死亡流程，模组缺失时跳过。 | 同一捕食补丁已在带诊断的测试包验证 10,000 普通捕食与目标生命归零；暴击和最高阶段组合没有完整实测。正式分支只含本补丁，不含临时探针或抗性重排。升级 Alex's Mobs 后复核 named/SRG tick 与唯一常量；上游改为安全有限伤害后评估移除。调查见 [CDC #126](https://github.com/Jasons-impart/Create-Delight-Core/pull/126) / [CDR #2298](https://github.com/Jasons-impart/Create-Delight-Remake/pull/2298)。 |
