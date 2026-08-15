package com.leo.stonereverser;

import com.leo.stonereverser.init.ModBlocks;
import com.leo.stonereverser.init.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(StoneReverserMod.MOD_ID)
public class StoneReverserMod {
    public static final String MOD_ID = "stonereverser";

    public StoneReverserMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
    }
}