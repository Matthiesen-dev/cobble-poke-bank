package dev.matthiesen.cobble_poke_bank.common;

import dev.matthiesen.libs.faststats.Token;
import dev.matthiesen.matthiesen_core.common.AbstractCommonMod;
import org.jetbrains.annotations.NotNull;

public final class CobblePokeBankCommon extends AbstractCommonMod {
    public static final String MOD_ID = "cobble_poke_bank";
    public static final String MOD_NAME = "Cobble Poke Bank";
    public static @Token final String METRICS_TOKEN = "";
    public static final CobblePokeBankCommon INSTANCE = new CobblePokeBankCommon();

    public CobblePokeBankCommon() {
        super(MOD_ID, MOD_NAME);
    }

    @Override
    public @Token @NotNull String getMetricsToken() {
        return METRICS_TOKEN;
    }

    public void initialize() {
        super.initialize();

       if (getCommonUtils().isModLoaded("cobblemon")) {
            createInfoLog("Cobblemon is loaded, Hello there Cobblemon!");
       }

        createInfoLog("Initialized");
    }
}
