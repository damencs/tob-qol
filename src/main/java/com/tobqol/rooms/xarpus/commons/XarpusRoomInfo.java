package com.tobqol.rooms.xarpus.commons;

import lombok.Getter;
import lombok.Setter;

public class XarpusRoomInfo
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
    private int p2Time = -1;

    @Getter
    @Setter
    private int p3Time = -1;

    public void reset()
    {
        startingTick = -1;
        totalTime = -1;
        p1Time = -1;
        p2Time = -1;
        p3Time = -1;
    }
}
