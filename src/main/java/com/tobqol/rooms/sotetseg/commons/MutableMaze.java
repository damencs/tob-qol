package com.tobqol.rooms.sotetseg.commons;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import net.runelite.api.Point;

import com.tobqol.api.game.Instance;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class MutableMaze
{
	private static final int xOffset_overworld = 9, yOffset_overworld = 22;
	private static final int xOffset_underworld = 42, yOffset_underworld = 31;

	private final Instance instance;
	private final List<Point> points = new ArrayList();

	@Getter
	private boolean inUnderworld = false;

	@Getter
	private boolean underworldTiles = false;

	public void addPoint(int x, int y)
	{
		Point point = new Point(x - xOffset_overworld, y - yOffset_overworld);

		if (points.contains(point))
		{
			return;
		}

		if (instance.getCurrentRegion().isSotetsegOverworld())
		{
			points.add(new Point(x - xOffset_overworld, y - yOffset_overworld));
		}
		else
		{
			points.add(new Point(x - xOffset_underworld, y - yOffset_underworld));
		}
	}
}
