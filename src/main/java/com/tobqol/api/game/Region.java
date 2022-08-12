/*
 * Copyright (c) 2022, Damen <gh: damencs>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.

 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tobqol.api.game;

import com.google.common.collect.ImmutableMap;
import com.tobqol.rooms.bloat.commons.BloatTable;
import com.tobqol.rooms.maiden.commons.MaidenTable;
import com.tobqol.rooms.nylocas.commons.NylocasConstants;
import com.tobqol.rooms.sotetseg.commons.SotetsegTable;
import com.tobqol.rooms.verzik.commons.VerzikMap;
import com.tobqol.rooms.xarpus.commons.XarpusTable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

import javax.annotation.Nullable;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum Region
{
	LOBBY("Lobby", 12869, 0, 400, 552),
	MAIDEN(MaidenTable.BOSS_NAME_SIMPLE, 12613, 0, 392, 552, new WorldArea(3175, 4421, 4, 1, 0)),
	BLOAT(BloatTable.BOSS_NAME_SIMPLE, 13125, 0, 408, 552, new WorldArea(3268, 4446, 1, 4, 0)),
	NYLOCAS(NylocasConstants.BOSS_NAME, 13122, 0, 408, 528, new WorldArea(3304, 4274, 1, 4, 0)),
	SOTETSEG(SotetsegTable.BOSS_NAME, 13123, 0, 408, 536, new WorldArea(3278, 4292, 4, 1, 0)),
	SOTETSEG_MAZE(SotetsegTable.BOSS_NAME, 13379, 3, 416, 536),
	XARPUS(XarpusTable.BOSS_NAME, 12612, 1, 392, 544, new WorldArea(3169, 4400, 3, 1, 1)),
	VERZIK(VerzikMap.BOSS_NAME, 12611, 0, 392, 536),
	LOOT_ROOM("Loot Room", 12867, 0, 400, 536),
	UNKNOWN("Unknown");

	private final String prettyName;
	private final int regionId;
	private final int zone;
	private final @Nullable WorldArea imaginaryLine;

	private static final Map<Integer, Region> lookupMap;

	static
	{
		ImmutableMap.Builder<Integer, Region> builder = ImmutableMap.builder();

		for (Region region : values())
		{
			builder.put(region.regionId, region);
		}

		lookupMap = builder.build();
	}

	public static Region of(int regionId)
	{
		return lookupMap.getOrDefault(regionId, UNKNOWN);
	}

	public static boolean inRegion(Client client, Region region)
	{
		return getCurrentRegionID(client, client.getLocalPlayer()) == region.regionId;
	}

	public static int getCurrentRegionID(Client client, Player player)
	{
		if (!client.isInInstancedRegion() || player == null)
		{
			return -1;
		}

		WorldPoint location = WorldPoint.fromLocalInstance(client, player.getLocalLocation());

		if (location == null)
		{
			return -1;
		}

		return location.getRegionID();
	}

	Region(String prettyName, int regionID, int p, int x, int y, WorldArea imaginaryLine)
	{
		this(prettyName, regionID, (p << 24 | x << 14 | y << 3), imaginaryLine);
	}

	Region(String prettyName, int regionID, int p, int x, int y)
	{
		this(prettyName, regionID, p, x, y, null);
	}

	Region(String prettyName)
	{
		this(prettyName, -1, -1, null);
	}

	public Region next()
	{
		if (this == SOTETSEG)
		{
			return XARPUS;
		}

		if (this == UNKNOWN)
		{
			return this;
		}

		Region[] regions = values();
		int index = ordinal() + 1;
		return index >= regions.length ? UNKNOWN : regions[index];
	}

	public boolean isPCIL(Client client, Player player)
	{
		if (!client.isInInstancedRegion() || player == null || imaginaryLine == null)
		{
			return false;
		}

		try
		{
			LocalPoint lp = LocalPoint.fromWorld(client, player.getWorldLocation());

			if (lp == null)
			{
				return false;
			}

			WorldPoint wp = WorldPoint.fromLocalInstance(client, lp);
			return wp != null && imaginaryLine.distanceTo2D(wp) <= 0;
		}
		catch (RuntimeException ex)
		{
			return false;
		}
	}

	public boolean isLobby()
	{
		return this == LOBBY;
	}

	public boolean isMaiden()
	{
		return this == MAIDEN;
	}

	public boolean isBloat()
	{
		return this == BLOAT;
	}

	public boolean isNylocas()
	{
		return this == NYLOCAS;
	}

	public boolean isSotetseg()
	{
		return this == SOTETSEG || this == SOTETSEG_MAZE;
	}

	public boolean isSotetsegOverworld()
	{
		return this == SOTETSEG;
	}

	public boolean isSotetsegUnderworld()
	{
		return this == SOTETSEG_MAZE;
	}

	public boolean isXarpus()
	{
		return this == XARPUS;
	}

	public boolean isVerzik()
	{
		return this == VERZIK;
	}

	public boolean isLootRoom()
	{
		return this == LOOT_ROOM;
	}

	public boolean isUnknown()
	{
		return this == UNKNOWN;
	}
}
