package com.leo.stonereverser.init;

import com.leo.stonereverser.StoneReverserMod;
import com.leo.stonereverser.block.StoneReverserBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(StoneReverserMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StoneReverserMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StoneReverserMod.MOD_ID);

    // 挖掘属性与原版石切机一致：需要类工具才能掉落、挖掘等级 wood（任何镐都能挖）。
    // ofFullCopy(Blocks.STONECUTTER) 已经继承这些属性，但显式再设一次以防 Create 等修改。
    public static final DeferredBlock<Block> STONE_REVERSER = BLOCKS.register("stone_reverser",
            () -> new StoneReverserBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONECUTTER)
                    .requiresCorrectToolForDrops()));

    public static final DeferredItem<?> STONE_REVERSER_ITEM = ITEMS.registerSimpleBlockItem("stone_reverser", STONE_REVERSER);

    // 让方块在创造物品栏可见（此前搜索不到的主要原因之一）
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.stonereverser"))
                    .icon(() -> new ItemStack(STONE_REVERSER.get()))
                    .displayItems((params, output) -> output.accept(STONE_REVERSER.get()))
                    .build());
}
