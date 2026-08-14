package io.github.jasonsimpart.createdelightcore.mixin.tetra;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiButton;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiStringOutline;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HolosphereEntryData;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.item.HoloItemGui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Pseudo
@Mixin(targets = "se.mickelus.tetra.items.modular.impl.holo.gui.craft.item.HoloItemsGui",
        remap = false)
public abstract class HoloItemsGuiPaginationMixin extends GuiElement {
    @Unique
    private static final int CREATEDelightCore$PAGE_SIZE = 7;
    @Unique
    private static final int CREATEDelightCore$CONTROL_Y = 100;

    @Shadow
    @Final
    private GuiElement itemContainer;

    @Unique
    private List<HoloItemGui> createdelightcore$pageEntries = Collections.emptyList();
    @Unique
    private int createdelightcore$currentPage;
    @Unique
    private int createdelightcore$pageCount = 1;
    @Unique
    private boolean createdelightcore$showingItemList = true;
    @Unique
    private GuiButton createdelightcore$previousPageButton;
    @Unique
    private GuiButton createdelightcore$nextPageButton;
    @Unique
    private GuiStringOutline createdelightcore$pageLabel;

    protected HoloItemsGuiPaginationMixin(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Inject(
            method = "<init>(IIIILjava/util/function/Consumer;Ljava/util/function/Consumer;Ljava/lang/Runnable;)V",
            at = @At("RETURN")
    )
    private void createdelightcore$addPageControls(int x, int y, int width, int height,
                                                   Consumer<String> onItemSelect,
                                                   Consumer<String> onSlotSelect,
                                                   Runnable onMaterialsClick,
                                                   CallbackInfo ci) {
        this.createdelightcore$previousPageButton = new GuiButton(
                -32, CREATEDelightCore$CONTROL_Y, 16, 12, "<",
                () -> createdelightcore$changePage(-1));
        this.createdelightcore$previousPageButton.setAttachment(GuiAttachment.topCenter);
        this.addChild(this.createdelightcore$previousPageButton);

        this.createdelightcore$nextPageButton = new GuiButton(
                32, CREATEDelightCore$CONTROL_Y, 16, 12, ">",
                () -> createdelightcore$changePage(1));
        this.createdelightcore$nextPageButton.setAttachment(GuiAttachment.topCenter);
        this.addChild(this.createdelightcore$nextPageButton);

        this.createdelightcore$pageLabel = new GuiStringOutline(
                0, CREATEDelightCore$CONTROL_Y + 2, "");
        this.createdelightcore$pageLabel.setAttachment(GuiAttachment.topCenter);
        this.addChild(this.createdelightcore$pageLabel);
        createdelightcore$setControlsVisible(false);
    }

    @ModifyVariable(
            method = "createItem(Lse/mickelus/tetra/items/modular/impl/holo/gui/craft/HolosphereEntryData;I)Lse/mickelus/tetra/items/modular/impl/holo/gui/craft/item/HoloItemGui;",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            require = 1
    )
    private int createdelightcore$usePageLocalIndex(int index) {
        return Math.floorMod(index, CREATEDelightCore$PAGE_SIZE);
    }

    @Inject(
            method = "loadEntries(Ljava/util/List;)V",
            at = @At("RETURN"),
            require = 1
    )
    private void createdelightcore$collectEntries(List<HolosphereEntryData> entries, CallbackInfo ci) {
        this.createdelightcore$pageEntries = new ArrayList<>(
                this.itemContainer.getChildren(HoloItemGui.class));
        this.createdelightcore$pageCount = Math.max(1,
                (this.createdelightcore$pageEntries.size() + CREATEDelightCore$PAGE_SIZE - 1)
                        / CREATEDelightCore$PAGE_SIZE);
        this.createdelightcore$currentPage = Math.min(
                this.createdelightcore$currentPage,
                this.createdelightcore$pageCount - 1);
        createdelightcore$applyPage();
    }

    @Inject(
            method = "changeItem(Ljava/lang/String;)V",
            at = @At("RETURN"),
            require = 1
    )
    private void createdelightcore$updateControlsForSelection(String key, CallbackInfo ci) {
        this.createdelightcore$showingItemList = key == null;
        if (this.createdelightcore$showingItemList) {
            createdelightcore$restoreCurrentPage();
        } else {
            createdelightcore$setControlsVisible(false);
        }
    }

    @Unique
    private void createdelightcore$changePage(int offset) {
        int nextPage = Math.max(0, Math.min(
                this.createdelightcore$pageCount - 1,
                this.createdelightcore$currentPage + offset));
        if (nextPage != this.createdelightcore$currentPage) {
            this.createdelightcore$currentPage = nextPage;
            createdelightcore$applyPage();
        }
    }

    @Unique
    private void createdelightcore$applyPage() {
        if (!this.createdelightcore$showingItemList) {
            createdelightcore$setControlsVisible(false);
            return;
        }

        int firstEntry = this.createdelightcore$currentPage * CREATEDelightCore$PAGE_SIZE;
        int lastEntry = Math.min(
                firstEntry + CREATEDelightCore$PAGE_SIZE,
                this.createdelightcore$pageEntries.size());

        for (int index = 0; index < this.createdelightcore$pageEntries.size(); index++) {
            HoloItemGui entry = this.createdelightcore$pageEntries.get(index);
            boolean onCurrentPage = index >= firstEntry && index < lastEntry;
            entry.setVisible(onCurrentPage);
        }

        createdelightcore$updateControls();
    }

    @Unique
    private void createdelightcore$restoreCurrentPage() {
        int firstEntry = this.createdelightcore$currentPage * CREATEDelightCore$PAGE_SIZE;
        int lastEntry = Math.min(
                firstEntry + CREATEDelightCore$PAGE_SIZE,
                this.createdelightcore$pageEntries.size());

        for (int index = 0; index < this.createdelightcore$pageEntries.size(); index++) {
            HoloItemGui entry = this.createdelightcore$pageEntries.get(index);
            boolean onCurrentPage = index >= firstEntry && index < lastEntry;
            entry.setVisible(onCurrentPage);
            if (onCurrentPage) {
                // HoloItemGui#setVisible stops its active show animation before delegating to
                // GuiElement. During a detail -> list transition that otherwise leaves the
                // restored entries visible but fully transparent until another page refresh.
                entry.setOpacity(1.0F);
            }
        }

        createdelightcore$updateControls();
    }

    @Unique
    private void createdelightcore$updateControls() {
        if (this.createdelightcore$previousPageButton == null) {
            return;
        }

        boolean showControls = this.createdelightcore$pageCount > 1;
        createdelightcore$setControlsVisible(showControls);
        this.createdelightcore$previousPageButton.setEnabled(
                this.createdelightcore$currentPage > 0);
        this.createdelightcore$nextPageButton.setEnabled(
                this.createdelightcore$currentPage + 1 < this.createdelightcore$pageCount);
        this.createdelightcore$pageLabel.setString(
                (this.createdelightcore$currentPage + 1)
                        + " / " + this.createdelightcore$pageCount);
    }

    @Unique
    private void createdelightcore$setControlsVisible(boolean visible) {
        if (this.createdelightcore$previousPageButton != null) {
            this.createdelightcore$previousPageButton.setVisible(visible);
            this.createdelightcore$nextPageButton.setVisible(visible);
            this.createdelightcore$pageLabel.setVisible(visible);
        }
    }
}
