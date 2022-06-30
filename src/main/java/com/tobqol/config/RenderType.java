package com.tobqol.config;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RenderType
{
	OFF("Off"),
	TILE("Tile"),
	HULL("Hull");

	private final String name;

	public boolean isOff()
	{
		return this == OFF;
	}

	public boolean isTile()
	{
		return this == TILE;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
