package com.tobqol.rooms.bloat;

import lombok.Getter;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Region;
import com.tobqol.rooms.RoomHandler;
import com.tobqol.rooms.bloat.commons.BloatConstants;
import com.tobqol.rooms.bloat.commons.BloatTable;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

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
			case "nullTankTiles":
				when(config.shouldNullTopTankTiles(), this::nullTopTankTiles, sceneManager::refreshScene);
				break;
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

		when(config.shouldNullTopTankTiles(), this::nullTopTankTiles, null);
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

	private void nullTopTankTiles()
	{
		List<WorldPoint> wpl = new WorldArea(3293, 4445, 6, 6, 1).toWorldPointList();
		wpl.forEach(wp ->
		{
			Collection<WorldPoint> tiles = WorldPoint.toLocalInstance(client, wp);
			tiles.forEach(sceneManager::removeThisTile);
		});

		sceneManager.removeTheseGameObjects(1, BloatTable.TOP_OF_TANK);
	}

	private void nullCeilingChains()
	{
		sceneManager.removeTheseGameObjects(1, BloatTable.CEILING_CHAINS);
	}
}
