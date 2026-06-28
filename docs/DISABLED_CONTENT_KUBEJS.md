# Disabled Content KubeJS API

本文档记录 CDC 提供给 KubeJS 的禁用物品/方块 API。目标是给整合包脚本一个统一入口，处理“拿不到、放不了、配方消失、创造栏隐藏、世界生成替换”等需求，同时避免玩家背包、掉落物、容器、历史区块这类高成本扫描。

Create Delight Core exposes two server-script KubeJS events. 这两个事件写在 server script 中：

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
