package dev.matthiesen.cobble_poke_bank.neoforge;

import dev.matthiesen.cobble_poke_bank.common.CobblePokeBankCommon;
import net.neoforged.fml.common.Mod;

@Mod(CobblePokeBankCommon.MOD_ID)
public class CobblePokeBankNeoForge {
    public static final CobblePokeBankCommon INSTANCE = CobblePokeBankCommon.INSTANCE;

    public CobblePokeBankNeoForge() {
        INSTANCE.createInfoLog("Loading for NeoForge Mod Loader");
        INSTANCE.initialize();
    }
}
