package dev.booky.craftattack.menu.result;
// Created by booky10 in CraftAttack (00:15 27.10.2025)

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@NullMarked
public final class MenuClickResult extends MenuResult {

    public static final MenuClickResult NONE = new MenuClickResult(null, false, false);
    public static final MenuClickResult SOUND = new MenuClickResult(
            Sound.sound().type(Key.key("block.note_block.hat")).build(), false, false);
    public static final MenuClickResult CLOSE = new MenuClickResult(null, true, false);
    public static final MenuClickResult CLOSE_SOUND = SOUND.plus(CLOSE);
    public static final MenuClickResult ALLOW = new MenuClickResult(null, false, true);

    private final boolean allow;

    public MenuClickResult(@Nullable Sound sound) {
        this(sound, false, false);
    }

    public MenuClickResult(@Nullable Sound sound, boolean close, boolean allow) {
        super(sound, close);
        this.allow = allow && !close; // don't close and allow taking/placing items
    }

    public MenuClickResult plus(MenuClickResult other) {
        if (this.equals(other)) {
            return this;
        }
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
        return new MenuClickResult(sound, this.close || other.close, this.allow || other.allow);
    }

    public boolean isAllow() {
        return this.allow;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) return false;
        MenuClickResult that = (MenuClickResult) obj;
        if (this.close != that.close) return false;
        if (this.allow != that.allow) return false;
        return Objects.equals(this.sound, that.sound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.sound, this.close, this.allow);
    }
}
