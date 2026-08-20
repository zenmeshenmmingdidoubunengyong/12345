package com.leo.stonereverser.client.gui;

import com.leo.stonereverser.menu.StoneReverserMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class StoneReverserScreen extends AbstractContainerScreen<StoneReverserMenu> {
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller_disabled");
    private static final ResourceLocation RECIPE_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_selected");
    private static final ResourceLocation RECIPE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe_highlighted");
    private static final ResourceLocation RECIPE_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/recipe");
    private static final ResourceLocation BG_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/stonecutter.png");

    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private boolean displayRecipes;

    public StoneReverserScreen(StoneReverserMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        menu.registerUpdateListener(this::containerChanged);
        --this.titleLabelY;
    }

    public void containerChanged() {
        this.displayRecipes = this.menu.hasInputItem();
        if (!this.displayRecipes) {
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
        }
    }

    @Override
    protected void init() {
        super.init();
        // 确保 GUI 尺寸与 stonecutter.png (176x166) 一致，防止物品/背景偏移
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.containerChanged();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BG_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        int k = (int)(41.0F * this.scrollOffs);
        ResourceLocation sprite;
        if (this.displayRecipes) {
            sprite = k > 0 ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        } else {
            sprite = SCROLLER_DISABLED_SPRITE;
        }
        guiGraphics.blitSprite(sprite, this.leftPos + 119, this.topPos + 15 + k, 12, 15);
        int end = this.startIndex + 12;
        this.renderButtons(guiGraphics, mouseX, mouseY, this.startIndex, end);
        this.renderRecipes(guiGraphics, this.leftPos + 52, this.topPos + 14, this.startIndex, end);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        if (this.displayRecipes) {
            int i = this.leftPos + 52;
            int j = this.topPos + 14;
            int k = this.startIndex + 12;
            for (int l = this.startIndex; l < k; ++l) {
                int idx = l - this.startIndex;
                double d0 = mouseX - (double)(i + idx % 4 * 16);
                double d1 = mouseY - (double)(j + idx / 4 * 18);
                if (d0 >= 0.0 && d1 >= 0.0 && d0 < 16.0 && d1 < 18.0 && this.menu.clickMenuButton(this.minecraft.player, l)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, l);
                    return true;
                }
            }
            i = this.leftPos + 119;
            j = this.topPos + 9;
            if (mouseX >= (double)i && mouseX < (double)(i + 12) && mouseY >= (double)j && mouseY < (double)(j + 54)) {
                this.scrolling = true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling && this.displayRecipes) {
            int i = this.topPos + 14;
            int j = i + 54;
            this.scrollOffs = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)this.getOffscreenRows()) + 0.5) * 4;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.displayRecipes) {
            int rows = this.getOffscreenRows();
            float f = (float)scrollY / (float)rows;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)rows) + 0.5) * 4;
        }
        return true;
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, int start, int end) {
        for (int i = start; i < end && i < this.menu.getNumRecipes(); ++i) {
            int j = i - start;
            int k = j % 4;
            int l = j / 4;
            int x = this.leftPos + 52 + k * 16;
            int y = this.topPos + 14 + l * 18;
            int state = 0;
            if (i == this.menu.getSelectedRecipeIndex()) state = 1;
            else if (mouseX >= x && mouseY >= y && mouseX < x + 16 && mouseY < y + 18) state = 2;
            ResourceLocation sprite = switch (state) {
                case 1 -> RECIPE_SELECTED_SPRITE;
                case 2 -> RECIPE_HIGHLIGHTED_SPRITE;
                default -> RECIPE_SPRITE;
            };
            guiGraphics.blitSprite(sprite, x, y - 1, 16, 18);
        }
    }

    private void renderRecipes(GuiGraphics guiGraphics, int x, int y, int start, int end) {
        List<StoneReverserMenu.ReverseRecipeInfo> list = this.menu.getRecipes();
        for (int i = start; i < end && i < this.menu.getNumRecipes(); ++i) {
            int j = i - start;
            int k = j % 4;
            int l = j / 4;
            guiGraphics.renderItem(list.get(i).result(), x + k * 16 + 2, y + l * 18 + 2);
        }
    }

    private int getOffscreenRows() {
        return (this.menu.getNumRecipes() + 4 - 1) / 4 - 3;
    }
}