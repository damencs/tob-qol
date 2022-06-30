package com.tobqol.api.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import java.awt.*;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PerspectiveUtil extends Perspective
{
	public static Optional<Polygon> toTilePoly(Client client, WorldPoint worldPoint)
	{
		if (client == null || worldPoint == null)
		{
			return Optional.empty();
		}

		LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
		if (localPoint == null)
		{
			return Optional.empty();
		}

		Polygon tile = getCanvasTilePoly(client, localPoint);
		return Optional.ofNullable(tile);
	}
}
