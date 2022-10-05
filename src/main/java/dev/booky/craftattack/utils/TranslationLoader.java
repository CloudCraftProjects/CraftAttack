package dev.booky.craftattack.utils;
// Created by booky10 in StoneAttack (14:48 28.07.22)

import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.Locale;
import java.util.ResourceBundle;

public class TranslationLoader {

    private final Plugin plugin;
    private Translator translator;

    public TranslationLoader(Plugin plugin) {
        this.plugin = plugin;
    }

    public void unload() {
        if (this.translator != null) {
            GlobalTranslator.translator().removeSource(this.translator);
        }
    }

    public void load() {
        this.unload();

        TranslationRegistry registry = TranslationRegistry.create(new NamespacedKey(this.plugin, "i18n"));
        registry.defaultLocale(Locale.ENGLISH);

        registerBundle(registry, Locale.ENGLISH);
        registerBundle(registry, Locale.GERMAN);

        GlobalTranslator.translator().addSource(registry);
        this.translator = registry;
    }

    private void registerBundle(TranslationRegistry registry, Locale locale) {
        String baseName = this.plugin.getDescription().getName().toLowerCase(Locale.ROOT);
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, UTF8ResourceBundleControl.get());
        registry.registerAll(locale, bundle, false);
    }
}
