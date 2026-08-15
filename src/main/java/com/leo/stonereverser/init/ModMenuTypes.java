package com.leo.stonereverser.init;

import com.leo.stonereverser.StoneReverserMod;
import com.leo.stonereverser.menu.StoneReverserMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, StoneReverserMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<StoneReverserMenu>> STONE_REVERSER = MENUS.register("stone_reverser",
            () -> new MenuType<>(StoneReverserMenu::new, FeatureFlags.VANILLA_SET));
}