package com.hotel.model.enums;

public enum MemberLevel {
    NONE("非会员"),
    BRONZE("铜牌会员"),
    SILVER("银牌会员"),
    GOLD("金牌会员"),
    PLATINUM("白金会员");

    private final String description;

    MemberLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}