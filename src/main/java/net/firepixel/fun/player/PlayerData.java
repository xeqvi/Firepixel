package net.firepixel.fun.player;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private String name;
    private long firstJoin;
    private long lastJoin;
    private String language;
    private String rank;

    public PlayerData(UUID uuid, String name, long firstJoin, long lastJoin, String language, String rank) {
        this.uuid = uuid;
        this.name = name;
        this.firstJoin = firstJoin;
        this.lastJoin = lastJoin;
        this.language = language;
        this.rank = rank;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFirstJoin() {
        return firstJoin;
    }

    public void setFirstJoin(long firstJoin) {
        this.firstJoin = firstJoin;
    }

    public long getLastJoin() {
        return lastJoin;
    }

    public void setLastJoin(long lastJoin) {
        this.lastJoin = lastJoin;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRank() {
        return rank == null || rank.trim().isEmpty() ? "default" : rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }
}