# CDC 兼容修复台账

本台账记录 CDC 中用于恢复预期行为或适配上游变更的兼容修复；功能设计和玩家可见内容仍记录在 `content-map.md`。

| 修复 | 受影响版本 | 实现位置 | 验证 | 状态与复核条件 |
|---|---|---|---|---|
| Alex's Caves 核爆遗留强加载区块 | Alex's Caves 2.0.2；Forge 1.20.1-47.4.16 | `mixin/alexscaves/NuclearExplosionEntityMixin.java` 将 `NuclearExplosionEntity#loadChunksAround` 调用 `ForgeChunkManager.forceChunk` 的最后一个参数固定为 `true`，使移除路径按创建时的 `ENTITY_TICKING` 票据类型释放区块。 | `./gradlew build --no-daemon`；反汇编确认目标调用描述符和第 6 个参数。 | PR 未合并；Alex's Caves 或 Forge 升级后复核 `loadChunksAround` 调用描述符，并在游戏内完成一次核爆后确认周边区块不再被强加载。 |
