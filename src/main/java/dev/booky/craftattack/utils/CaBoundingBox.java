package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (13:27 05.10.22)

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Objects;

public final class CaBoundingBox extends BoundingBox {

    private final WeakReference<World> world;

    public CaBoundingBox(Location corner1, Location corner2) {
        this(corner1.getWorld(), corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(), corner2.getZ());
        assert corner1.getWorld() == corner2.getWorld() : "Worlds mismatch between corners: corner1=" + corner1 + ", corner2=" + corner2;
    }

    public CaBoundingBox(World world, double x1, double y1, double z1, double x2, double y2, double z2) {
        super(x1, y1, z1, x2, y2, z2);
        this.world = new WeakReference<>(world);
    }

    public boolean contains(Location location) {
        if (this.getWorld() != location.getWorld()) {
            return false;
        }
        return super.contains(location.getX(), location.getY(), location.getZ());
    }

    public int getBlockMinX() {
        return NumberConversions.floor(super.getMinX());
    }

    public int getBlockMinY() {
        return NumberConversions.floor(super.getMinY());
    }

    public int getBlockMinZ() {
        return NumberConversions.floor(super.getMinZ());
    }

    public int getBlockMaxX() {
        return NumberConversions.floor(super.getMaxX());
    }

    public int getBlockMaxY() {
        return NumberConversions.floor(super.getMaxY());
    }

    public int getBlockMaxZ() {
        return NumberConversions.floor(super.getMaxZ());
    }

    public Location getMinPos() {
        return new Location(this.getWorld(), super.getMinX(), super.getMinY(), super.getMinZ());
    }

    public Location getMaxPos() {
        return new Location(this.getWorld(), super.getMaxX(), super.getMaxY(), super.getMaxZ());
    }

    public World getWorld() {
        return Objects.requireNonNull(this.world.get(), "World has been unloaded");
    }

    @Override
    public @NotNull CaBoundingBox clone() {
        return (CaBoundingBox) super.clone();
    }

    @Override
    public boolean equals(Object ob) {
        if (this == ob) return true;
        if (!(ob instanceof CaBoundingBox that)) return false;
        if (!super.equals(ob)) return false;
        return this.getWorld().equals(that.getWorld());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.getWorld().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CaBoundingBox{world=" + this.getWorld() + ",parent={" + super.toString() + "}}";
    }
}
