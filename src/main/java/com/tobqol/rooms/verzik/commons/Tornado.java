package com.tobqol.rooms.verzik.commons;

import lombok.Getter;
import lombok.experimental.Accessors;

import net.runelite.api.NPC;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

import com.tobqol.api.util.PerspectiveUtil;

import java.awt.*;
import java.util.Optional;
import java.util.function.Predicate;

@Accessors(fluent = true)
public class Tornado implements Predicate<NPC>
{
	@Getter
	private final NPC npc;

	private WorldPoint first;
	private WorldPoint second;

	public Tornado(NPC npc)
	{
		this.npc = npc;
		this.first = npc.getWorldLocation();
		this.second = this.first;
	}

	public Optional<Polygon> first(Client client)
	{
		return PerspectiveUtil.toTilePoly(client, first);
	}

	public Optional<Polygon> second(Client client)
	{
		return PerspectiveUtil.toTilePoly(client, second);
	}

	public void shift()
	{
		first = second;
		second = npc.getWorldLocation();
	}

	@Override
	public boolean test(NPC npc)
	{
		return this.npc.getIndex() == npc.getIndex();
	}
}
