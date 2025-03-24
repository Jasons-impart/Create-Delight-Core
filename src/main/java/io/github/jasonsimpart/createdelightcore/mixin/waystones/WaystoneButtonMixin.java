package io.github.jasonsimpart.createdelightcore.mixin.waystones;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.jasonsimpart.createdelightcore.CDConfig;
import io.github.jasonsimpart.createdelightcore.content.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.client.gui.widget.WaystoneButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mixin(WaystoneButton.class)
public class WaystoneButtonMixin extends Button {

    @Final
    @Shadow(remap = false)
    private int xpLevelCost;

    @Final
    @Shadow(remap = false)
    private IWaystone waystone;

    @Unique
    private int CreateDelightCore$distance;

    @Unique
    private int CreateDelightCore$cost;

    @Unique
    private boolean CreateDelightCore$canAfford;

    @Unique
    private boolean CreateDelightCore$isRender;
    protected WaystoneButtonMixin(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress, CreateNarration pCreateNarration) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pCreateNarration);
    }

    // 额外判断isRender
    @Inject(method = "<init>", at = @At("TAIL"), remap = false, cancellable = true)
    private void WaystoneButton(int x, int y, IWaystone waystone, int xpLevelCost, OnPress pressable, CallbackInfo ci) {

        if (CDConfig.useMoneyTeleport) {
            active = true;
            Player player = Minecraft.getInstance().player;

            if (player == null) {
                return;
            }
            // 计算距离
            CreateDelightCore$distance = (int) player.position().distanceTo(waystone.getPos().getCenter());
            CreateDelightCore$cost = CDConfig.teleportCost * xpLevelCost;
            // 计算传送费用并判断余额是否足以支付传送费用
            CreateDelightCore$canAfford = MoneyUtil.playerCanAfford(player, MoneyUtil.baseCoinNumberToCoinValue(CreateDelightCore$cost));
            // 余额不足
            if (!CreateDelightCore$canAfford && !player.getAbilities().instabuild) {
                active = false;
            }
            // 渲染距离大于5m以上的传送石按钮
            if (CreateDelightCore$distance > 5) {
                CreateDelightCore$isRender = true;
            }

            ci.cancel();
        }
    }

    // 重写渲染方法
    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (CDConfig.useMoneyTeleport) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            Minecraft mc = Minecraft.getInstance();

            // 渲染传送花费
            if (CreateDelightCore$isRender) {

                // 渲染距离
                if (waystone.getDimension() == mc.player.level().dimension()) {

                    String distanceStr;

                    if (CreateDelightCore$distance < 10000 && (mc.font.width(getMessage()) < 120 || CreateDelightCore$distance < 1000)) {
                        distanceStr = CreateDelightCore$distance + "m";
                    } else {
                        distanceStr = String.format("%.1f", CreateDelightCore$distance / 1000f).replace(",0", "").replace(".0", "") + "km";
                    }

                    int xOffset = getWidth() - mc.font.width(distanceStr);
                    guiGraphics.drawString(mc.font, distanceStr, getX() + xOffset - 4, getY() + 6, isActive() ? 0xFFFFFF : 0x9E9E9E);

                }
                if (CreateDelightCore$cost != 0) {
                    int xOffset = 0;
                    for (ItemStack item: ((CoinValue) MoneyUtil.baseCoinNumberToCoinValue(CreateDelightCore$cost)).getAsItemList()) {
                        guiGraphics.renderItem(item, getX() + 2 + xOffset, getY() + 2);
                        guiGraphics.drawString(mc.font, String.valueOf(item.getCount()), getX() + 16 + xOffset, getY() + 6 + 5, 16777215);
                        xOffset += 16;
                    }
                }

                // 判断鼠标指针位置
                if (isHovered && mouseX <= getX() + 16) {

                    final List<Component> tooltip = new ArrayList<>();

                    // 判断余额与经验是否足以传送消耗
                    boolean haveXpLevelRequirement = xpLevelCost > 0 && false;
                    boolean haveMoneyRequirement = CreateDelightCore$cost != 0;
                    boolean canXpLevelAfford = Objects.requireNonNull(mc.player).experienceLevel >= xpLevelCost || mc.player.getAbilities().instabuild;

                    // 经验消耗提示
                    if (haveXpLevelRequirement) {
                        final var levelRequirementText = Component.translatable("gui.waystones.waystone_selection.level_requirement", xpLevelCost);
                        levelRequirementText.withStyle(canXpLevelAfford ? ChatFormatting.GREEN : ChatFormatting.RED);
                        tooltip.add(levelRequirementText);
                    }

                    // 余额消耗提示
                    if (haveMoneyRequirement) {
                        String money = MoneyUtil.baseCoinNumberToCoinValue(CreateDelightCore$cost).getText().getString();
                        final var moneyRequirementText = Component.translatable("gui.createdelightcore.need", money);
                        moneyRequirementText.withStyle(CreateDelightCore$canAfford ? ChatFormatting.GREEN : ChatFormatting.RED);
                        tooltip.add(moneyRequirementText);
                    }

                    // 没有消耗
                    if (!haveXpLevelRequirement && !haveMoneyRequirement) {
                        final var moneyRequirementText = Component.translatable("gui.createdelightcore.free");
                        moneyRequirementText.withStyle(ChatFormatting.GREEN);
                        tooltip.add(moneyRequirementText);
                    }

                    guiGraphics.renderTooltip(mc.font, tooltip, Optional.empty(), mouseX, mouseY + mc.font.lineHeight);
                }
            }
            // 在头部混入并提前结束方法，来达到重写方法的目的
            ci.cancel();
        }
    }

}