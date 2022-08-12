package com.tobqol.rooms.nylocas.commons;

import lombok.Getter;
import lombok.Setter;

public class NylocasRoomInfo
{
    @Getter
    @Setter
    private int startingTick = -1;

    @Getter
    @Setter
    private int totalTime = -1;

    @Getter
    @Setter
    private int wave = 0;

    @Getter
    @Setter
    private boolean wavesFinished = false;

    @Getter
    @Setter
    private boolean cleanupfinished = false;

    @Getter
    @Setter
    private boolean waveThisTick = false;

    @Getter
    @Setter
    private int waveTime = -1;

    @Getter
    @Setter
    private int cleanupTime = -1;

    @Getter
    @Setter
    private int bossSpawnTime = -1;

    public void reset()
    {
        startingTick = -1;
        totalTime = -1;
        wave = 0;
        wavesFinished = false;
        cleanupfinished = false;
        waveThisTick = false;
        waveTime = -1;
        cleanupTime = -1;
        bossSpawnTime = -1;
    }
}
