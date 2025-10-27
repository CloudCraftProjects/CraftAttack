package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (00:53 27.10.2025)

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.stream.JsonWriter;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@NullMarked
public final class PlayerHeadUtil {

    public static final String MINECRAFT_TEXTURE_BASE_URL = "http://textures.minecraft.net/texture/";
    public static final String WHITE_ARROW_LEFT_TEXTURE = MINECRAFT_TEXTURE_BASE_URL + "cdc9e4dcfa4221a1fadc1b5b2b11d8beeb57879af1c42362142bae1edd5";

    // use a common profile owner id to prevent clients from resolving textures for this uuid over and over
    public static final UUID STATIC_PROFILE_UUID = UUID.fromString("853c80ef-3c37-39fd-aa49-938b674adae6");
    public static final String PROFILE_PROPERTY_TEXTURES = "textures";

    private PlayerHeadUtil() {
    }

    public static String encodeTextureUrl(String textureUrl) {
        String json;
        try (StringWriter stringWriter = new StringWriter()) {
            try (JsonWriter writer = new JsonWriter(stringWriter)) {
                writer.beginObject()
                        .name("textures").beginObject()
                        .name("SKIN").beginObject()
                        .name("url").value(textureUrl)
                        .endObject().endObject().endObject();
            }
            stringWriter.flush();
            json = stringWriter.toString();
        } catch (IOException exception) {
            throw new RuntimeException("Error while encoding texture url " + textureUrl + " to json", exception);
        }
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public static PlayerProfile createProfile(String textureUrl) {
        PlayerProfile profile = Bukkit.createProfile(STATIC_PROFILE_UUID).clone();
        profile.clearProperties();
        profile.setProperty(createProfileProperty(textureUrl));
        return profile;
    }

    public static ProfileProperty createProfileProperty(String textureUrl) {
        String encodedTextureUrl = encodeTextureUrl(textureUrl);
        return new ProfileProperty(PROFILE_PROPERTY_TEXTURES, encodedTextureUrl);
    }

    public static ItemStack createHeadStack(String textureUrl) {
        ItemStack stack = ItemStack.of(Material.PLAYER_HEAD);
        stack.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile()
                .uuid(STATIC_PROFILE_UUID)
                .addProperty(createProfileProperty(textureUrl))
                .build());
        return stack;
    }
}
