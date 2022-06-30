package com.tobqol.rooms.maiden.commons;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import com.tobqol.api.game.Health;
import com.tobqol.api.game.Hitpoints;
import com.tobqol.api.game.Instance;
import com.tobqol.rooms.maiden.commons.util.MaidenPhase;
import com.tobqol.rooms.maiden.commons.util.MaidenTable;

import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;

import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Predicate;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(of = "npc", doNotUseGetters = true)
public class MaidenRedCrab implements Predicate<NPC>
{
	NPC npc;
	Health health;
	String phaseKey;
	String spawnKey;
	boolean scuffed;

	public MaidenRedCrab(Client client, Instance instance, NPC npc, MaidenPhase phase)
	{
		this.npc = npc;
		this.health = new Health(Hitpoints.MAIDEN_MATOMENOS.getBaseHP(instance));
		this.phaseKey = phase.key();

		Pair<String, Boolean> id = MaidenTable.lookupMatomenosSpawn(client, npc);
		this.spawnKey = id == null ? "Unknown" : id.getLeft();
		this.scuffed = id != null && id.getRight();
	}

	public int distance(Actor actor)
	{
		if (actor == null)
		{
			return -1;
		}

		return npc.getWorldArea().distanceTo2D(actor.getWorldArea()) - 1;
	}

	@Override
	public boolean test(NPC npc)
	{
		return npc != null && (npc == this.npc || npc.getIndex() == this.npc.getIndex());
	}
}
