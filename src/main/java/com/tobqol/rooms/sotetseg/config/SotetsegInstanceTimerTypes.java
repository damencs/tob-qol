package com.tobqol.rooms.sotetseg.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SotetsegInstanceTimerTypes
{
	OFF("Off"),
	ENTRANCE("Entrance"),
	MAZE("Maze"),
	BOTH("Both");

	private final String option;

	public final boolean showOnlyForEntrance()
	{
		return this == ENTRANCE;
	}

	public final boolean showOnlyForMaze()
	{
		return this == MAZE;
	}

	public final boolean showForAll()
	{
		return this == BOTH;
	}

	@Override
	public String toString()
	{
		return option;
	}
}
