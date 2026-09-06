package io.github.jasonsimpart.createdelightcore.data.lang;

import com.tterrag.registrate.util.entry.FluidEntry;
import io.github.jasonsimpart.createdelightcore.CreateDelightCore;
import io.github.jasonsimpart.createdelightcore.registry.CDBlocks;
import io.github.jasonsimpart.createdelightcore.registry.CDCreativeTabs;
import io.github.jasonsimpart.createdelightcore.registry.CDFluids;
import io.github.jasonsimpart.createdelightcore.registry.CDItems;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

public class ChineseLangHandler {
    private static void coin(RegistrateCNLangProvider provider, Supplier<Item> item, String name, String initial) {
        var id = item.get().getDescriptionId();
        provider.add(id, name);
        provider.add(id + ".initial", initial);
        provider.add(id + ".plural", name);
    }
    private static void addFluid(RegistrateCNLangProvider provider, FluidEntry<?> fluid, String name) {
        var namespace = fluid.getId().getNamespace();
        var id = fluid.getId().getPath();
        if (id.startsWith("flowing_"))
            id = id.substring("flowing_".length());
        provider.add("fluid." + namespace + "." + id, name);
        provider.add("block." + namespace + "." + id, name);
        provider.add(fluid.getBucket().get(), name + "桶");
    }
    private static void addVirtualFluid(RegistrateCNLangProvider provider, FluidEntry<?> fluid, String name) {
        var namespace = fluid.getId().getNamespace();
        var id = fluid.getId().getPath();
        if (id.startsWith("flowing_"))
            id = id.substring("flowing_".length());
        provider.add("fluid." + namespace + "." + id, name);
    }

    private static void addManualOverrides(RegistrateCNLangProvider provider) {
        replace(provider, "itemGroup.createdelightcore.coin", "钱币与物品");
        replace(provider, "item.createdelightcore.iron_coin", "§7铁币");
        replace(provider, "item.createdelightcore.iron_coin.plural", "§7铁币");
        replace(provider, "item.createdelightcore.copper_coin", "§e铜币");
        replace(provider, "item.createdelightcore.copper_coin.plural", "§e铜币");
        replace(provider, "item.createdelightcore.gold_coin", "§6金币");
        replace(provider, "item.createdelightcore.gold_coin.plural", "§6金币");
        replace(provider, "item.createdelightcore.emerald_coin", "§2绿宝石币");
        replace(provider, "item.createdelightcore.emerald_coin.plural", "§2绿宝石币");
        replace(provider, "item.createdelightcore.netherite_coin", "§5下界合金币");
        replace(provider, "item.createdelightcore.netherite_coin.plural", "§5下界合金币");
        replace(provider, "block.createdelightcore.order_requester", "订单请求器");
        replace(provider, "block.createdelightcore.order_board", "订单公告板");
        replace(provider, "block.createdelightcore.life_matter_injector", "生命质注入器");
        replace(provider, "createdelightcore.life_matter_injector.powered", "已禁用");
        replace(provider, "createdelightcore.life_matter_injector.ready", "待命");
        replace(provider, "createdelightcore.life_matter_injector.docking.none", "未对准");
        replace(provider, "createdelightcore.life_matter_injector.docking.ready", "已对准（%s/%s）");
        replace(provider, "createdelightcore.life_matter_injector.docking.full", "控制器已满（%s/%s）");
        replace(provider, "createdelightcore.life_matter_injector.status", "输入：%s | 上次注入：%s | 朝向：%s | 对接：%s | %s");
        replace(provider, "createdelightcore.direction.down", "下");
        replace(provider, "createdelightcore.direction.up", "上");
        replace(provider, "createdelightcore.direction.north", "北");
        replace(provider, "createdelightcore.direction.south", "南");
        replace(provider, "createdelightcore.direction.west", "西");
        replace(provider, "createdelightcore.direction.east", "东");
        replace(provider, "block.createdelightcore.quality_harvest_controller", "品控收割控制器");
        replace(provider, "createdelightcore.quality_harvest_controller.no_calibrator", "未安装校准器");
        replace(provider, "createdelightcore.quality_harvest_controller.status", "生命质：%s/%s | %s");
        replace(provider, "createdelightcore.gui.address", "地址");
        replace(provider, "createdelightcore.gui.candidates", "候选物品");
        replace(provider, "createdelightcore.gui.estimate", "预期：%s");
        replace(provider, "createdelightcore.gui.expected_reward", "预期收益：%s");
        replace(provider, "createdelightcore.gui.reward_score", "收益评分：%s");
        replace(provider, "createdelightcore.gui.estimate.incomplete", "未满足");
        replace(provider, "createdelightcore.gui.estimate.normal", "普通");
        replace(provider, "createdelightcore.gui.estimate.good", "良好");
        replace(provider, "createdelightcore.gui.estimate.excellent", "优秀");
        replace(provider, "createdelightcore.gui.estimate.great", "极佳");
        replace(provider, "createdelightcore.gui.estimate.great_overflow", "%s %s");
        replace(provider, "createdelightcore.gui.full", "完整");
        replace(provider, "createdelightcore.gui.missing", "缺货");
        replace(provider, "createdelightcore.gui.mode_fixed", "数量");
        replace(provider, "createdelightcore.gui.mode_ratio", "配比");
        replace(provider, "createdelightcore.gui.order", "订单");
        replace(provider, "createdelightcore.gui.partial", "允许部分");
        replace(provider, "createdelightcore.gui.planned_count", "用%s");
        replace(provider, "createdelightcore.gui.shortage_count", "缺%s");
        replace(provider, "createdelightcore.gui.summary_total", "合计");
        replace(provider, "createdelightcore.gui.ratio_parts_short", "%s份");
        replace(provider, "createdelightcore.gui.quantity_hint", "点击调整数量，按住 Shift 每次调整 16。");
        replace(provider, "createdelightcore.gui.weight_hint", "点击调整份数，按住 Shift 每次调整 16。");
        replace(provider, "createdelightcore.gui.refresh", "刷新");
        replace(provider, "createdelightcore.gui.save", "保存");
        replace(provider, "createdelightcore.gui.select_candidates", "选择候选");
        replace(provider, "createdelightcore.gui.send", "发送");
        replace(provider, "createdelightcore.gui.help.select", "左键候选会选择最多一组物品。");
        replace(provider, "createdelightcore.gui.help.multi", "同一条目可以继续选择其他候选混合提交。");
        replace(provider, "createdelightcore.gui.help.quantity", "使用 - / + 调整已选数量。");
        replace(provider, "createdelightcore.gui.help.cancel", "右键候选可以取消选择。");
        replace(provider, "createdelightcore.gui.help.ratio_select", "左键加入候选，右键移除候选。");
        replace(provider, "createdelightcore.gui.help.ratio_weight", "用 - / + 调整份数；份数越多，这种物品分到的订单需求越多。");
        replace(provider, "createdelightcore.gui.help.ratio_planned", "每行“用X”表示这张订单预计会消耗 X 个。");
        replace(provider, "createdelightcore.gui.help.ratio_shortage", "红色行表示库存不足；“缺X”就是还差 X 个。");
        replace(provider, "createdelightcore.gui.help.ratio_missing", "缺货候选会保留；补货后可继续使用这套配比。");
        replace(provider, "createdelightcore.gui.help.score", "收益评分越高，完成订单时获得的奖励包和钱币越多。");
        replace(provider, "createdelightcore.gui.help.redstone", "给机器一个红石脉冲，会按已保存方案发送请求。");
        replace(provider, "createdelightcore.order_request.no_selection", "请至少选择一个候选物品");
        replace(provider, "createdelightcore.order_request.saved", "订单请求已保存");
        replace(provider, "createdelightcore.order_request.sent", "订单请求已发送");
        replace(provider, "createdelightcore.order_request.not_enough_items", "绑定网络中没有足够的匹配物品");
        replace(provider, "createdelightcore.order_request.failed", "订单请求失败：请检查物流绑定、地址和包裹网络");
        replace(provider, "createdelightcore.order_board.accept", "领取草稿");
        replace(provider, "createdelightcore.order_board.loading", "正在读取今日委托……");
        replace(provider, "createdelightcore.order_board.kind.adapted", "适配单");
        replace(provider, "createdelightcore.order_board.kind.expansion", "拓展单");
        replace(provider, "createdelightcore.order_board.kind.opportunity", "机会单");
        replace(provider, "createdelightcore.order_board.grade", "%s级 · %s");
        replace(provider, "createdelightcore.order_board.total", "数量 %s–%s");
        replace(provider, "createdelightcore.order_board.required", "必含：%s");
        replace(provider, "createdelightcore.order_board.stock", "仓库已有 %s（%s%%）");
        replace(provider, "createdelightcore.order_board.market", "额外金币 ×%s");
        replace(provider, "createdelightcore.order_board.reroll", "重置候选（%s）");
        replace(provider, "createdelightcore.order_board.refresh_in", "距离刷新 %s");
        replace(provider, "createdelightcore.order_board.accepted_wait", "今日已领取 · %s 后刷新");
        replace(provider, "createdelightcore.order_board.hint", "查看三张草稿，选择一张领取");
        replace(provider, "createdelightcore.order_board.already_accepted", "今天已经领取过公告板订单");
        replace(provider, "createdelightcore.order_board.invalid_candidate", "这张草稿已失效");
        replace(provider, "createdelightcore.order_board.candidates_refreshed", "今日订单已更新");
        replace(provider, "createdelightcore.order_board.missing_draft_item", "无法找到未开启的订单物品");
        replace(provider, "createdelightcore.order_board.accepted", "已领取订单草稿");
        replace(provider, "createdelightcore.order_board.rerolled", "已支付 %s，重新生成今日候选");
        replace(provider, "createdelightcore.order_board.reroll_insufficient", "余额不足，重置候选需要 %s");
        replace(provider, "createdelightcore.order_board.reroll_after_accept", "领取草稿后不能再重置今日候选");
        replace(provider, "createdelightcore.order_board.reroll_unavailable", "当前无法生成新的候选，未扣除费用");
        replace(provider, "createdelightcore.supply_commission.submit", "确认委托");
        replace(provider, "createdelightcore.supply_commission.cancel", "撤销所选");
        replace(provider, "createdelightcore.supply_commission.vouchers", "商会凭证");
        replace(provider, "createdelightcore.supply_commission.output", "到货包裹");
        replace(provider, "createdelightcore.supply_commission.no_unlocked_catalog", "尚未发现可委托商品");
        replace(provider, "createdelightcore.supply_commission.catalog_position", "可用目录 %s/%s");
        replace(provider, "createdelightcore.supply_commission.preview_batch_simple", "每批 %s 个");
        replace(provider, "createdelightcore.supply_commission.preview_cost", "费用 %s 张凭证 + %s");
        replace(provider, "createdelightcore.supply_commission.preview_time", "预计工期 %s");
        replace(provider, "createdelightcore.supply_commission.queue_header", "队列 · 待取%s");
        replace(provider, "createdelightcore.supply_commission.queue_page", "%s/%s");
        replace(provider, "createdelightcore.supply_commission.queue_empty", "当前没有进行中的委托");
        replace(provider, "createdelightcore.supply_commission.queue_remaining", "剩余 %s");
        replace(provider, "createdelightcore.supply_commission.queue_owner", "%s · %s");
        replace(provider, "createdelightcore.supply_commission.break_active", "请先处理进行中的委托和待提货包裹");
        replace(provider, "createdelightcore.supply_commission.completed_waiting", "%s ×%s 已完成生产，正在 %s 等待输出");
        replace(provider, "createdelightcore.supply_commission.arrived", "%s ×%s 已到货，委托台位于 %s");
        replace(provider, "createdelightcore.supply_commission.output_reserved", "这份到货包裹属于 %s");
        replace(provider, "createdelightcore.supply_commission.submit.success", "供货委托已确认");
        replace(provider, "createdelightcore.supply_commission.submit.catalog_missing", "当前没有有效的供货目标");
        replace(provider, "createdelightcore.supply_commission.submit.item_not_discovered", "需要先获得一次该物品");
        replace(provider, "createdelightcore.supply_commission.item_discovered", "已解锁供货委托：%s");
        replace(provider, "createdelightcore.supply_commission.items_discovered", "已解锁 %s 项供货委托");
        replace(provider, "createdelightcore.supply_commission.submit.vouchers_missing", "委托台中的订单商会凭证不足");
        replace(provider, "createdelightcore.supply_commission.submit.money_missing", "LC 钱包余额不足");
        replace(provider, "createdelightcore.supply_commission.submit.payment_failed", "委托付款失败，未创建委托");
        replace(provider, "createdelightcore.supply_commission.cancel.full_refund", "委托已在宽限期内撤销：退回全部金币和凭证");
        replace(provider, "createdelightcore.supply_commission.cancel.partial_refund", "委托已撤销：退回 50% 金币，凭证不退");
        replace(provider, "createdelightcore.supply_commission.cancel.not_owner", "只能由委托人撤销这项委托");
        replace(provider, "createdelightcore.supply_commission.cancel.already_complete", "供货已经完成；请先取走输出包裹");
        replace(provider, "createdelightcore.supply_commission.cancel.not_found", "这项委托已经不存在");
    }

    public static void init(RegistrateCNLangProvider provider) {
        //creativetabs
        provider.add(CDCreativeTabs.MISC.get(), "齿轮盛宴 | 杂项");
        provider.add(CDCreativeTabs.COIN.get(), "齿轮盛宴 | 货币");
        provider.add(CDCreativeTabs.FLUID.get(), "齿轮盛宴 | 流体");
        provider.add(CDCreativeTabs.FOOD.get(), "齿轮盛宴 | 食物");
        provider.add("key.categories.createdelightcore", "齿轮盛宴");
        provider.addItem(CDItems.KINETIC_CONFIGURATION_MODULE, "传动构形模块");
        provider.addItem(CDItems.STRUCTURAL_CONFIGURATION_MODULE, "结构构形模块");
        provider.addItem(CDItems.FLUID_CONFIGURATION_MODULE, "流体构形模块");
        provider.addItem(CDItems.CONTROL_CONFIGURATION_MODULE, "控制构形模块");
        provider.addItem(CDItems.LOGISTICS_CONFIGURATION_MODULE, "物流构形模块");
        provider.addItem(CDItems.SPONSOR_MEDAL, "§dJSI赞助纪念章");
        provider.addBlock(CDBlocks.SUPPLY_COMMISSION_TABLE, "供货委托台");
        provider.addTooltip(CDItems.SPONSOR_MEDAL, "感谢你对JSI制作组的支持");
        provider.add("item.createdelightcore.configuration_module.tooltip.mode", "当前构形：%s");
        provider.add("item.createdelightcore.configuration_module.tooltip.unselected", "当前构形：未选择");
        provider.add("item.createdelightcore.configuration_module.tooltip.target", "目标 ID：%s");
        provider.add("item.createdelightcore.configuration_module.tooltip.charge", "构形量：%s / %s");
        provider.add("item.createdelightcore.configuration_module.tooltip.cost", "单次消耗：%s 点构形量");
        provider.add("item.createdelightcore.configuration_module.tooltip.control", "按住 %s 打开构形选择轮盘");
        provider.add("item.createdelightcore.configuration_module.tooltip.auto_refill", "构形量不足时自动消耗背包中的充填组件");
        provider.add("item.createdelightcore.configuration_module.message.mode", "已切换为：%s");
        provider.add("item.createdelightcore.configuration_module.message.auto_refill", "自动充填 %s 次，补充 %s 点构形量");
        provider.add("item.createdelightcore.configuration_module.error.no_modes", "当前没有可用构形");
        provider.add("item.createdelightcore.configuration_module.error.invalid_target", "所选构形无法放置");
        provider.add("item.createdelightcore.configuration_module.error.invalid_mode", "所选构形已不可用");
        provider.add("item.createdelightcore.configuration_module.error.no_charge", "构形量不足，且背包中没有足够的充填组件");
        provider.add("menu.createdelightcore.configuration_module.title", "选择构形");
        provider.add("menu.createdelightcore.configuration_module.release_to_select", "松开 %s 选择构形");
        // food
        provider.addItem(CDItems.UNFRIED_SHRIMP, "生炸虾仁");
        provider.addItem(CDItems.UNFRIED_CHICKEN_CHIP, "生炸鸡块");
        provider.addItem(CDItems.UNFRIED_CHICKEN_LEG, "生炸鸡腿");
        provider.addItem(CDItems.UNFRIED_TONKATSU, "生炸猪排");
        provider.addItem(CDItems.UNFRIED_FISH, "生炸鱼");
        provider.addItem(CDItems.UNFRIED_POTATO, "生炸土豆");
        provider.addItem(CDItems.UNFRIED_CALAMARI, "生炸鱿鱼圈");
        replace(provider, CDItems.PIZZA_SLICE.get().getDescriptionId(), "披萨切片");
        replace(provider, CDItems.VEGETABLE_PIZZA_SLICE.get().getDescriptionId(), "蔬菜披萨切片");
        replace(provider, CDItems.MEATLOVERS_PIZZA_SLICE.get().getDescriptionId(), "肉披萨切片");
        replace(provider, CDItems.NETHER_PIZZA_SLICE.get().getDescriptionId(), "下界披萨切片");
        provider.addItem(CDItems.LUSH_CONFITURE_JELLO, "繁茂果冻");
        // chocolate
        provider.addItem(CDItems.BLACK_CHOCOLATE_MOLD_FILLED, "盛满的黑巧克力模具");
        provider.addItem(CDItems.BLACK_CHOCOLATE_MOLD_SOLID, "凝固的黑巧克力");
        provider.addItem(CDItems.WHITE_CHOCOLATE_MOLD_FILLED, "盛满的白巧克力模具");
        provider.addItem(CDItems.WHITE_CHOCOLATE_MOLD_SOLID, "凝固的白巧克力");
        provider.addItem(CDItems.RUBY_CHOCOLATE_MOLD_FILLED, "盛满的红宝石巧克力模具");
        provider.addItem(CDItems.RUBY_CHOCOLATE_MOLD_SOLID, "凝固的红宝石巧克力");
        //tin
        provider.addItem(CDItems.RAW_TIN, "粗锡");
        provider.addItem(CDItems.TIN_INGOT, "锡锭");
        provider.addItem(CDItems.TIN_NUGGET, "锡粒");
        //bronze
        provider.addItem(CDItems.BRONZE_INGOT, "青铜锭");
        provider.addItem(CDItems.BRONZE_NUGGET, "青铜粒");

        //block
        provider.addBlock(CDBlocks.FRAGMENT_OF_BORDER, "边境碎片");
        provider.addBlock(CDBlocks.TIN_ORE, "锡矿石");
        provider.addBlock(CDBlocks.DEEPSLATE_TIN_ORE, "深层锡矿石");
        provider.addBlock(CDBlocks.RAW_TIN, "粗锡块");
        provider.addBlock(CDBlocks.TIN, "锡块");
        provider.addBlock(CDBlocks.BRONZE, "青铜块");
        provider.addBlock(CDBlocks.FORGED_STEEL, "锻造钢块");
        //casing
        provider.addBlock(CDBlocks.STEEL_CASING, "钢制机壳");
        provider.addBlock(CDBlocks.FORGE_STEEL_CASING, "锻造钢机壳");
        provider.addBlock(CDBlocks.STEEL_GLASS_CASING, "钢制玻璃机壳");
        provider.addBlock(CDBlocks.STEEL_CLEAR_GLASS_CASING, "钢制通透玻璃机壳");
        //syrup
        provider.addBlock(CDBlocks.BASE, "基础糖浆块");
        provider.addBlock(CDBlocks.STRAWBERRY, "草莓糖浆块");
        provider.addBlock(CDBlocks.VANILLA, "香草糖浆块");
        provider.addBlock(CDBlocks.MINT, "薄荷糖浆块");
        provider.addBlock(CDBlocks.BANANA, "香蕉糖浆块");
        provider.addBlock(CDBlocks.COCONUT, "椰子糖浆块");
        //fruits leaves
        provider.addBlock(CDBlocks.JUJUBE_LEAVES, "大枣树叶");
        provider.addBlock(CDBlocks.JUJUBE_SAPLING, "大枣树苗");
        provider.addBlock(CDBlocks.WALNUT_LEAVES, "核桃树叶");
        provider.addBlock(CDBlocks.WALNUT_SAPLING, "核桃树苗");
        //jelly bottle
        provider.addBlock(CDBlocks.LUSH_CONFITURE, "繁茂果酱瓶");
        //jelly_block
        provider.addBlock(CDBlocks.LUSH_CONFITURE_JELLY, "繁茂果酱块");
        //jello_block
        provider.addBlock(CDBlocks.LUSH_CONFITURE_JELLO_BLOCK, "繁茂果冻块");
        //coin
        coin(provider, CDItems.IRON, "铁币", "铁");
        coin(provider, CDItems.COPPER, "铜币", "铜");
        coin(provider, CDItems.GOLD, "金币", "金");
        coin(provider, CDItems.EMERALD, "绿宝石币", "绿");
        coin(provider, CDItems.NETHERITE, "下界合金币", "下界");
        provider.addBlock(CDBlocks.IRON_COIN_PILE, "铁币堆");
        provider.addBlock(CDBlocks.COPPER_COIN_PILE, "铜币堆");
        provider.addBlock(CDBlocks.GOLD_COIN_PILE, "金币堆");
        provider.addBlock(CDBlocks.EMERALD_COIN_PILE, "绿宝石币堆");
        provider.addBlock(CDBlocks.NETHERITE_COIN_PILE, "下界合金币堆");
        provider.addBlock(CDBlocks.PHANTOM_COMPOST, "幻灵肥料");
        provider.addBlock(CDBlocks.LUNA_SOIL, "月壤沃土");
        provider.addBlock(CDBlocks.LUNA_SOIL_FARMLAND, "月壤沃土耕地");
        provider.addBlock(CDBlocks.FIRE_LILY_CLUSTER, "烈焰百合簇");
        provider.addBlock(CDBlocks.FROST_LILY_CLUSTER, "寒冰百合簇");
        provider.addBlock(CDBlocks.LIGHTNING_LILY_CLUSTER, "闪电百合簇");
        replace(provider, CDBlocks.RAW_VEGETABLE_PIZZA.get().getDescriptionId(), "生蔬菜披萨");
        replace(provider, CDBlocks.VEGETABLE_PIZZA.get().getDescriptionId(), "蔬菜披萨");
        replace(provider, CDBlocks.RAW_MEATLOVERS_PIZZA.get().getDescriptionId(), "生肉披萨");
        replace(provider, CDBlocks.MEATLOVERS_PIZZA.get().getDescriptionId(), "肉披萨");
        replace(provider, CDBlocks.RAW_NETHER_PIZZA.get().getDescriptionId(), "生下界披萨");
        replace(provider, CDBlocks.NETHER_PIZZA.get().getDescriptionId(), "下界披萨");
        //molten fluid
        addFluid(provider, CDFluids.MOLTEN_ANDESITE, "熔融安山合金");
        addFluid(provider, CDFluids.MOLTEN_AZURE_NEODYMIUM, "熔融青钕合金");
        addFluid(provider, CDFluids.MOLTEN_SCARLET_NEODYMIUM, "熔融赤钕合金");
        addFluid(provider, CDFluids.MOLTEN_TITANIUM, "熔融钛");
        addFluid(provider, CDFluids.MOLTEN_MARTIAN_STEEL, "熔融火星钢");
        addFluid(provider, CDFluids.MOLTEN_FIRE_STEEL, "熔融龙炎钢");
        addFluid(provider, CDFluids.MOLTEN_ICE_STEEL, "熔融龙霜钢");
        addFluid(provider, CDFluids.MOLTEN_LIGHTNING_STEEL, "熔融龙霆钢");
        addFluid(provider, CDFluids.MOLTEN_FORGED_STEEL, "熔融锻造钢");
        addFluid(provider, CDFluids.MOLTEN_GLASS, "熔融玻璃");
        addFluid(provider, CDFluids.MOLTEN_QUARTZ_GLASS, "熔融石英玻璃");
        addFluid(provider, CDFluids.MOLTEN_QUARTZ_VIBRANT_GLASS, "熔融聚能石英玻璃");
        //ice cream scoop
        provider.addItem(CDItems.STRAWBERRY_ICE_CREAM_SCOOP, "草莓味冰淇淋球");
        provider.addItem(CDItems.BANANA_ICE_CREAM_SCOOP, "香蕉味冰淇淋球");
        provider.addItem(CDItems.MINT_ICE_CREAM_SCOOP, "薄荷味冰淇淋球");
        provider.addItem(CDItems.ADZUKI_ICE_CREAM_SCOOP, "红豆味冰淇淋球");
        provider.addItem(CDItems.POMEGRANATE_ICE_CREAM_SCOOP, "石榴味冰淇淋球");
        provider.addItem(CDItems.LIME_ICE_CREAM_SCOOP, "青柠味冰淇淋球");
        provider.addItem(CDItems.APPLE_ICE_CREAM_SCOOP, "苹果味冰淇淋球");
        provider.addItem(CDItems.BEETROOT_ICE_CREAM_SCOOP, "甜菜根味冰淇淋球");
        provider.addItem(CDItems.CARROT_ICE_CREAM_SCOOP, "胡萝卜味冰淇淋球");
        provider.addItem(CDItems.ENCHANTED_FRUIT_ICE_CREAM_SCOOP, "附魔之果味冰淇淋球");
        provider.addItem(CDItems.GLOW_BERRY_ICE_CREAM_SCOOP, "发光浆果味冰淇淋球");
        provider.addItem(CDItems.PUMPKIN_ICE_CREAM_SCOOP, "南瓜味冰淇淋球");
        provider.addItem(CDItems.LUCUMA_ICE_CREAM_SCOOP, "蛋黄果味冰淇淋球");
        provider.addItem(CDItems.PINK_DRAGON_FRUIT_ICE_CREAM_SCOOP, "火龙果味冰淇淋球");
        //slime
        addFluid(provider, CDFluids.SLIME, "黏液");
        addFluid(provider, CDFluids.FERROUSLIME, "富铁黏液");
        addFluid(provider, CDFluids.CHORUSSLIME, "紫颂黏液");
        //radiation
        addFluid(provider, CDFluids.NUCLEAR_WASTE, "核废液");
        //milkshake
        addVirtualFluid(provider, CDFluids.APPLE, "苹果味奶昔");
        addVirtualFluid(provider, CDFluids.GLOW_BERRY, "发光浆果味奶昔");
        addVirtualFluid(provider, CDFluids.CARROT, "胡萝卜味奶昔");
        addVirtualFluid(provider, CDFluids.BEETROOT, "甜菜味奶昔");
        addVirtualFluid(provider, CDFluids.ENCHANTED_FRUIT, "附魔之果味奶昔");
        addVirtualFluid(provider, CDFluids.LUCUMA, "蛋黄果味奶昔");
        addVirtualFluid(provider, CDFluids.PINK_DRAGON_FRUIT, "火龙果味奶昔");
        //grape juice
        addVirtualFluid(provider, CDFluids.RED_GRAPE, "红葡萄汁");
        addVirtualFluid(provider, CDFluids.JUNGLE_RED_GRAPE, "丛林红葡萄汁");
        addVirtualFluid(provider, CDFluids.SAVANNA_RED_GRAPE, "热带草原红葡萄汁");
        addVirtualFluid(provider, CDFluids.TAIGA_RED_GRAPE, "针叶林红葡萄汁");
        addVirtualFluid(provider, CDFluids.WHITE_GRAPE, "白葡萄汁");
        addVirtualFluid(provider, CDFluids.JUNGLE_WHITE_GRAPE, "丛林白葡萄汁");
        addVirtualFluid(provider, CDFluids.SAVANNA_WHITE_GRAPE, "热带草原白葡萄汁");
        addVirtualFluid(provider, CDFluids.TAIGA_WHITE_GRAPE, "针叶林白葡萄汁");
        addVirtualFluid(provider, CDFluids.WARPED_GRAPE, "诡异葡萄汁");
        addVirtualFluid(provider, CDFluids.CRIMSON_GRAPE, "绯红葡萄汁");
        //syrup
        addVirtualFluid(provider, CDFluids.BASE_SYRUP, "基础糖浆");
        addVirtualFluid(provider, CDFluids.STRAWBERRY_SYRUP, "草莓糖浆");
        addVirtualFluid(provider, CDFluids.VANILLA_SYRUP, "香草糖浆");
        addVirtualFluid(provider, CDFluids.MINT_SYRUP, "薄荷糖浆");
        addVirtualFluid(provider, CDFluids.BANANA_SYRUP, "香蕉糖浆");
        addVirtualFluid(provider, CDFluids.COCONUT_SYRUP, "椰子糖浆");
        //Fruit Delight jelly/jello
        addVirtualFluid(provider, CDFluids.LUSH_CONFITURE_JELLY, "繁茂果酱(流动中)");
        addVirtualFluid(provider, CDFluids.LUSH_CONFITURE_JELLO, "繁茂熔融果冻(流动中)");
        addVirtualFluid(provider, CDFluids.GENETIC_CULTURE, "遗传培养液");
        //recipes
        provider.add("death.attack." + CreateDelightCore.MODID + ".molten_metal", "%1$s变成了熔融金属的一部分");
        provider.add("death.attack." + CreateDelightCore.MODID + ".molten_metal.player", "%2$s以为%1$s是一块没有融化的金属");
        provider.add("death.attack." + CreateDelightCore.MODID + ".ice_cream", "%1$s因为吃掉过多的冰淇淋而冻成了冰棍");
        provider.add("death.attack." + CreateDelightCore.MODID + ".ice_cream.player", "%1$s因为吃掉过多的冰淇淋而冻成了冰棍");
        provider.add("death.attack." + CreateDelightCore.MODID + ".radiation", "%1$s因辐射而浑身溃烂");
        provider.add("death.attack." + CreateDelightCore.MODID + ".radiation.player", "%1$s因辐射而浑身溃烂");
        //jei
        provider.add("jei." + CreateDelightCore.MODID + ".BlazeBurnerFluid", "烈焰人液体燃料配方");
        provider.add("jei." + CreateDelightCore.MODID + ".amountConsume", "消耗%smB");
        provider.add("jei." + CreateDelightCore.MODID + ".SnowmanCoolerFluid", "雪傀儡液体冷却剂配方");
        provider.add("jei." + CreateDelightCore.MODID + ".BlazeCoolerFluid", "烈焰人冷却室液体冷却剂配方");
        provider.add("jei." + CreateDelightCore.MODID + ".amountConsumeCool", "消耗%smB");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting", "幻化");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting.dimension", "在外星球中幻化");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting.light", "有日光时会加快");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting.fluid", "周围有灵质时会加快");
        provider.add("jei." + CreateDelightCore.MODID + ".phantomComposting.accelerators", "周围有以下幻化催化剂时会加快");
        //tooltip
        provider.add("tooltip." + CreateDelightCore.MODID + ".holdShiftToSeeHeat", "§r§8按住 [§r§7Shift§r§8] 查看烈焰人燃烧信息§r");
        provider.add("tooltip." + CreateDelightCore.MODID + ".holdShiftHeat", "§r§8按住 [§r§rShift§r§8] 查看烈焰人燃烧信息§r");
        provider.add("tooltip." + CreateDelightCore.MODID + ".amountConsume", "§8消耗燃料: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".burnTime", "§8燃烧时间: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".heatType", "§8燃烧类型: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".superHeat", "超级燃烧");
        provider.add("tooltip." + CreateDelightCore.MODID + ".Heat", "加热");

        provider.add("tooltip." + CreateDelightCore.MODID + ".holdControlToSeeCool", "§r§8按住 [§r§7Ctrl§r§8] 查看雪傀儡冷却信息§r");
        provider.add("tooltip." + CreateDelightCore.MODID + ".holdControlCool", "§r§8按住 [§r§rCtrl§r§8] 查看雪傀儡冷却信息§r");
        provider.add("tooltip." + CreateDelightCore.MODID + ".coolTime", "§8冷却时间: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".coolType", "§8冷却类型: ");
        provider.add("tooltip." + CreateDelightCore.MODID + ".Frozen", "速冻");
        provider.add("tooltip." + CreateDelightCore.MODID + ".Cooled", "冷却");

        provider.add("tooltip." + CreateDelightCore.MODID + ".jelly_block", "带有粘性，但是不和其他粘性方块粘黏");
        provider.add("tooltip." + CreateDelightCore.MODID + ".jello_block", "光滑，但是能和相同果酱/果冻方块粘黏");
        //jade
        provider.add("config.jade.plugin_balm.jade", "Jade");
        provider.add("config.jade.plugin_createdelightcore.cmr.snowman_cooler", "雪傀儡冷却器");
        //waystone
        provider.add("gui." + CreateDelightCore.MODID + ".need", "需要：");
        provider.add("gui." + CreateDelightCore.MODID + ".free", "免费");
        addManualOverrides(provider);
    }

    public static void replace(@NotNull RegistrateCNLangProvider provider, @NotNull String key,
                               @NotNull String value) {
        try {
            // the regular lang mappings
            Field field = LanguageProvider.class.getDeclaredField("data");
            field.setAccessible(true);
            // noinspection unchecked
            Map<String, String> map = (Map<String, String>) field.get(provider);
            map.put(key, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error replacing entry in datagen.", e);
        }
    }
}
