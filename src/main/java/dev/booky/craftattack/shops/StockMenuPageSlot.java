package dev.booky.craftattack.shops;

import dev.booky.craftattack.menu.result.MenuClickResult;
import dev.booky.craftattack.utils.PlayerHeadUtil;
import net.kyori.adventure.sound.Sound;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static dev.booky.craftattack.utils.PlayerHeadUtil.WHITE_ARROW_LEFT_TEXTURE;
import static dev.booky.craftattack.utils.PlayerHeadUtil.WHITE_ARROW_RIGHT_TEXTURE;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.TranslationArgument.numeric;

@NullMarked
final class StockMenuPageSlot extends Slot {

    private final StockMenuState state;
    private final int pageOffset;
    private final @Nullable Runnable fallback;

    public StockMenuPageSlot(
            Container container, int slot, StockMenuState state,
            int pageOffset, @Nullable Runnable fallback
    ) {
        if (pageOffset == 0) {
            throw new IllegalArgumentException("Unexpected page offset: 0");
        }
        super(container, slot, 0, 0);
        this.state = state;
        this.pageOffset = pageOffset;
        this.fallback = fallback;
    }

    public void trigger(@Nullable Player player) {
        int newPage = this.state.getPage() + this.pageOffset;
        if (newPage < 0 || newPage > this.state.getPageCount()) {
            if (this.fallback != null) {
                this.fallback.run();
            }
        } else {
            this.state.setPage(newPage);
            this.container.setChanged();
        }
        if (player != null) {
            // manually play click sound
            Sound sound = MenuClickResult.SOUND.getSound();
            assert sound != null;
            player.getBukkitEntity().playSound(sound, Sound.Emitter.self());
        }
    }

    @Override
    public Optional<ItemStack> tryRemove(int amount, int maxAmount, Player player) {
        this.trigger(player);
        return Optional.empty();
    }

    @Override
    public ItemStack safeTake(int amount, int maxAmount, Player player) {
        this.trigger(player);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasItem() {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return false;
    }

    @Override
    public ItemStack getItem() {
        int newPage = this.state.getPage() + this.pageOffset;
        boolean oob = newPage < 0 || newPage > this.state.getPageCount();
        if (oob && this.fallback == null) {
            return ItemStack.EMPTY;
        }
        org.bukkit.inventory.ItemStack bukkitStack = this.pageOffset < 0
                ? PlayerHeadUtil.createHeadStack(WHITE_ARROW_LEFT_TEXTURE,
                translatable(oob ? "ca.menu.back" : "ca.menu.previous", numeric(newPage)))
                : PlayerHeadUtil.createHeadStack(WHITE_ARROW_RIGHT_TEXTURE,
                translatable("ca.menu.next", numeric(newPage)));
        return CraftItemStack.unwrap(bukkitStack);
    }

    @Override
    public void set(ItemStack itemStack) {
    }

    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public void onTake(Player player, ItemStack carried) {
        this.trigger(player);
    }
}
