package com.tobqol.rooms.sotetseg.commons;

import lombok.Getter;
import lombok.Setter;

public class SotetsegRoomInfo
{
    @Getter
    @Setter
    private int startingTick = -1;

    @Getter
    @Setter
    private int totalTime = -1;

    @Getter
    @Setter
    private int firstMaze = -1;

    @Getter
    @Setter
    private int secondMaze = -1;

    public void reset()
    {
        startingTick = -1;
        totalTime = -1;
        firstMaze = -1;
        secondMaze = -1;
    }
}
