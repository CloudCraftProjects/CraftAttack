package dev.booky.craftattack.menu;
// Created by booky10 in CraftAttack (00:09 27.10.2025)

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class MenuResult {

    public static final MenuResult NONE = new MenuResult(null, false);
    public static final MenuResult SOUND = new MenuResult(
            Sound.sound().type(Key.key("block.note_block.hat")).build(), false);
    public static final MenuResult CLOSE = new MenuResult(null, true);

    protected final @Nullable Sound sound;
    protected final boolean close;

    public MenuResult(@Nullable Sound sound, boolean close) {
        this.sound = sound;
        this.close = close;
    }

    public MenuResult plus(MenuResult other) {
        Sound sound;
        if (this.sound != null) {
            // in the case of both sounds not being empty, we can't properly combine this;
            // choose to just play one sound instead of throwing errors
            sound = this.sound;
        } else if (other.sound != null) {
            sound = other.sound;
        } else {
            sound = null; // no sound
        }
        return new MenuResult(sound, this.close || other.close);
    }

    public @Nullable Sound getSound() {
        return this.sound;
    }

    public boolean isClose() {
        return this.close;
    }
}
