package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (21:22 13.11.22)

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class ProtectedArea {

    private final CaBoundingBox box;
    private final Set<ProtectionFlag> flags;

    public ProtectedArea(CaBoundingBox box) {
        this(box, ProtectionFlag.values());
    }

    public ProtectedArea(CaBoundingBox box, ProtectionFlag... flags) {
        this.box = box.clone();
        this.flags = Arrays.stream(flags).collect(Collectors.toSet());
    }

    public boolean addFlag(ProtectionFlag flag) {
        return this.flags.add(flag);
    }

    public boolean removeFlag(ProtectionFlag flag) {
        return this.flags.remove(flag);
    }

    public boolean hasFlag(ProtectionFlag flag) {
        return this.flags.contains(flag);
    }

    public CaBoundingBox getBox() {
        return this.box.clone();
    }

    public Set<ProtectionFlag> getFlags() {
        return Collections.unmodifiableSet(this.flags);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ProtectedArea that)) return false;
        return this.box.equals(that.box);
    }

    @Override
    public int hashCode() {
        return this.box.hashCode();
    }

    @Override
    public String toString() {
        return "ProtectedArea{box=" + this.box + ", flags=" + this.flags + '}';
    }
}
