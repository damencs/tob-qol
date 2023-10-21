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
package com.tobqol.rooms.verzik.commons;

import com.google.common.collect.*;
import com.tobqol.api.game.Instance;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.runelite.api.NpcID;
import net.runelite.api.NullNpcID;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum VerzikMap
{
	VERZIK_P1_INACTIVE(NpcID.VERZIK_VITUR_10830, NpcID.VERZIK_VITUR_8369, NpcID.VERZIK_VITUR_10847),
	VERZIK_P1(NpcID.VERZIK_VITUR_10831, NpcID.VERZIK_VITUR_8370, NpcID.VERZIK_VITUR_10848),
	VERZIK_P2_INACTIVE(NpcID.VERZIK_VITUR_10832, NpcID.VERZIK_VITUR_8371, NpcID.VERZIK_VITUR_10849),
	VERZIK_P2(NpcID.VERZIK_VITUR_10833, NpcID.VERZIK_VITUR_8372, NpcID.VERZIK_VITUR_10850),
	VERZIK_P3_INACTIVE(NpcID.VERZIK_VITUR_10834, NpcID.VERZIK_VITUR_8373, NpcID.VERZIK_VITUR_10851),
	VERZIK_P3(NpcID.VERZIK_VITUR_10835, NpcID.VERZIK_VITUR_8374, NpcID.VERZIK_VITUR_10852),
	VERZIK_BAT(NpcID.VERZIK_VITUR_10836, NpcID.VERZIK_VITUR_8375, NpcID.VERZIK_VITUR_10853),
	PURPLE_NYLO(NpcID.NYLOCAS_ATHANATOS_10844, NpcID.NYLOCAS_ATHANATOS, NpcID.NYLOCAS_ATHANATOS_10861),
	RED_NYLO(NpcID.NYLOCAS_MATOMENOS_10845, NpcID.NYLOCAS_MATOMENOS_8385, NpcID.NYLOCAS_MATOMENOS_10862),
	MELEE_NYLO(NpcID.NYLOCAS_ISCHYROS_10841, NpcID.NYLOCAS_ISCHYROS_8381, NpcID.NYLOCAS_ISCHYROS_10858),
	RANGE_NYLO(NpcID.NYLOCAS_TOXOBOLOS_10842, NpcID.NYLOCAS_TOXOBOLOS_8382, NpcID.NYLOCAS_TOXOBOLOS_10859),
	MAGIC_NYLO(NpcID.NYLOCAS_HAGIOS_10843, NpcID.NYLOCAS_HAGIOS_8383, NpcID.NYLOCAS_HAGIOS_10860),
	WEB(NpcID.WEB_10837, NpcID.WEB, NpcID.WEB_10854),
	TORNADO(NullNpcID.NULL_10846, NullNpcID.NULL_8386, NullNpcID.NULL_10863); // TODO -> Confirm Story Mode

	private final int sm;
	private final int rg;
	private final int hm;

	public static final String BOSS_NAME = "Verzik Vitur";

	public static final Pattern VERZIK_WAVE = Pattern.compile("Wave 'The Final Challenge' \\(.*\\) complete!");

	public static final int BOSS_IMAGE = 22473;

	// TODO -> Find Story Mode/Regular Pillar NPC IDs
	public static final int PILLAR_NPC_ID = NpcID.SUPPORTING_PILLAR;
	public static final int COLLAPSING_PILLAR_NPC_ID = NpcID.COLLAPSING_PILLAR;

	public static final int VERZIK_P1_ATK_ANIM = 8109;
	public static final int VERZIK_P2_ATK_ANIM = 8114;
	public static final int VERZIK_P2_BOUNCE_ANIM = 8116;
	public static final int VERZIK_P2_HEALING_STATE_ANIM = 8117;
	public static final int VERZIK_P2_TRANSITION = 8118;
	public static final int VERZIK_P3_MAGIC_ANIM = 8124;
	public static final int VERZIK_P3_RANGE_ANIM = 8125; // Reused animation for the Green Ball attack
	public static final int VERZIK_P3_YELLOWS_ANIM = 8126;
	public static final int VERZIK_P3_WEBS_ANIM = 8127;

	public static final int YELLOW_POOL = 1595;
	public static final int YELLOW_GRAPHIC = 1597;

	public static final int GREEN_BALL = 1598;
	public static final String GREEN_BALL_TEXT = "Verzik Vitur fires a powerful projectile in your direction...";
	public static final String GREEN_BALL_BOUNCE_TEXT = "A powerful projectile bounces into your direction...";

	public static final Color VERZIK_COLOR = new Color(176, 92, 204);

	private static final ImmutableMultimap<VerzikMap, Integer> container;
	private static final Table<Instance.Mode, Integer, VerzikMap> lookupTable;

	static
	{
		ImmutableMultimap.Builder<VerzikMap, Integer> mapBuilder = new ImmutableListMultimap.Builder<>();
		ImmutableTable.Builder<Instance.Mode, Integer, VerzikMap> tableBuilder = new ImmutableTable.Builder<>();

		for (VerzikMap def : values())
		{
			mapBuilder.putAll(def, def.sm, def.rg, def.hm);

			tableBuilder.put(Tables.immutableCell(Instance.Mode.STORY, def.sm, def));
			tableBuilder.put(Tables.immutableCell(Instance.Mode.REGULAR, def.rg, def));
			tableBuilder.put(Tables.immutableCell(Instance.Mode.HARD, def.hm, def));
		}

		container = mapBuilder.build();
		lookupTable = tableBuilder.build();
	}

	public static boolean matchesAnyMode(VerzikMap def, int npcId)
	{
		if (def == null)
		{
			return false;
		}

		return container.get(def).contains(npcId);
	}

	@Nullable
	public static VerzikMap queryTable(Instance.Mode mode, int npcId)
	{
		if (mode == null)
		{
			return null;
		}

		return lookupTable.get(mode, npcId);
	}

	@Nullable
	public static VerzikMap queryTable(int npcId)
	{
		for (Instance.Mode mode : Instance.Mode.values())
		{
			VerzikMap def = queryTable(mode, npcId);

			if (def == null)
			{
				continue;
			}

			return def;
		}

		return null;
	}

	public static boolean isStoryMode(int npcId)
	{
		return queryTable(Instance.Mode.STORY, npcId) != null;
	}

	public static boolean isRegularMode(int npcId)
	{
		return queryTable(Instance.Mode.REGULAR, npcId) != null;
	}

	public static boolean isHardMode(int npcId)
	{
		return queryTable(Instance.Mode.HARD, npcId) != null;
	}

	@Nullable
	public static Instance.Mode findMode(int npcId)
	{
		if (isStoryMode(npcId))
		{
			return Instance.Mode.STORY;
		}

		if (isRegularMode(npcId))
		{
			return Instance.Mode.REGULAR;
		}

		if (isHardMode(npcId))
		{
			return Instance.Mode.HARD;
		}

		return null;
	}
}
