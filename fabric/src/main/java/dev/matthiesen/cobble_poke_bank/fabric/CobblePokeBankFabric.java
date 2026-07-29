package dev.matthiesen.cobble_poke_bank.fabric;

import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import net.fabricmc.api.ModInitializer;

public class CobblePokeBankFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        var instance = CobblePokeBankCommon.INSTANCE;
        instance.createInfoLog("Loading for Fabric Mod Loader");
        instance.initialize();
    }
}
