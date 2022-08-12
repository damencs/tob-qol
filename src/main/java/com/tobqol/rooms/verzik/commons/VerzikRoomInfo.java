package com.tobqol.rooms.verzik.commons;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public class VerzikRoomInfo
{
    @Getter
    @Setter
    private int startingTick = -1;

    @Getter
    @Setter
    private int totalTime = -1;

    @Getter
    @Setter
    private int p1Time = -1;

    @Getter
    @Setter
    private int redsSpawn = -1;

    @Getter
    @Setter
    private int p2Time = -1;

    @Getter
    @Setter
    private HashMap<Integer, Integer> webTimes = new HashMap<>();

    @Getter
    @Setter
    private HashMap<Integer, Integer> crabTimes = new HashMap<>();

    @Getter
    @Setter
    private HashMap<Integer, Integer> yellowTimes = new HashMap<>();

    @Getter
    @Setter
    private HashMap<Integer, Integer> greenBallTimes = new HashMap<>();

    @Getter
    @Setter
    private int p3Time = -1;

    public void reset()
    {
        startingTick = -1;
        totalTime = -1;
        p1Time = -1;
        redsSpawn = -1;
        p2Time = -1;
        webTimes.clear();
        crabTimes.clear();
        yellowTimes.clear();
        greenBallTimes.clear();
    }
}
