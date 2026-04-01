package com.fix3dll.skyblockaddons.core;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @EqualsAndHashCode
public class PetInfo {

    @SerializedName("type")
    private String petSkyblockId;
    @SerializedName("active")
    private boolean active;
    @SerializedName("exp")
    private double exp;
    @SerializedName("tier")
    private SkyblockRarity petRarity;
    @SerializedName("hideInfo")
    private boolean hideInfo;
    @SerializedName("heldItem")
    @Setter
    private String heldItemId;
    @SerializedName("candyUsed")
    private int candyUsed;
    @SerializedName("skin")
    private String skin;
    @SerializedName("uuid")
    private UUID uuid;
    @SerializedName("uniqueId")
    private UUID uniqueId;
    @SerializedName("hideRightClick")
    private boolean hideRightClick;
    @SerializedName("noMove")
    private boolean noMove;
    @SerializedName("petSoulbound")
    private boolean petSoulbound;

}