package com.leo.stonereverser.init;

import com.leo.stonereverser.StoneReverserMod;
import com.leo.stonereverser.block.StoneReverserBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(StoneReverserMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StoneReverserMod.MOD_ID);

    public static final DeferredBlock<Block> STONE_REVERSER = BLOCKS.register("stone_reverser",
            () -> new StoneReverserBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONECUTTER)));

    public static final DeferredItem<?> STONE_REVERSER_ITEM = ITEMS.registerSimpleBlockItem("stone_reverser", STONE_REVERSER);
}