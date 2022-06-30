package com.tobqol;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Client;
import net.runelite.api.Player;

import com.tobqol.api.game.Instance;
import com.tobqol.api.game.Region;

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

	private int varbit6440 = 0; // Never resets, should always match the varbit value
	private int varbit6447 = 0; // Never resets, should always match the varbit value

	private Mode mode = null;
	private Region region = Region.UNKNOWN;

	private final Set<String> raiders = new HashSet<>();
	private final Set<String> deadRaiders = new HashSet<>();

	private boolean regionUpdated = false;
	private int tickCycle = -1;

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
	}

	void tick()
	{
		if (regionUpdated && varbit6447 == 0)
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
		return varbit6440 <= 1;
	}

	boolean limbo()
	{
		return outside() || varbit6447 == 0;
	}

	void setVarbit6440(int value)
	{
		if (varbit6440 == value)
		{
			return;
		}

		varbit6440 = value;

		if (outside())
		{
			reset();
			plugin.reset(false);
		}
	}

	void setVarbit6447(int value)
	{
		if (varbit6447 == value)
		{
			return;
		}

		varbit6447 = value;

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
		return varbit6440;
	}

	@Override
	public int getRoomStatus()
	{
		return varbit6447;
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
