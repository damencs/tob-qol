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
package com.tobqol.rooms.xarpus.commons;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import net.runelite.api.NPC;

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

			builder.put(table.sm(), phase);
			builder.put(table.rg(), phase);
			builder.put(table.hm(), phase);
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
