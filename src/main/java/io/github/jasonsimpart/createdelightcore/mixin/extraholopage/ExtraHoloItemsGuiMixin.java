package io.github.jasonsimpart.createdelightcore.mixin.extraholopage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiButton;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiStringOutline;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloItemGui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Pseudo
@Mixin(targets = "net.yiran.extraholopage.gui.ExtraHoloItemsGui", remap = false)
public abstract class ExtraHoloItemsGuiMixin extends GuiElement {
    @Unique
    private static final int CREATEDelightCore$PAGE_SIZE = 7;
    @Unique
    private static final int CREATEDelightCore$PAGE_CONTROL_Y = 100;

    @Unique
    private List<GuiElement> createdelightcore$pageEntries = Collections.emptyList();
    @Unique
    private int createdelightcore$currentPage;
    @Unique
    private int createdelightcore$pageCount = 1;
    @Unique
    private GuiButton createdelightcore$previousPageButton;
    @Unique
    private GuiButton createdelightcore$nextPageButton;
    @Unique
    private GuiStringOutline createdelightcore$pageLabel;
    @Unique
    private int createdelightcore$constructedEntryCount;

    protected ExtraHoloItemsGuiMixin(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @ModifyArgs(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lse/mickelus/tetra/items/modular/impl/holo/gui/craft/HoloItemGui;<init>(IILse/mickelus/tetra/items/modular/IModularItem;ILjava/lang/Runnable;Ljava/util/function/Consumer;)V"
            ),
            require = 1
    )
    private void createdelightcore$usePageLocalEntryCoordinates(Args args) {
        int pageIndex = this.createdelightcore$constructedEntryCount++
                % CREATEDelightCore$PAGE_SIZE;
        args.set(0, createdelightcore$getEntryX(pageIndex));
        args.set(1, createdelightcore$getEntryY(pageIndex));
    }

    @Inject(method = "<init>", at = @At("RETURN"), require = 0)
    private void createdelightcore$setupPagination(CallbackInfo ci) {
        List<GuiElement> entries = new ArrayList<>(this.getChildren(HoloItemGui.class));

        this.createdelightcore$pageEntries = entries;
        this.createdelightcore$pageCount = Math.max(1,
                (entries.size() + CREATEDelightCore$PAGE_SIZE - 1) / CREATEDelightCore$PAGE_SIZE);

        for (int index = 0; index < entries.size(); index++) {
            GuiElement entry = entries.get(index);
            int pageIndex = index % CREATEDelightCore$PAGE_SIZE;
            entry.setX(createdelightcore$getEntryX(pageIndex));
            entry.setY(createdelightcore$getEntryY(pageIndex));
        }

        this.createdelightcore$previousPageButton = new GuiButton(
                -32,
                CREATEDelightCore$PAGE_CONTROL_Y,
                16,
                12,
                "<",
                () -> createdelightcore$changePage(-1)
        );
        this.createdelightcore$previousPageButton.setAttachment(GuiAttachment.topCenter);
        this.addChild(this.createdelightcore$previousPageButton);

        this.createdelightcore$nextPageButton = new GuiButton(
                32,
                CREATEDelightCore$PAGE_CONTROL_Y,
                16,
                12,
                ">",
                () -> createdelightcore$changePage(1)
        );
        this.createdelightcore$nextPageButton.setAttachment(GuiAttachment.topCenter);
        this.addChild(this.createdelightcore$nextPageButton);

        this.createdelightcore$pageLabel = new GuiStringOutline(
                0,
                CREATEDelightCore$PAGE_CONTROL_Y + 2,
                ""
        );
        this.createdelightcore$pageLabel.setAttachment(GuiAttachment.topCenter);
        this.addChild(this.createdelightcore$pageLabel);

        createdelightcore$applyPage(true);
    }

    @Inject(method = "changeItem", at = @At("RETURN"), require = 0)
    private void createdelightcore$restorePageAfterItemChange(IModularItem item, CallbackInfo ci) {
        boolean showingItemList = item == null;
        createdelightcore$setControlsVisible(showingItemList && this.createdelightcore$pageCount > 1);
        if (showingItemList) {
            createdelightcore$applyPage(false);
        }
    }

    @Unique
    private void createdelightcore$changePage(int offset) {
        int nextPage = Math.max(0,
                Math.min(this.createdelightcore$pageCount - 1, this.createdelightcore$currentPage + offset));
        if (nextPage != this.createdelightcore$currentPage) {
            this.createdelightcore$currentPage = nextPage;
            createdelightcore$applyPage(false);
        }
    }

    @Unique
    private void createdelightcore$applyPage(boolean initialSetup) {
        int firstEntry = this.createdelightcore$currentPage * CREATEDelightCore$PAGE_SIZE;
        int lastEntry = Math.min(firstEntry + CREATEDelightCore$PAGE_SIZE, this.createdelightcore$pageEntries.size());

        for (int index = 0; index < this.createdelightcore$pageEntries.size(); index++) {
            this.createdelightcore$pageEntries.get(index).setVisible(index >= firstEntry && index < lastEntry);
        }

        if (this.createdelightcore$previousPageButton == null) {
            return;
        }

        boolean hasMultiplePages = this.createdelightcore$pageCount > 1;
        createdelightcore$setControlsVisible(hasMultiplePages);
        this.createdelightcore$previousPageButton.setEnabled(this.createdelightcore$currentPage > 0);
        this.createdelightcore$nextPageButton.setEnabled(
                this.createdelightcore$currentPage + 1 < this.createdelightcore$pageCount);
        this.createdelightcore$pageLabel.setString(
                (this.createdelightcore$currentPage + 1) + " / " + this.createdelightcore$pageCount);

        if (initialSetup && !hasMultiplePages) {
            createdelightcore$setControlsVisible(false);
        }
    }

    @Unique
    private void createdelightcore$setControlsVisible(boolean visible) {
        if (this.createdelightcore$previousPageButton != null) {
            this.createdelightcore$previousPageButton.setVisible(visible);
            this.createdelightcore$nextPageButton.setVisible(visible);
            this.createdelightcore$pageLabel.setVisible(visible);
        }
    }

    @Unique
    private static int createdelightcore$getEntryX(int index) {
        if (index == 0) {
            return 1;
        }

        int group = (index - 1) / 6;
        int remainder = (index - 1) % 6;
        int distance = remainder < 2 ? group * 2 + 1 : group * 2 + 2;
        return (remainder == 1 || remainder >= 4 ? -distance : distance) * 40 + 1;
    }

    @Unique
    private static int createdelightcore$getEntryY(int index) {
        if (index == 0) {
            return -40;
        }
        if (index < 3) {
            return 0;
        }

        int[] pattern = {-1, 1, -1, 1, 0, 0};
        return pattern[(index - 3) % pattern.length] * 40;
    }
}
