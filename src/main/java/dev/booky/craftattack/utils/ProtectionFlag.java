package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (21:31 13.11.22)

import net.kyori.adventure.text.Component;

import java.util.Locale;

public enum ProtectionFlag {

    INTERACT,
    BUILDING,
    HEALTH,
    HUNGER,
    EXPLOSION,
    FIRE,
    MOB_SPAWNING,
    MOB_AI,
    REDSTONE;

    private final String i18nKey;

    ProtectionFlag() {
        String lcName = super.name().toLowerCase(Locale.ROOT);
        this.i18nKey = "ca.protection-flag." + lcName.replace('_', '-');
    }

    public Component getTranslation() {
        return Component.translatable(this.i18nKey);
    }
}
