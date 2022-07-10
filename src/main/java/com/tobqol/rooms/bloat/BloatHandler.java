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
package com.tobqol.rooms.bloat;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Region;
import com.tobqol.rooms.RoomHandler;
import com.tobqol.rooms.bloat.commons.BloatConstants;
import com.tobqol.rooms.bloat.commons.BloatTable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

@Slf4j
public class BloatHandler extends RoomHandler
{
	@Getter
	@CheckForNull
	private NPC bloatNpc = null;

	@Inject
	protected BloatHandler(TheatreQOLPlugin plugin, TheatreQOLConfig config)
	{
		super(plugin, config);
		setRoomRegion(Region.BLOAT);
	}

	@Override
	public void load()
	{
	}

	@Override
	public void unload()
	{
		reset();
	}

	@Override
	public void reset()
	{
		bloatNpc = null;
	}

	@Override
	public boolean active()
	{
		return instance.getCurrentRegion().isBloat() && bloatNpc != null && !bloatNpc.isDead();
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged e)
	{
		if (!e.getGroup().equals(TheatreQOLConfig.GROUP_NAME))
		{
			return;
		}

		switch (e.getKey())
		{
			case "hideCeilingChains":
				when(config.shouldNullCeilingChains(), this::nullCeilingChains, sceneManager::refreshScene);
				break;
		}
	}

	@Subscribe(priority = -1)
	private void onGameStateChanged(GameStateChanged e)
	{
		if (!active() || e.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		when(config.shouldNullCeilingChains(), this::nullCeilingChains, null);
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned e)
	{
		if (active())
		{
			return;
		}

		isNpcFromName(e.getNpc(), BloatConstants.BOSS_NAME, n ->
		{
			instance.lazySetMode(() -> BloatTable.findMode(n.getId()));
			bloatNpc = n;
		});

		when(config.shouldNullCeilingChains(), this::nullCeilingChains, null);
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned e)
	{
		if (!active())
		{
			return;
		}

		isNpcFromName(e.getNpc(), BloatConstants.BOSS_NAME, $ -> reset());
	}

	private void nullCeilingChains()
	{
		sceneManager.removeTheseGameObjects(1, BloatTable.CEILING_CHAINS);
	}
}
