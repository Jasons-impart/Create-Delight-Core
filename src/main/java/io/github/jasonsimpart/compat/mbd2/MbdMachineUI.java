package io.github.jasonsimpart.compat.mbd2;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.appliedenergistics.yoga.YogaPositionType;

final class MbdMachineUI {
    private MbdMachineUI() {}

    static UITemplate create(CompoundTag data) {
        var root = element(data, true);
        return UITemplate.of(root, StylesheetManager.MC);
    }

    private static UIElement element(CompoundTag data, boolean root) {
        UIElement element = switch (data.getString("type")) {
            case "item_slot" -> new ItemSlot();
            case "fluid_slot" -> fluid(data);
            case "create_rotation" -> new com.lowdragmc.mbd2.integration.create.CreateRotationElement();
            case "player_inventory" -> new InventorySlots();
            case "progress" -> progress(data);
            case "text_texture", "label" -> label(data);
            case "button" -> new Button().noText().buttonStyle(style -> style
                    .baseTexture(IGuiTexture.EMPTY).hoverTexture(IGuiTexture.EMPTY).pressedTexture(IGuiTexture.EMPTY));
            case "group" -> new UIElement();
            case "tab_group" -> tabs(data);
            case "switch" -> toggle(data);
            case "text_field" -> new TextField();
            case "draggable_scrollable_group" -> new ScrollerView()
                    .scrollerStyle(style -> style.mode(ScrollerMode.VERTICAL));
            default -> throw new IllegalArgumentException("Unsupported machine UI element " + data);
        };
        element.setId(data.getString("id"));
        element.layout(layout -> {
            layout.width(data.getInt("width")).height(data.getInt("height")).paddingAll(0).marginAll(0);
            if (!root) layout.positionType(YogaPositionType.ABSOLUTE).left(data.getInt("x")).top(data.getInt("y"));
        });
        if (element instanceof InventorySlots inventory) {
            inventory.layout(layout -> layout.paddingAll(5));
            inventory.hotbar.layout(layout -> layout.marginTop(4));
        }
        if (data.contains("background")) {
            element.style(style -> style.backgroundTexture(texture(data.getCompound("background"))));
        }
        if (data.contains("tooltips")) {
            var tooltips = data.getList("tooltips", Tag.TAG_STRING).stream().map(raw ->
                    net.minecraft.network.chat.Component.Serializer.fromJson(raw.getAsString(),
                            com.lowdragmc.lowdraglib2.Platform.getFrozenRegistry())).toArray(net.minecraft.network.chat.Component[]::new);
            element.style(style -> style.tooltips(tooltips));
        }
        int contentHeight = 0;
        for (var raw : data.getList("children", Tag.TAG_COMPOUND)) {
            var child = (CompoundTag) raw;
            contentHeight = Math.max(contentHeight, child.getInt("y") + child.getInt("height"));
            if (element instanceof ScrollerView scroller) scroller.addScrollViewChild(element(child, false));
            else element.addChild(element(child, false));
        }
        if (element instanceof ScrollerView scroller) {
            int height = contentHeight;
            scroller.viewContainer.layout(layout -> layout.height(height).width(data.getInt("width") - 10));
        }
        return element;
    }

    private static Label label(CompoundTag data) {
        var label = new Label();
        if (data.contains("component")) label.setText(net.minecraft.network.chat.Component.Serializer.fromJson(
                data.getString("component"), com.lowdragmc.lowdraglib2.Platform.getFrozenRegistry()));
        else label.setText(data.getString("text"));
        return label;
    }

    private static Toggle toggle(CompoundTag data) {
        var toggle = new Toggle().setOn(data.getBoolean("pressed"), false);
        toggle.toggleLabel.setDisplay(false);
        toggle.toggleButton.layout(layout -> layout.widthPercent(100).heightPercent(100).setAspectRatio(Float.NaN));
        toggle.toggleStyle(style -> style.baseTexture(IGuiTexture.EMPTY)
                .hoverTexture(texture(data.getCompound("hoverTexture")))
                .unmarkTexture(texture(data.getCompound("baseTexture")))
                .markTexture(texture(data.getCompound("pressedTexture"))));
        return toggle;
    }

    private static UIElement tabs(CompoundTag data) {
        var root = new UIElement();
        var pages = new java.util.ArrayList<UIElement>();
        var buttons = new java.util.ArrayList<Toggle>();
        for (var raw : data.getList("tabs", Tag.TAG_COMPOUND)) {
            var tab = (CompoundTag) raw;
            var page = element(tab.getCompound("group"), false);
            page.layout(layout -> layout.width(data.getInt("width")).height(data.getInt("height")));
            var button = (Toggle) element(tab.getCompound("button"), false);
            page.setId("legacy_tab_page_" + pages.size());
            button.setId("legacy_tab_button_" + buttons.size());
            pages.add(page);
            buttons.add(button);
            root.addChildren(page, button);
        }
        for (int i = 0; i < buttons.size(); i++) {
            pages.get(i).setDisplay(i == 0);
            buttons.get(i).setOn(i == 0, false);
        }
        return root;
    }

    private static FluidSlot fluid(CompoundTag data) {
        var slot = new FluidSlot();
        slot.setAllowClickFilled(data.getBoolean("allowClickFilled"));
        slot.setAllowClickDrained(data.getBoolean("allowClickDrained"));
        slot.amountLabel.setVisible(data.getBoolean("showAmount"));
        slot.slotStyle(style -> style.fillDirection(FillDirection.valueOf(data.getString("fillDirection"))));
        slot.style(style -> style.overlay(texture(data.getCompound("overlay"))));
        return slot;
    }

    private static ProgressBar progress(CompoundTag data) {
        var bar = new ProgressBar();
        bar.label.setVisible(false);
        bar.barContainer.layout(layout -> layout.paddingAll(0))
                .style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        bar.barBackground.style(style -> style.backgroundTexture(texture(data.getCompound("empty"))));
        bar.bar.style(style -> style.backgroundTexture(texture(data.getCompound("filled"))));
        bar.progressBarStyle(style -> style.fillDirection(FillDirection.valueOf(data.getString("direction"))));
        return bar;
    }

    static IGuiTexture texture(CompoundTag data) {
        if (data.isEmpty()) return IGuiTexture.EMPTY;
        if (data.contains("borderWidth")) return new com.lowdragmc.lowdraglib2.gui.texture.ColorBorderTexture(
                data.getInt("borderWidth"), data.getInt("color"));
        if (data.contains("textures")) {
            var layers = new java.util.ArrayList<IGuiTexture>();
            for (var raw : data.getList("textures", Tag.TAG_COMPOUND)) layers.add(texture((CompoundTag) raw));
            return com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup.of(layers.toArray(IGuiTexture[]::new));
        }
        if (data.contains("items")) {
            var items = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
            for (var raw : data.getList("items", Tag.TAG_STRING)) items.add(new net.minecraft.world.item.ItemStack(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                            net.minecraft.resources.ResourceLocation.parse(raw.getAsString()))));
            return new com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture(items.toArray(net.minecraft.world.item.ItemStack[]::new))
                    .rotate(data.getFloat("rotation")).scale(data.getFloat("scale"))
                    .transform(data.getFloat("xOffset"), data.getFloat("yOffset"));
        }
        var sprite = SpriteTexture.of(data.getString("image"));
        var rect = data.getIntArray("rect");
        if (rect.length == 4) sprite.setSprite(rect[0], rect[1], rect[2], rect[3]);
        var border = data.getIntArray("border");
        if (border.length == 2) sprite.setBorder(border[0], border[1], border[0], border[1]);
        return sprite;
    }
}
