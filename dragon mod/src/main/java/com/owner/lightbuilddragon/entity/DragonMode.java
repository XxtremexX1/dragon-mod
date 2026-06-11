package com.owner.lightbuilddragon.entity;

public enum DragonMode {
    FOLLOW("message.lightbuilddragon.mode.follow"),
    WANDER("message.lightbuilddragon.mode.wander");

    private final String translationKey;

    DragonMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public DragonMode next() {
        return this == FOLLOW ? WANDER : FOLLOW;
    }

    public String translationKey() {
        return translationKey;
    }

    public static DragonMode byId(int id) {
        DragonMode[] values = values();
        if (id < 0 || id >= values.length) {
            return FOLLOW;
        }
        return values[id];
    }
}
