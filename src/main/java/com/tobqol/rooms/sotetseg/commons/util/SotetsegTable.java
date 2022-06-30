package com.tobqol.rooms.sotetseg.commons.util;

import lombok.RequiredArgsConstructor;

import net.runelite.api.GroundObject;
import net.runelite.api.NpcID;
import net.runelite.api.NullNpcID;

import com.tobqol.api.game.Instance;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

@RequiredArgsConstructor
public enum SotetsegTable implements SotetsegConstants
{
	SOTETSEG_NOT_CLICKABLE(NpcID.SOTETSEG_10864, NpcID.SOTETSEG, NpcID.SOTETSEG_10867),
	SOTETSEG_CLICKABLE(NpcID.SOTETSEG_10865, NpcID.SOTETSEG_8388, NpcID.SOTETSEG_10868),
	TORNADO(NullNpcID.NULL_10866, NullNpcID.NULL_8389, NullNpcID.NULL_10869);

	private final int sm;
	private final int rg;
	private final int hm;

	private static final Table<Instance.Mode, Integer, SotetsegTable> TABLE;

	static
	{
		ImmutableTable.Builder<Instance.Mode, Integer, SotetsegTable> builder = ImmutableTable.builder();

		for (SotetsegTable table : values())
		{
			builder.put(Instance.Mode.STORY, table.sm, table);
			builder.put(Instance.Mode.REGULAR, table.rg, table);
			builder.put(Instance.Mode.HARD, table.hm, table);
		}

		TABLE = builder.build();
	}

	public static Instance.Mode findMode(int npcId)
	{
		return Instance.findFirstMode(mode -> TABLE.contains(mode, npcId));
	}

	public static boolean anyMatch(SotetsegTable table, int npcId)
	{
		return table != null && (table.sm == npcId || table.rg == npcId || table.hm == npcId);
	}

	public static boolean isActiveMazeObject(GroundObject obj)
	{
		return obj != null && ACTIVE_MAZE_GROUND_OBJS.contains(obj.getId());
	}
}
