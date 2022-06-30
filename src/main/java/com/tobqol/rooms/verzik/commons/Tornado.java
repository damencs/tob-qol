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

import com.tobqol.api.util.PerspectiveUtil;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

import java.awt.*;
import java.util.Optional;
import java.util.function.Predicate;

@Accessors(fluent = true)
public class Tornado implements Predicate<NPC>
{
	@Getter
	private final NPC npc;

	private WorldPoint first;
	private WorldPoint second;

	public Tornado(NPC npc)
	{
		this.npc = npc;
		this.first = npc.getWorldLocation();
		this.second = this.first;
	}

	public Optional<Polygon> first(Client client)
	{
		return PerspectiveUtil.toTilePoly(client, first);
	}

	public Optional<Polygon> second(Client client)
	{
		return PerspectiveUtil.toTilePoly(client, second);
	}

	public void shift()
	{
		first = second;
		second = npc.getWorldLocation();
	}

	@Override
	public boolean test(NPC npc)
	{
		return this.npc.getIndex() == npc.getIndex();
	}
}
