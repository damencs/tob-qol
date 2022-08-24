package com.tobqol.api.game;

import com.google.common.collect.ImmutableSet;

import java.text.DecimalFormat;
import java.util.Set;

public interface RaidConstants
{
    DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##0.0");

    int PRECISE_TIMER = 11866;
    int THEATRE_OF_BLOOD_ROOM_STATUS = 6447;
    int THEATRE_OF_BLOOD_BOSS_HP = 6448;
    int TOB_CHEST_UNLOOTED = 41435;
    int TOB_CHEST_LOOTED = 41436;
    int TOB_BANK_CHEST = 41437;
    int TOB_ENTRANCE = 32653;

    Set<Integer> VER_SINHAZA_REGIONS = ImmutableSet.of(
            14386,
            14642
    );

    Set<String> TOB_CHEST_TARGETS = ImmutableSet.of(
            "Stamina potion(4)",
            "Prayer potion(4)",
            "Saradomin brew(4)",
            "Super restore(4)",
            "Mushroom potato",
            "Shark",
            "Sea turtle",
            "Manta ray"
    );
}
