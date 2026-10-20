# 铜工作盆产出流体品质修复

## 1. 问题与目标

FluidLogistics 的铜工作盆（Copper Basin）处理「产物为流体」的 Create 搅拌配方时，产物丢失 Quality Food 的品质；Create 原版工作盆不受影响。反馈单 MQSM7F（整合包 v0.5.0.15）。

本次修复只增加一条 CDC 兼容 Mixin，未修改 FluidLogistics、Quality Food Fluids、Create 或 Quality Food 的源码。

## 2. 改动清单

| 文件 | 修改内容 |
| --- | --- |
| `mixin/fluidlogistics/CopperBasinQualityMixin.java` | 新增。在 `CopperBasinBlockEntity#acceptOutputs` 的 HEAD 注入。 |
| `src/main/resources/mixins.createdelightcore.json` | 在 `mixins` 数组登记 `fluidlogistics.CopperBasinQualityMixin`。 |
| `mixin/CombatMixinPlugin.java` | `fluidlogistics` 分支增加判断：只有 `CopperBasinQualityMixin` 同时要求 `quality_food_fluids` 已加载，其余 `fluidlogistics` Mixin 行为不变。 |
| `build.gradle` | 增加 `quality_food_fluids` 的 compileOnly 本地依赖，写法与既有 `fluidlogistics` 本地依赖一致。 |

## 3. 根因

两条代码路径互相咬合：

1. **Create 原版工作盆**：`BasinRecipe.apply` 内部调用 `basin.acceptOutputs(items, fluids, simulate)`，Quality Food Fluids 的 `BasinBlockEntityMixin` 注入在这个方法的 HEAD，把品质写回 `outputFluids`。
2. **铜工作盆**：`CopperBasinBlockEntity extends BasinBlockEntity`，但**覆写了 `acceptOutputs`** —— 有流体时把流体从 `super` 调用里剥掉（传 `Collections.emptyList()`），改由 `acceptFluidsIntoOutputTank` 自己灌进 `outputTank`。

于是 Quality Food Fluids 的注入永远拿到空流体列表，守卫条件 `!hasQualityCapableOutput(空) && !hasQualityFluidSource(consumedFluids)` 成立时直接 `return true` 跳过，品质被静默丢弃。

## 4. 修法

`src/main/java/io/github/jasonsimpart/createdelightcore/mixin/fluidlogistics/CopperBasinQualityMixin.java`：

```java
@Mixin(value = CopperBasinBlockEntity.class, remap = false)
public abstract class CopperBasinQualityMixin {
    @Inject(method = "acceptOutputs", at = @At("HEAD"), remap = false)
    private void createdelightcore$applyFluidQuality(List<ItemStack> outputItems, List<FluidStack> outputFluids,
                                                     boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        BasinRecipeQualityContext.applyToOutputs((BasinBlockEntity) (Object) this, outputItems, outputFluids, simulate);
    }
}
```

注入点位于方法开头，此时 `outputFluids` 仍是 `BasinRecipe` 传进来的可变原列表；`applyToOutputs` 会用 Quality Food Fluids 已算好的 ticket 把带品质的 `FluidStack` 写回该列表，铜工作盆随后灌罐即为带品质流体。

### 安全性

- `applyToOutputs` 内部有 `context == null` 守卫：从非配方路径（管道插入、手动交互等）调用时直接返回 `true`，无副作用。
- 注入点在 `outputTank.allowInsertion()` 之前、`Collections.emptyList()` 分支之外，不干扰其 try/finally，也不会对不可变列表写入。
- `CombatMixinPlugin` 增加 `quality_food_fluids` 判断：缺少该模组时整条 Mixin 不应用，避免 `BasinRecipeQualityContext` 解析失败。

## 5. 验证

- 修复逻辑先在 0.5.0.15 测试服以 hotai 字节码补丁（`hotai/com/yision/fluidlogistics/content/processing/copperBasin/CopperBasinBlockEntity.badiff`）形式验证通过：铜工作盆 + 动力搅拌器，用 cosmopolitan 冰淇淋配方（带品质水果 1 + 冰块 1 + 糖 1 + 750mB 牛奶 → 750mB 冰淇淋流体），产出流体带品质。
- 本次 Mixin 在本地编译中未出现与 `CopperBasin`、`BasinRecipeQualityContext` 相关的错误。
- 完整 `./gradlew build` 在本机未通过：本地只有 `fluidlogistics-1.2.7`（`build.gradle` 的 compileOnly 指向 1.3.0），且 JEI 主 Maven 按约定禁用，既有的 `JeiCategoryBlazeCoolerFluid` / `CDJEI` 报「找不到符号」，与本次改动无关。需要在具备 `fluidlogistics-1.3.0` 与完整 JEI 依赖的环境补跑 `build` 与 `runClient`。

## 6. 影响范围

- 只影响铜工作盆产出流体的配方；Create 原版工作盆行为不变。
- 输入流体带品质的配方同样覆盖（走 `hasQualityFluidSource` 分支）。
- 未安装 FluidLogistics 或 Quality Food Fluids 时，对应 Mixin 不应用，无行为变化。

## 7. 上游归属

根因在 Quality Food Fluids 的 `BasinBlockEntityMixin` 只覆盖 Create 原版 `BasinBlockEntity`、没有覆盖 FluidLogistics 的子类。本修复以 CDC 兼容层形式提供；若 Quality Food Fluids 后续在自身加 `@Mixin(CopperBasinBlockEntity.class)`，此补丁可移除。
