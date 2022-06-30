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
package com.tobqol.api.game;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Hitpoints
{
	MAIDEN(
			500,
			2625, 3062, 3500,
			2625, 3062, 3500 // Maiden's HP is identical to Regular Mode
	),
	MAIDEN_MATOMENOS(
			16,
			75, 87, 100,
			75, 87, 100 // Maiden's Matomenos HP is identical to Regular Mode
	),
	MAIDEN_BLOOD_SPAWN(
			0, // TODO -> Find Story Mode SOLO Base HP
			0, 0, 0, // TODO -> Find Regular Mode Base HP
			0, 0, 0 // TODO -> Find Hard Mode Base HP
	),
	BLOAT(
			320,
			1500, 1750, 2000,
			1800, 2100, 2400
	),
	NYLOCAS_BOSS(
			360,
			1875, 2187, 2500,
			0, 0, 0 // TODO -> Find Hard Mode Base HP
	),
	NYLOCAS_DEMI_BOSS(
			-1, //-> Demi-Boss doesn't spawn in Story Mode
			-1, -1, -1, //-> Demi-Boss doesn't spawn in Regular Mode
			0, 0, 0 // TODO -> Find Hard Mode Base HP
	),
	NYLOCAS_SMALL(
			2,
			8, 9, 11,
			0, 0, 0 // TODO -> Find Hard Mode Base HP
	),
	NYLOCAS_BIG(
			3,
			16, 19, 22,
			0, 0, 0 // TODO -> Find Hard Mode Base HP
	),
	SOTETSEG(
			560,
			3000, 3500, 4000,
			3000, 3500, 4000 // Sotetseg's HP is identical to Regular Mode
	),
	XARPUS(
			411,
			3810, 4445, 5080,
			0, 0, 0 // TODO -> Find Hard Mode Base HP
	),
	VERZIK_P1(
			300,
			0, 0, 0, // TODO -> Find Regular Mode Base HP
			0, 0, 0 // TODO -> Find Hard Mode Base HP
	),
	VERZIK_P2(
			400,
			2437, 2843, 3250,
			0, 0, 0 // TODO -> Find Hard Mode Base HP
	),
	VERZIK_P3(
			574,
			2437, 2843, 3250,
			0, 0, 0 // TODO -> Find Hard Mode Base HP
	),
	VERZIK_MATOMENOS(
			0, // TODO -> Find Story Mode SOLO Base HP
			150, 175, 200,
		0, 0, 0 // TODO -> Find Hard Mode Base HP
	),
	VERZIK_NYLOCAS(
			0, // TODO -> Find Story Mode SOLO Base HP
			0, 0, 0, // TODO -> Find Regular Mode Base HP
		0, 0, 0 // TODO -> Find Hard Mode Base HP
	);

	private final int sm_solo;
	private final int rg_trios, rg_fours, rg_fives;
	private final int hm_trios, hm_fours, hm_fives;

	public int getBaseHP(Instance instance)
	{
		if (instance == null)
		{
			return 0;
		}

		Instance.Mode mode = instance.mode();
		int partySize = instance.getPartySize();

		if (mode == null || partySize <= 0)
		{
			return 0;
		}

		// Story Mode HP scales linearly
		if (mode.isStoryMode())
		{
			return sm_solo * partySize;
		}

		switch (partySize)
		{
			case 5: return mode.isRegularMode() ? rg_fives : hm_fives;
			case 4: return mode.isRegularMode() ? rg_fours : hm_fours;
			default: return mode.isRegularMode() ? rg_trios : hm_trios;
		}
	}
}
