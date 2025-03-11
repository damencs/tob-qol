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
package com.tobqol.loottracking;

import lombok.Getter;
import net.runelite.api.ItemID;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

@Getter
public enum LootItems
{
	AVERNIC_DEFENDER_HILT(ItemID.AVERNIC_DEFENDER_HILT, "Avernic defender hilt"),
	JUSTICIAR_FACEGUARD(ItemID.JUSTICIAR_FACEGUARD, "Justiciar faceguard"),
	JUSTICIAR_CHESTGUARD(ItemID.JUSTICIAR_CHESTGUARD, "Justiciar chestguard"),
	JUSTICIAR_LEGGUARDS(ItemID.JUSTICIAR_LEGGUARDS, "Justiciar leguards"),
	GHRAZI_RAPIER(ItemID.GHRAZI_RAPIER, "Ghrazi rapier"),
	SANGUINESTI_STAFF(ItemID.SANGUINESTI_STAFF_UNCHARGED, "Sanguinesti staff"),
	SCYTHE_OF_VITUR(ItemID.SCYTHE_OF_VITUR_UNCHARGED, "Scythe of vitur");

	private final int itemId;
	private final String itemName;

	@Getter
	private static final HashMap<Integer, String> itemLookup = new HashMap<>();

	@Getter
	private static final HashMap<String, Integer> itemLookupByName = new HashMap<>();

	static
	{
		EnumSet.allOf(LootItems.class).forEach(item -> itemLookup.put(item.getItemId(), item.getItemName()));
		EnumSet.allOf(LootItems.class).forEach(item -> itemLookupByName.put(item.getItemName(), item.getItemId()));
	}

	LootItems(int itemId, String itemName)
	{
		this.itemId = itemId;
		this.itemName = itemName;
	}
}
