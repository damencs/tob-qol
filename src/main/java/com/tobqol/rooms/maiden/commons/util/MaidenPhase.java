package com.tobqol.rooms.maiden.commons.util;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.runelite.api.NPC;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum MaidenPhase
{
	P1("70s"),
	P2("50s"),
	P3("30s"),
	OTHER("*");

	private final String key;

	private static final Map<Integer, MaidenPhase> LOOKUP;

	static
	{
		ImmutableMap.Builder<Integer, MaidenPhase> builder = ImmutableMap.builder();

		Map<MaidenTable, MaidenPhase> phases = new HashMap<>();

		phases.put(MaidenTable.MAIDEN_P0, MaidenPhase.P1);
		phases.put(MaidenTable.MAIDEN_P1, MaidenPhase.P2);
		phases.put(MaidenTable.MAIDEN_P2, MaidenPhase.P3);


		phases.forEach((table, phase) ->
		{
			builder.put(table.sm(), phase);
			builder.put(table.rg(), phase);
			builder.put(table.hm(), phase);
		});

		LOOKUP = builder.build();
	}

	public static MaidenPhase compose(NPC npc)
	{
		if (npc == null)
		{
			return OTHER;
		}

		return LOOKUP.getOrDefault(npc.getId(), OTHER);
	}

	public boolean isPhaseOne()
	{
		return this == P1;
	}

	public boolean isPhaseTwo()
	{
		return this == P2;
	}

	public boolean isPhaseThree()
	{
		return this == P3;
	}

	public boolean isNonTrackedPhase()
	{
		return this == OTHER;
	}
}
