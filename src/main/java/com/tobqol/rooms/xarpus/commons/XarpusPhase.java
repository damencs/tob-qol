package com.tobqol.rooms.xarpus.commons;

import lombok.RequiredArgsConstructor;

import net.runelite.api.NPC;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.Map;

@RequiredArgsConstructor
public enum XarpusPhase
{
	INACTIVE(XarpusTable.XARPUS_INACTIVE),
	P1(XarpusTable.XARPUS_P1),
	P2(XarpusTable.XARPUS_P23),
	P3(null),
	DEAD(XarpusTable.XARPUS_DEAD),
	UNKNOWN(null);

	@Nullable
	private final XarpusTable table;

	private static final Map<Integer, XarpusPhase> LOOKUP_MAP;

	static
	{
		ImmutableMap.Builder<Integer, XarpusPhase> builder = ImmutableMap.builder();

		for (XarpusPhase phase : values())
		{
			if (phase.isAbsent() || phase.isP3())
			{
				continue;
			}

			XarpusTable table = phase.table;

			if (table == null)
			{
				continue;
			}

			builder.put(table.getSmCell().getColumnKey(), phase);
			builder.put(table.getRgCell().getColumnKey(), phase);
			builder.put(table.getHmCell().getColumnKey(), phase);
		}

		LOOKUP_MAP = builder.build();
	}

	public static XarpusPhase compose(NPC npc)
	{
		return LOOKUP_MAP.getOrDefault(npc.getId(), UNKNOWN);
	}

	public boolean isInactive()
	{
		return this == INACTIVE;
	}

	public boolean isP1()
	{
		return this == P1;
	}

	public boolean isInactiveOrP1()
	{
		return isInactive() || isP1();
	}

	public boolean isP2()
	{
		return this == P2;
	}

	public boolean isP3()
	{
		return this == P3;
	}

	public boolean isP2OrP3()
	{
		return isP2() || isP3();
	}

	public boolean isAbsent()
	{
		return this == UNKNOWN;
	}

	public boolean isDead()
	{
		return this == DEAD;
	}
}
