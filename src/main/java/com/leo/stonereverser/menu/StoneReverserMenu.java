package com.leo.stonereverser.menu;

import com.leo.stonereverser.init.ModBlocks;
import com.leo.stonereverser.init.ModMenuTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoneReverserMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final DataSlot selectedRecipeIndex = DataSlot.standalone();
    private final Level level;
    private List<ReverseRecipeInfo> reverseRecipes = new ArrayList<>();
    private ItemStack inputItem = ItemStack.EMPTY;
    private long lastSoundTime;
    final Slot inputSlot;
    final Slot resultSlot;
    Runnable slotUpdateListener = () -> { };
    public final Container container;

    public record ReverseRecipeInfo(ItemStack result, int inputCost) { }

    public StoneReverserMenu(int id, Inventory inv) {
        this(id, inv, ContainerLevelAccess.NULL);
    }

    public StoneReverserMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(ModMenuTypes.STONE_REVERSER.get(), id);
        this.access = access;
        this.level = inv.player.level();
        this.container = new SimpleContainer(2) {
            public void setChanged() {
                super.setChanged();
                StoneReverserMenu.this.slotsChanged(this);
            }
        };

        this.inputSlot = this.addSlot(new Slot(this.container, 0, 20, 33));
        this.resultSlot = this.addSlot(new Slot(this.container, 1, 143, 33) {
            public boolean mayPlace(ItemStack stack) { return false; }

            public void onTake(Player player, ItemStack stack) {
                stack.onCraftedBy(player.level(), player, stack.getCount());
                StoneReverserMenu.this.access.execute((lvl, pos) -> {
                    long gameTime = lvl.getGameTime();
                    if (StoneReverserMenu.this.lastSoundTime != gameTime) {
                        StoneReverserMenu.this.lastSoundTime = gameTime;
                        lvl.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                });
                int cost = StoneReverserMenu.this.getSelectedRecipeCost();
                ItemStack input = StoneReverserMenu.this.inputSlot.getItem();
                input.shrink(cost);
                StoneReverserMenu.this.inputSlot.set(input);
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inv, k, 8 + k * 18, 142));
        }

        this.addDataSlot(this.selectedRecipeIndex);
    }

    public int getSelectedRecipeIndex() { return this.selectedRecipeIndex.get(); }
    public List<ReverseRecipeInfo> getRecipes() { return this.reverseRecipes; }
    public int getNumRecipes() { return this.reverseRecipes.size(); }
    public boolean hasInputItem() { return !this.inputSlot.getItem().isEmpty() && !this.reverseRecipes.isEmpty(); }
    public boolean isValidRecipeIndex(int index) { return index >= 0 && index < this.reverseRecipes.size(); }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.STONE_REVERSER.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int index) {
        if (this.isValidRecipeIndex(index)) {
            this.selectedRecipeIndex.set(index);
            this.setSelectedRecipe(index);
        }
        return true;
    }

    private void setSelectedRecipe(int index) {
        if (this.isValidRecipeIndex(index)) {
            ReverseRecipeInfo info = this.reverseRecipes.get(index);
            this.resultSlot.set(info.result().copy());
        } else {
            this.resultSlot.set(ItemStack.EMPTY);
        }
        this.broadcastChanges();
    }

    @Override
    public void slotsChanged(Container container) {
        ItemStack input = this.inputSlot.getItem();
        if (input.isEmpty()) {
            this.inputItem = ItemStack.EMPTY;
            this.reverseRecipes.clear();
            this.selectedRecipeIndex.set(-1);
            this.resultSlot.set(ItemStack.EMPTY);
        } else if (!ItemStack.isSameItemSameComponents(input, this.inputItem)) {
            this.inputItem = input.copy();
            this.updateReverseRecipes(input);
        } else {
            if (this.selectedRecipeIndex.get() >= 0) {
                int cost = this.getSelectedRecipeCost();
                if (input.getCount() < cost) {
                    this.resultSlot.set(ItemStack.EMPTY);
                } else {
                    this.setSelectedRecipe(this.selectedRecipeIndex.get());
                }
            }
        }
        this.slotUpdateListener.run();
    }

    private void updateReverseRecipes(ItemStack input) {
        this.reverseRecipes.clear();
        this.selectedRecipeIndex.set(-1);
        this.resultSlot.set(ItemStack.EMPTY);
        if (input.isEmpty()) return;

        RecipeManager manager = this.level.getRecipeManager();
        var allRecipes = manager.getAllRecipesFor(RecipeType.STONECUTTING);
        Set<Item> seen = new HashSet<>();

        for (var holder : allRecipes) {
            SingleItemRecipe recipe = holder.value();
            ItemStack result = recipe.getResultItem(this.level.registryAccess());
            if (result.getItem() == input.getItem()) {
                Ingredient ingredient = recipe.getIngredients().get(0);
                for (ItemStack item : ingredient.getItems()) {
                    if (item.isEmpty()) continue;
                    if (seen.add(item.getItem())) {
                        ItemStack out = item.copy();
                        out.setCount(1);
                        this.reverseRecipes.add(new ReverseRecipeInfo(out, result.getCount()));
                    }
                }
            }
        }

        if (!this.reverseRecipes.isEmpty() && input.getCount() >= this.reverseRecipes.get(0).inputCost()) {
            this.selectedRecipeIndex.set(0);
            this.setSelectedRecipe(0);
        }
        this.slotUpdateListener.run();
    }

    public int getSelectedRecipeCost() {
        if (this.isValidRecipeIndex(this.selectedRecipeIndex.get())) {
            return this.reverseRecipes.get(this.selectedRecipeIndex.get()).inputCost();
        }
        return 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();
            if (index == 1) {
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(slotStack, stack);
            } else if (index == 0) {
                if (!this.moveItemStackTo(slotStack, 2, 38, false)) return ItemStack.EMPTY;
            } else if (this.moveItemStackTo(slotStack, 0, 1, false)) {
                // moved to input
            } else if (index >= 2 && index < 29) {
                if (!this.moveItemStackTo(slotStack, 29, 38, false)) return ItemStack.EMPTY;
            } else if (index >= 29 && index < 38) {
                if (!this.moveItemStackTo(slotStack, 2, 29, false)) return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();

            if (slotStack.getCount() == stack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, slotStack);
        }
        return stack;
    }

    public void registerUpdateListener(Runnable listener) {
        this.slotUpdateListener = listener;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((lvl, pos) -> this.clearContainer(player, this.container));
    }
}