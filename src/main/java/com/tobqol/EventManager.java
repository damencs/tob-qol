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
package com.tobqol;

import com.google.common.base.Strings;
import com.tobqol.api.game.RaidConstants;
import com.tobqol.api.game.Region;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Slf4j
final class EventManager
{
	private final Client client;
	private final EventBus eventBus;
	private final TheatreQOLPlugin plugin;

	@Getter
	private final InstanceService instance;

	@Inject
	EventManager(Client client, EventBus eventBus, TheatreQOLPlugin plugin, InstanceService instance)
	{
		this.client = client;
		this.eventBus = eventBus;
		this.plugin = plugin;
		this.instance = instance;
	}

	void startUp()
	{
		instance.reset();
		eventBus.register(this);
	}

	void shutDown()
	{
		eventBus.unregister(this);
		instance.reset();
	}

	public void reset(boolean global)
	{
		plugin.reset(global);
		instance.reset();
	}

	@Subscribe(priority = 7)
	private void onGameStateChanged(GameStateChanged gsc)
	{
		GameState gs = gsc.getGameState();

		if (gs.equals(GameState.LOGGED_IN) || gs.equals(GameState.LOGIN_SCREEN))
		{
			if (gs.equals(GameState.LOGIN_SCREEN))
			{
				reset(true);
				return;
			}

			boolean inside = false;

			for (Region r : Region.values())
			{
				if (r.isLobby() || r.isUnknown())
				{
					continue;
				}

				if (Region.inRegion(client, r))
				{
					inside = true;
					instance.setRegion(r);
					break;
				}
			}

			if (!inside)
			{
				reset(false);
			}
		}
	}

	@Subscribe(priority = 7)
	private void onVarbitChanged(VarbitChanged e)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		instance.setPartyStatus(client.getVarbitValue(Varbits.THEATRE_OF_BLOOD));
		instance.setRoomStatus(client.getVarbitValue(RaidConstants.THEATRE_OF_BLOOD_ROOM_STATUS));
		instance.setBossHealth(client.getVarbitValue(RaidConstants.THEATRE_OF_BLOOD_BOSS_HP));
		instance.setPreciseTimers(client.getVarbitValue(RaidConstants.PRECISE_TIMER) == 1);
	}

	@Subscribe(priority = 7)
	private void onGameTick(GameTick e)
	{
		if (instance.outside())
		{
			if (instance.getPartySize() > 0)
			{
				log.info("reset instance from inside event manager");
				instance.reset();
			}

			return;
		}

		for (int varcStrId = 330; varcStrId <= 334; varcStrId++)
		{
			String username = Text.standardize(client.getVarcStrValue(varcStrId));

			if (Strings.isNullOrEmpty(username))
			{
				continue;
			}

			switch (client.getVarbitValue(Varbits.THEATRE_OF_BLOOD_ORB1 + (varcStrId % 5)))
			{
				case 0: break;
				case 1:
					instance.addRaider(username);
					instance.addDeadRaider(username);
					log.info("GameTick: Varbit Analysis for Party Size and Dead Raider - Username Detected: {} - Party Size: {} - Dead Raider Size: {}", username, instance.getPartySize(), instance.getDeathSize());
					log.info("GameTick Lists: Party: {}, Death: {}", instance.getRaiders(), instance.getDeadRaiders());
					break;
				default:
					instance.addRaider(username);
					break;
			}
		}

		instance.tick();
	}
}