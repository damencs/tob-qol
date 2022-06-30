package com.tobqol.rooms.bloat.commons;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import lombok.RequiredArgsConstructor;
import net.runelite.api.NpcID;
import com.tobqol.api.game.Instance;

import static com.google.common.collect.Tables.immutableCell;

@RequiredArgsConstructor
public enum BloatTable implements BloatConstants
{
	BLOAT(NpcID.PESTILENT_BLOAT_10812, NpcID.PESTILENT_BLOAT, NpcID.PESTILENT_BLOAT_10813);

	private final int sm, rg, hm;

	private static final Table<Instance.Mode, Integer, BloatTable> LOOKUP_TABLE;

	static
	{
		ImmutableTable.Builder<Instance.Mode, Integer, BloatTable> l_builder = ImmutableTable.builder();

		for (BloatTable def : values())
		{
			l_builder.put(immutableCell(Instance.Mode.STORY, def.sm, def));
			l_builder.put(immutableCell(Instance.Mode.REGULAR, def.rg, def));
			l_builder.put(immutableCell(Instance.Mode.HARD, def.hm, def));
		}

		LOOKUP_TABLE = l_builder.build();
	}

	public static Instance.Mode findMode(int npcId)
	{
		return Instance.findFirstMode(mode -> LOOKUP_TABLE.get(mode, npcId) != null);
	}
}
