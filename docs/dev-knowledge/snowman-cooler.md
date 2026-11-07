# 雪傀儡冷却室

| 项目 | 当前规则与入口 |
|---|---|
| 方块与物品 | `createdelightcore:snowman_cooler`、`empty_snowman_cooler`、`frozen_cake`、`frozen_cake_base`；通过 `CDBlocks`、`CDBlockEntities`、`CDItems` 注册。空冷却室右键雪傀儡获得完整冷却室。 |
| 扳手 | 空冷却室与完整冷却室均实现 Create `IWrenchable`；潜行使用扳手拆卸，生存模式返还对应方块物品。 |
| 工作盆 | 沿用 `create:mixing` / `create:compacting`，`heatRequirement` 为 `cooled` 或 `frozen`。冷却室放在工作盆正下方；冰冻可满足两档条件，待机和烈焰人燃烧室不能满足冷却条件。 |
| 固体燃料 | `createdelightcore:snowman_cooler_fuel/regular` 与 `/special` 物品标签；普通一次 1600 tick，特殊一次 3200 tick。寒冰蛋糕属于特殊燃料，支持机械手。 |
| 原生流体槽 | 1000 mB，Forge 流体能力开放所有面；支持管道与流体容器投料，不依赖 Create Liquid Fuel 或 Fluid Logistics 的机器实现。 |
| 耗料 | 当前周期结束后才扣除下一个完整批次；不足一批、失效或被数据包移除的流体仍可抽出。特殊流体到期不会获得额外的普通冷却时长。 |
| 护目镜 | 顺序为状态、流体槽、剩余时间；普通冷却/冰冻沿用 JEI 青色/蓝色，待机为浅灰。`GoggleTooltipStyle.addFluidLine` 经 `compat/createcybergoggles/CyberGogglesFluidTooltip` 调用 CCG 7.2.2 的 `CCGLang.fluid(...).forGoggles(...)`，保留组件标记以显示纹理、填充比例及容量文字；无 CCG 或 API 不兼容时退回文本。 |
| JEI | `CDJEI` 向 Create 原版 `mystery_conversion` 分类注册 `createdelightcore:snowman_capture`，展示空冷却室捕捉雪傀儡后的物品关系。原生燃料分类显示每批用量、持续时间和冷却档位；服务端登录/数据包重载时同步燃料，客户端刷新并隐藏过期配方，退出服务器清空缓存。 |
| 蛋糕 | 蛋糕胚与寒冰蛋糕贴图来自 CMR；蛋糕可食用，恢复 10 饥饿值，饱和度系数 0.7，赋予 30 秒缓慢 III / 力量 III，并增加 800 tick 冰冻。 |
| 整合包配置 | `kubejs/server_scripts/Create Delight/snowman_cooler.js` 保留原有蛋糕糊→蛋糕胚→注液制作链和冷却配方；`Custom/food/icecream.js` 使用原生冷却条件；鼓风机继续使用龙加的冷冻及 `passive_block_freezers` 标签。 |
| 授权 | CMR 参考版本、修改范围和 MIT 文本见 [打包署名](../../src/main/resources/META-INF/licenses/create-more-recipes.txt)。 |
| 验证 | `SnowmanCoolerGameTests` 覆盖流体批次/余量/持久化、桶与机械手模拟、特殊燃料到期、冷热工作盆匹配、无效燃料参数，以及空/完整冷却室扳手拆卸、生存单件/堆叠捕捉、创造捕捉与其他生物拒绝；随现有测试共 29 项通过。JEI 画面、Flywheel 与完整整合包客户端尚需实机验收。 |

## 自定义液体燃料

文件位置为 `data/<数据包命名空间>/snowman_cooler_fuel/<名称>.json`，字段沿用 Create Liquid Fuel：

```json
{
  "fluid": "createdelight:cryo_fuel",
  "burnTime": 30,
  "superHeat": true,
  "amountConsumedPerTick": 1
}
```

- `fluid` 为已注册的流体 ID；不支持物品 ID 或标签。
- `burnTime` 为一批燃料提供的 tick 数，必须大于零。
- `superHeat` 为 `true` 时提供冰冻，`false` 时提供普通冷却；也接受 `freezing`，两者同时存在时以 `freezing` 为准。
- `amountConsumedPerTick` 保留上游字段名称，实际表示每批消耗的 mB，范围为 1～1000；不是每 tick 扣一次。
- 未填写时，普通档默认 20 tick / 1 mB，冰冻档默认 32 tick / 10 mB。数据包通过同一资源路径覆盖默认定义；无效定义记录错误并跳过。

实现入口为 `content/cooler/SnowmanCoolerFuelManager.java`、`SnowmanCoolerBlockEntity.java`，热条件与工作盆桥接在 `mixin/create/{CoolerHeatConditionMixin,SnowmanCoolingBasinMixin}.java`。
