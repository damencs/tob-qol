package com.tobqol.rooms.maiden.commons;

import lombok.Getter;
import lombok.Setter;

public class MaidenRoomInfo
{
    @Getter
    @Setter
    private int startingTick = -1;

    @Getter
    @Setter
    private int totalTime = -1;

    @Getter
    @Setter
    private int time70s = -1;

    @Getter
    @Setter
    private int time50s = -1;

    @Getter
    @Setter
    private int time30s = -1;

    public void reset()
    {
        startingTick = -1;
        totalTime = -1;
        time70s = -1;
        time50s = -1;
        time30s = -1;
    }
}
