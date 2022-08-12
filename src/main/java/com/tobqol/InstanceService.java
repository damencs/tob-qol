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

import com.tobqol.api.game.Instance;
import com.tobqol.api.game.Region;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
@Singleton
final class InstanceService implements Instance
{
	private final Client client;
	private final TheatreQOLPlugin plugin;

	// Party Status (0=None, 1=In Party, 2=Inside/Spectator, 3=Dead Spectating)
	private int partyStatus = 0;
	private int roomStatus = 0;

	@Getter
	@Setter
	private int bossHealth = -1;

	@Setter
	private boolean preciseTimers = false;

	private Mode mode = null;
	private Region region = Region.UNKNOWN;

	private final Set<String> raiders = new HashSet<>();
	private final Set<String> deadRaiders = new HashSet<>();

	private boolean regionUpdated = false;
	private int tickCycle = -1;

	@Setter
	private int previousRegion;

	@Inject
	InstanceService(Client client, TheatreQOLPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;
	}

	void reset()
	{
		region = Region.UNKNOWN;
		mode = null;
		regionUpdated = false;
		tickCycle = -1;
		deadRaiders.clear();
		raiders.clear();
		bossHealth = -1;
	}

	void tick()
	{
		if (regionUpdated && roomStatus == 0)
		{
			if (region.isLobby() || region.isLootRoom() || region.isUnknown())
			{
				regionUpdated = false;
				tickCycle = -1;
				return;
			}

			for (Player player : client.getPlayers())
			{
				if (region.isPCIL(client, player))
				{
					log.debug("Updating Theatre of Blood instance timer for '{}'. Previous: {}, New: [2b, 3a]", region.next().prettyName(), tickCycle);
					regionUpdated = false;
					tickCycle = 2;
					break;
				}
			}
		}

		if (tickCycle == -1)
		{
			return;
		}

		tickCycle = ++tickCycle % 4;
	}

	boolean outside()
	{
		return partyStatus <= 1;
	}

	boolean limbo()
	{
		return outside() || roomStatus == 0;
	}

	void setPartyStatus(int value)
	{
		if (partyStatus == value)
		{
			return;
		}

		partyStatus = value;

		if (outside())
		{
			reset();
			plugin.reset(false);
		}
	}

	void setRoomStatus(int value)
	{
		if (roomStatus == value)
		{
			return;
		}

		roomStatus = value;

		if (value == 0)
		{
			deadRaiders.clear();
		}
	}

	void setRegion(Region region)
	{
		if (region == null)
		{
			return;
		}

		if ((this.region.isSotetseg() && !region.isSotetseg()) || this.region != region)
		{
			regionUpdated = true;
		}

		this.region = region;
	}

	void addRaider(String name)
	{
		if (raiders.contains(name))
		{
			return;
		}

		raiders.add(name);
	}

	void addDeadRaider(String name)
	{
		if (deadRaiders.contains(name))
		{
			return;
		}

		deadRaiders.add(name);
	}

	@Override
	public boolean lazySetMode(Supplier<Mode> modeSupplier)
	{
		if (mode != null || modeSupplier == null)
		{
			return false;
		}

		Mode nMode = modeSupplier.get();

		if (nMode == null || Objects.equals(mode, nMode))
		{
			return false;
		}

		log.debug("Setting Theatre of Blood instanced-mode. Previous: {}, New: {}", mode == null ? "UNKNOWN" : mode, nMode);
		mode = nMode;
		return true;
	}

	public boolean isInRaid()
	{
		return partyStatus == 2 || partyStatus == 3;
	}

	@Nullable
	@Override
	public Mode mode()
	{
		return mode;
	}

	@Override
	public boolean isStoryMode()
	{
		return mode != null && mode.isStoryMode();
	}

	@Override
	public boolean isRegularMode()
	{
		return mode != null && mode.isRegularMode();
	}

	@Override
	public boolean isHardMode()
	{
		return mode != null && mode.isHardMode();
	}

	@Override
	public Region getCurrentRegion()
	{
		return region;
	}

	@Override
	public int getRaidStatus()
	{
		return partyStatus;
	}

	@Override
	public int getRoomStatus()
	{
		return roomStatus;
	}

	@Override
	public int getPartyStatus()
	{
		return partyStatus;
	}

	@Override
	public int getPartySize()
	{
		return raiders.size();
	}

	@Override
	public int getDeathSize()
	{
		return deadRaiders.size();
	}

	@Override
	public int getTotalAlive()
	{
		return Math.max(getPartySize() - getDeathSize(), 0);
	}

	@Override
	public int getTickCycle()
	{
		return tickCycle;
	}

	@Override
	public void resetTickCycle()
	{
		tickCycle = -1;
	}
}
