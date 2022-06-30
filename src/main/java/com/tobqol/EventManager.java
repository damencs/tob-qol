package com.tobqol;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import com.tobqol.api.game.Region;
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

	private void reset(boolean global)
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
		instance.setVarbit6440(client.getVarbitValue(6440));
		instance.setVarbit6447(client.getVarbitValue(6447));
	}

	@Subscribe(priority = 7)
	private void onGameTick(GameTick e)
	{
		if (instance.outside())
		{
			return;
		}

		for (int varcStrId = 330; varcStrId <= 334; varcStrId++)
		{
			String username = Text.standardize(client.getVarcStrValue(varcStrId));

			if (Strings.isNullOrEmpty(username))
			{
				continue;
			}

			switch (client.getVarbitValue(6442 + (varcStrId % 5)))
			{
				case 0: break;
				case 1:
					instance.addRaider(username);
					instance.addDeadRaider(username);
					break;
				default:
					instance.addRaider(username);
					break;
			}
		}

		instance.tick();
	}
}