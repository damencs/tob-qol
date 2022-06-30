package com.tobqol.rooms.nylocas.commons;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.runelite.api.NPC;

import com.tobqol.api.game.Instance;

import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
public class NyloBoss
{
	private final NPC npc;
	private final Instance.Mode mode;

	private NylocasMap definition;

	public static NyloBoss spawned(NPC npc, Instance.Mode mode)
	{
		return new NyloBoss(npc, mode, NylocasMap.queryTable(mode, npc.getId()));
	}

	public void changed()
	{
		this.definition = NylocasMap.queryTable(mode, npc.getId());
	}

	public boolean dead()
	{
		return this.npc.getHealthRatio() == 0;
	}

	public Optional<NylocasMap> definition()
	{
		return this.definition == null ? Optional.empty() : Optional.of(this.definition);
	}
}
