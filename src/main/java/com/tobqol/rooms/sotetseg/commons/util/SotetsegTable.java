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
package com.tobqol.rooms.sotetseg.commons.util;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.tobqol.api.game.Instance;
import lombok.RequiredArgsConstructor;
import net.runelite.api.GroundObject;
import net.runelite.api.NpcID;
import net.runelite.api.NullNpcID;

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
