package com.leo.stonereverser.client;

import com.leo.stonereverser.StoneReverserMod;
import com.leo.stonereverser.client.gui.StoneReverserScreen;
import com.leo.stonereverser.init.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = StoneReverserMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.STONE_REVERSER.get(), StoneReverserScreen::new);
    }
}