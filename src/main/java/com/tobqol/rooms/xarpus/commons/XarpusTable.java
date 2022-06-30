package com.tobqol.rooms.xarpus.commons;

import lombok.AccessLevel;
import lombok.Getter;

import net.runelite.api.NpcID;

import com.tobqol.api.game.Instance;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import javax.annotation.Nullable;

@Getter(AccessLevel.PACKAGE)
public enum XarpusTable implements XarpusConstants
{
	XARPUS_INACTIVE(NpcID.XARPUS_10766, NpcID.XARPUS, NpcID.XARPUS_10770),
	XARPUS_P1(NpcID.XARPUS_10767, NpcID.XARPUS_8339, NpcID.XARPUS_10771),
	XARPUS_P23(NpcID.XARPUS_10768, NpcID.XARPUS_8340, NpcID.XARPUS_10772),
	XARPUS_DEAD(NpcID.XARPUS_10769, NpcID.XARPUS_8341, NpcID.XARPUS_10773);

	private final Table.Cell<Instance.Mode, Integer, XarpusTable> smCell;
	private final Table.Cell<Instance.Mode, Integer, XarpusTable> rgCell;
	private final Table.Cell<Instance.Mode, Integer, XarpusTable> hmCell;

	private static final Table<Instance.Mode, Integer, XarpusTable> TABLE;

	static
	{
		ImmutableTable.Builder<Instance.Mode, Integer, XarpusTable> builder = ImmutableTable.builder();

		for (XarpusTable table : values())
		{
			builder.put(table.smCell);
			builder.put(table.rgCell);
			builder.put(table.hmCell);
		}

		TABLE = builder.build();
	}

	@Nullable
	public static Instance.Mode findMode(int npcId)
	{
		return Instance.findFirstMode(mode -> TABLE.contains(mode, npcId));
	}

	XarpusTable(int smId, int rgId, int hmId)
	{
		this.smCell = Tables.immutableCell(Instance.Mode.STORY, smId, this);
		this.rgCell = Tables.immutableCell(Instance.Mode.REGULAR, rgId, this);
		this.hmCell = Tables.immutableCell(Instance.Mode.HARD, hmId, this);
	}
}
