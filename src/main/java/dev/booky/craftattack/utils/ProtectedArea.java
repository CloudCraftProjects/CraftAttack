package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (21:22 13.11.22)

public class ProtectedArea {

    private final CaBoundingBox box;

    public ProtectedArea(CaBoundingBox box) {
        this.box = box.clone();
    }

    public CaBoundingBox getBox() {
        return box.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ProtectedArea that)) return false;
        return box.equals(that.box);
    }

    @Override
    public int hashCode() {
        return box.hashCode();
    }

    @Override
    public String toString() {
        return "ProtectedArea{box=" + box + '}';
    }
}
