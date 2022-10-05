package dev.booky.craftattack.utils;
// Created by booky10 in CraftAttack (18:56 05.10.22)

public enum TpResult {

    CANCELLED(true),
    DISCONNECTED(false),
    SUCCESSFUL(false),
    ALREADY_TELEPORTING(true);

    private final boolean sentMessage;

    TpResult(boolean sentMessage) {
        this.sentMessage = sentMessage;
    }

    public boolean isSentMessage() {
        return sentMessage;
    }
}
