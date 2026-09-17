# Disabled Content KubeJS API

本文档记录 CDC 提供给 KubeJS 的禁用物品/方块 API。目标是给整合包脚本一个统一入口，处理“拿不到、放不了、配方消失、创造栏隐藏、世界生成替换”等需求，同时避免玩家背包、掉落物、容器、历史区块这类高成本扫描。

Create Delight Core exposes server-script KubeJS events. 以下事件写在 `kubejs/server_scripts/` 中：

```js
CreateDelightCoreEvents.disabledItems(event => {
  event.item("minecraft:elytra")
})

CreateDelightCoreEvents.disabledBlocks(event => {
  event.block("minecraft:tnt")
})
```

If a rule has no chained policy calls, CDC applies the full default policy for that target.

也就是说：**链式不写策略就是默认全禁**。如果链上写了任意策略，则只启用你显式写出来的策略。

Default item policy:

- remove matching recipes
- hide from creative tabs/search
- block right-click use
- block attacking with the item
- block equipping the item

Default block policy:

- remove matching recipes
- hide its block item from creative tabs/search
- block using its block item
- block player placement
- replace matching `setBlock`, chunk writes, and worldgen placements with air

## Item Rules

物品规则用于禁用 item stack。方块物品也可以通过 `disabledItems` 禁用，但如果你要处理世界里的 block state 或 worldgen 替换，应使用 `disabledBlocks`。

```js
CreateDelightCoreEvents.disabledItems(event => {
  event.item("minecraft:elytra")

  event.items([
    "minecraft:debug_stick",
    "minecraft:command_block"
  ]).hideFromCreativeTabs().blockUse()

  event.tag("c:bad_tools")
    .removeRecipes()
    .blockUse()

  event.regex(/some_mod:debug_.*/)
    .hideFromCreativeTabs()

  event.stack("minecraft:stick", {
    createdelight_test: "blocked"
  }).blockUse()
})
```

Available item policies:

- `.removeRecipes()`
- `.hideFromCreativeTabs()`
- `.blockUse()`
- `.blockAttack()`
- `.blockEquip()`

Chained policies are per rule. Different items can use different policies.

每条规则独立保存策略，不会影响后面的规则。

## Block Rules

方块规则用于禁用 block / block state / block entity。默认 block 策略会把未来 `setBlock` / worldgen 放置出来的匹配方块替换为空气。

```js
CreateDelightCoreEvents.disabledBlocks(event => {
  event.block("minecraft:tnt")

  event.blocks([
    "minecraft:bedrock",
    "minecraft:end_portal_frame"
  ]).hideFromCreativeTabs().blockPlace()

  event.tag("minecraft:replaceable_by_trees")
    .replaceGeneratedWith("minecraft:air")

  event.state("minecraft:candle", {
    candles: "4",
    lit: "true"
  }).blockPlace().replaceGeneratedWith("minecraft:air")

  event.blockEntity("minecraft:spawner", {
    SpawnData: {
      entity: {
        id: "minecraft:zombie"
      }
    }
  }).replaceGeneratedWith("minecraft:air")
})
```

Available block policies:

- `.removeRecipes()`
- `.hideFromCreativeTabs()`
- `.blockUse()`
- `.blockPlace()`
- `.replaceGeneratedWith("namespace:block")`
- `.replaceWith("namespace:block")`

`replaceWith(...)` is an alias for `replaceGeneratedWith(...)`.

`replaceGeneratedWith(...)` only changes the replacement block. By itself it still uses the default block policy set. If you combine it with explicit policies such as `.blockPlace()`, CDC keeps replacement enabled too, regardless of chain order.

如果想把矿石替换成石头，可以写：

```js
CreateDelightCoreEvents.disabledBlocks(event => {
  event.block("some_mod:bad_ore").replaceGeneratedWith("minecraft:stone")
})
```

## Creative Tab Rules

按创造模式 Tab 的注册 ID 隐藏整个标签页：

```js
// kubejs/server_scripts/disabled_creative_tabs.js
CreateDelightCoreEvents.disabledCreativeTabs(event => {
  event.tab('tacz:other')
  event.tabs(['example:tab_a', 'example:tab_b'])
})
```

- `tab(id)` 添加一个 ID；`tabs(ids)` 添加多个 ID，重复 ID 自动去重。
- 使用 Tab 注册 ID，不是显示名称、物品 ID 或模组 ID。无效格式会警告并跳过。
- 保留 Tab 注册、排序关系及内容，只过滤创造模式界面的分页列表和可见标签。普通 Tab 隐藏后不占用分页位置。
- 不禁用该 Tab 的物品，不移除配方，也不控制 JEI 物品列表。彻底禁用物品/方块仍用 `disabledItems` / `disabledBlocks`。
- 规则由服务端执行，玩家入服及 `/reload` 后同步完整快照；客户端无需重复放置此脚本，但须安装支持此 API 的 CDC。
- `/reload` 会重新收集规则。删除一条规则或整个事件脚本后，对应 Tab 恢复；已打开的创造模式界面会重新初始化分页，当前 Tab 被隐藏时选择仍可见的 Tab。
- 特殊 Tab（如 `minecraft:search`、`minecraft:hotbar`、`minecraft:inventory`）也支持隐藏。如果所有可见 Tab 都被隐藏，创造模式界面会关闭，避免空列表导致崩溃。
- 退服清理客户端快照；单人内置服务端与客户端也使用独立快照。
- 服务端找不到 ID 时在 KubeJS 服务端日志中警告，但仍下发 ID，以支持仅客户端注册的 Tab。客户端也找不到时记录明确警告并忽略，不会崩溃。

这里的网络同步仅针对 `disabledCreativeTabs`，不改变已有物品/方块禁用规则的同步行为。

## Matching Notes

`event.state(id, properties)` matches a block ID plus the listed state properties only. Unlisted properties are ignored.

state 属性值建议都写成字符串，和 blockstate JSON 中的值一致。

`event.stack(id, nbt)` matches partial custom item data. This is not a generic component matcher; Minecraft 1.21 stores many things, such as potions, in data components rather than legacy item NBT.

`event.blockEntity(id, nbt)` matches partial block entity saved NBT when a block entity is available. Plain blocks do not have NBT.

Recipe removal scans recipe JSON for matching IDs, tags, and regex strings. It is intentionally conservative and runs during KubeJS recipe loading, before normal `ServerEvents.recipes`.

CDC does not scan player inventories, dropped items, containers, or existing chunks. The system prevents access paths and replaces future matching block placements; it does not clean historical worlds.

## Implementation Notes

- KubeJS 插件入口：`src/main/resources/kubejs.plugins.txt`
- 事件定义：`io.github.jasonsimpart.compat.kubejs.disabled.CreateDelightCoreKubeEvents`
- 规则存储和匹配：`io.github.jasonsimpart.disabled.DisabledContentManager`
- NeoForge 事件拦截：`io.github.jasonsimpart.disabled.DisabledContentEvents`
- 方块替换 mixin：
  - `io.github.jasonsimpart.mixin.minecraft.ChunkAccessDisabledBlockMixin`
  - `io.github.jasonsimpart.mixin.minecraft.LevelDisabledBlockMixin`
  - `io.github.jasonsimpart.mixin.minecraft.WorldGenRegionDisabledBlockMixin`
