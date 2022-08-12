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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;
import net.runelite.api.NullNpcID;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VerzikConstants
{
	public static final String VERZIK_NAME = "Verzik Vitur";

	Pattern COMPLETION = Pattern.compile("Theatre of Blood total completion time:");

	// NPC IDs
	public static final int SM_VERZIK_P1_INACTIVE_NPC = NpcID.VERZIK_VITUR_10830; // Not Attackable
	public static final int SM_VERZIK_P1_NPC = NpcID.VERZIK_VITUR_10831;
	public static final int SM_VERZIK_P2_INACTIVE_NPC = NpcID.VERZIK_VITUR_10832; // Transitioning into P2
	public static final int SM_VERZIK_P2_NPC = NpcID.VERZIK_VITUR_10833;
	public static final int SM_VERZIK_P3_INACTIVE_NPC = NpcID.VERZIK_VITUR_10834; // Transitioning into P3
	public static final int SM_VERZIK_P3_NPC = NpcID.VERZIK_VITUR_10835;
	public static final int SM_VERZIK_P3_DEATH_NPC = NpcID.VERZIK_VITUR_10836; // Dying and turning into a bat

	public static final int RG_VERZIK_P1_INACTIVE_NPC = NpcID.VERZIK_VITUR_8369; // Not Attackable
	public static final int RG_VERZIK_P1_NPC = NpcID.VERZIK_VITUR_8370;
	public static final int RG_VERZIK_P2_INACTIVE_NPC = NpcID.VERZIK_VITUR_8371; // Transitioning into P2
	public static final int RG_VERZIK_P2_NPC = NpcID.VERZIK_VITUR_8372;
	public static final int RG_VERZIK_P3_INACTIVE_NPC = NpcID.VERZIK_VITUR_8373; // Transitioning into P3
	public static final int RG_VERZIK_P3_NPC = NpcID.VERZIK_VITUR_8374;
	public static final int RG_VERZIK_P3_DEATH_NPC = NpcID.VERZIK_VITUR_8375; // Dying and turning into a bat

	public static final int HM_VERZIK_P1_INACTIVE_NPC = NpcID.VERZIK_VITUR_10847; // Not Attackable
	public static final int HM_VERZIK_P1_NPC = NpcID.VERZIK_VITUR_10848;
	public static final int HM_VERZIK_P2_INACTIVE_NPC = NpcID.VERZIK_VITUR_10849; // Transitioning into P2
	public static final int HM_VERZIK_P2_NPC = NpcID.VERZIK_VITUR_10850;
	public static final int HM_VERZIK_P3_INACTIVE_NPC = NpcID.VERZIK_VITUR_10851; // Transitioning into P3
	public static final int HM_VERZIK_P3_NPC = NpcID.VERZIK_VITUR_10852;
	public static final int HM_VERZIK_P3_DEATH_NPC = NpcID.VERZIK_VITUR_10853; // Dying and turning into a bat

	public static final List<Integer> P2_NPC_IDS = ImmutableList.of(SM_VERZIK_P2_NPC, RG_VERZIK_P2_NPC, HM_VERZIK_P2_NPC);
	public static final List<Integer> P3_NPC_IDS = ImmutableList.of(SM_VERZIK_P3_NPC, RG_VERZIK_P3_NPC, HM_VERZIK_P3_NPC);

	public static final int TORNADO_NPC = NullNpcID.NULL_8386;

	// Pillar Name has color tags and is called "Supporting Pillar"
	public static final int PILLAR_NPC = NpcID.SUPPORTING_PILLAR;
	public static final int PILLAR_DEATH_NPC = NpcID.COLLAPSING_PILLAR_8378;

	public static final int SM_WEB = NpcID.WEB_10837;
	public static final int RG_WEB = NpcID.WEB;
	public static final int HM_WEB = NpcID.WEB_10854;

	// Graphic Object IDs
	public static final int YELLOW_POOL = 1595;

	// Misc. Stuff
	public static final Set<Integer> WEAPON_SET = ImmutableSet.of(
			ItemID.TOXIC_BLOWPIPE,
			ItemID.ABYSSAL_TENTACLE
	);

	public static final Set<Integer> HELMET_SET = ImmutableSet.of(
			ItemID.SERPENTINE_HELM,
			ItemID.TANZANITE_HELM,
			ItemID.MAGMA_HELM
	);
}
