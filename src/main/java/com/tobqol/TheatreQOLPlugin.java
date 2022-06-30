/*
 * Copyright (c) 2022, Damen <gh: damencs>
 * Copyright (c) 2022, WLoumakis <gh: WLoumakis> - Portions of "Loot Reminder" and "MES Options"
 * Copyright (c) 2020, Broooklyn <gh: Broooklyn> - "ToB Light Up" Relevant Code
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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.tobqol.api.game.Instance;
import com.tobqol.api.util.ConfigReflectUtil;
import com.tobqol.config.SupplyChestPreference;
import com.tobqol.rooms.RemovableOverlay;
import com.tobqol.rooms.RoomHandler;
import com.tobqol.rooms.bloat.BloatHandler;
import com.tobqol.rooms.maiden.MaidenHandler;
import com.tobqol.rooms.nylocas.NylocasHandler;
import com.tobqol.rooms.sotetseg.SotetsegHandler;
import com.tobqol.rooms.verzik.VerzikHandler;
import com.tobqol.rooms.xarpus.XarpusHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ExternalPluginsChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

@PluginDescriptor(
		name = "<html><font color=\"#ff6961\"><b>ToB QoL</b></font></html>",
		description = "Theatre of Blood Quality of Life Enhancement Features to be used throughout a raid.<br>" +
				"Created By: <em>Damen</em><br>" +
				"Prior Plugin References: <em>#</em>",
		tags = { "tob", "tobqol", "of", "blood", "maiden", "bloat", "nylo", "nylocas", "sotetseg", "xarpus", "verzik", "combat", "bosses", "pvm", "pve", "damen" },
		enabledByDefault = false,
		loadInSafeMode = false
)
@Slf4j
public class TheatreQOLPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private com.tobqol.EventManager eventManager;

	@Inject
	private Provider<MaidenHandler> maiden;

	@Inject
	private Provider<BloatHandler> bloat;

	@Inject
	private Provider<NylocasHandler> nylocas;

	@Inject
	private Provider<SotetsegHandler> sotetseg;

	@Inject
	private Provider<XarpusHandler> xarpus;

	@Inject
	private Provider<VerzikHandler> verzik;

	@Inject
	private TheatreQOLOverlay overlay;

	@Inject
	private TheatreQOLConfig config;

	@Provides
	TheatreQOLConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TheatreQOLConfig.class);
	}

	@CheckForNull
	private RoomHandler[] rooms = null;

	private final Multimap<String, RemovableOverlay> removableOverlays = MultimapBuilder
			.hashKeys()
			.arrayListValues()
			.build();

	private boolean darknessHidden;
	private static final Set<Integer> VER_SINHAZA_REGIONS = ImmutableSet.of(
			14386,
			14642
	);

	@Getter
	private GameObject entrance;

	@Getter
	private GameObject lootChest;

	@Getter
	boolean chestHasLoot = false;

	private static final int TOB_CHEST_UNLOOTED = 41435;
	private static final int TOB_CHEST_LOOTED = 41436;
	private static final int TOB_BANK_CHEST = 41437;

	private static final int TOB_ENTRANCE = 32653;

	private final ArrayListMultimap<String, Integer> optionIndexes = ArrayListMultimap.create();

	private static final Set<String> TOB_CHEST_TARGETS = ImmutableSet.of(
			"Stamina potion(4)",
			"Prayer potion(4)",
			"Saradomin brew(4)",
			"Super restore(4)",
			"Mushroom potato",
			"Shark",
			"Sea turtle",
			"Manta ray"
	);

	@Override
	public void configure(Binder binder)
	{
		binder.bind(Instance.class).to(com.tobqol.InstanceService.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		eventManager.startUp();

		if (rooms == null)
		{
			rooms = new RoomHandler[] { maiden.get(), bloat.get(), nylocas.get(), sotetseg.get(), xarpus.get(), verzik.get() };

			for (RoomHandler room : rooms)
			{
				room.init();
			}
		}

		for (RoomHandler room : rooms)
		{
			room.load();
			eventBus.register(room);
		}
	}

	@Override
	protected void shutDown()
	{
		reset(true);

		overlayManager.remove(overlay);
		eventManager.shutDown();

		removableOverlays.forEach((k, v) -> overlayManager.removeIf(v.provideOverlay().getClass()::isInstance)); // Remove all of the active 'RoomHandler' Overlays
		removableOverlays.clear(); // Explode the collection here as this collection gets rebuilt on this.startUp

		if (rooms != null)
		{
			for (RoomHandler room : rooms)
			{
				// Unregister before unloading to prevent potential data population
				eventBus.unregister(room);
				room.unload();
			}
		}
	}

	void reset(boolean global)
	{
		if (rooms != null)
		{
			for (RoomHandler room : rooms)
			{
				room.reset();
				log.debug("Resetting {}", room.getClass().getSimpleName());
			}
		}

		if (global)
		{
			darknessHidden = false;
			hideDarkness(false);

			entrance = null;
			lootChest = null;
			chestHasLoot = false;
		}
	}

	@Subscribe(priority = -1)
	private void onPluginChanged(PluginChanged e)
	{
		if (e.getPlugin() != this || !e.isLoaded())
		{
			return;
		}

		executor.execute(() ->
		{
			ConfigReflectUtil.load(config, configManager);

			if (ConfigReflectUtil.updateAll(config, configManager))
			{
				eventBus.post(new ExternalPluginsChanged(Collections.emptyList()));
			}
		});
	}

	public boolean addRemovableOverlay(String configKey, RemovableOverlay removableOverlay)
	{
		if (Strings.isNullOrEmpty(configKey) || removableOverlay == null || removableOverlays.containsValue(removableOverlay))
		{
			return false;
		}

		if (!removableOverlay.remove(config))
		{
			overlayManager.add(removableOverlay.provideOverlay());
		}

		return removableOverlays.put(configKey, removableOverlay);
	}

	@Subscribe(priority = 1) // Reassure parent class has priority over the children classes
	private void onConfigChanged(ConfigChanged e)
	{
		if (!e.getGroup().equals(TheatreQOLConfig.GROUP_NAME))
		{
			return;
		}

		String key = e.getKey();

		removableOverlays.get(key).forEach(removableOverlay ->
		{
			Overlay overlay = removableOverlay.provideOverlay();

			if (removableOverlay.remove(config))
			{
				overlayManager.removeIf(overlay.getClass()::isInstance);
				return;
			}

			overlayManager.add(overlay);
		});

		if (ConfigReflectUtil.update(config, configManager, key))
		{
			eventBus.post(new ExternalPluginsChanged(Collections.emptyList()));
		}
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if (isInVerSinhaza() && config.lightUp() && !darknessHidden)
		{
			hideDarkness(true);
		}
		else if ((!isInVerSinhaza() && darknessHidden) || (!config.lightUp() && darknessHidden))
		{
			hideDarkness(false);
		}

		if (isInVerSinhaza())
		{
			// Determine if chest has loot and draw an arrow overhead
			if (lootChest != null && Objects.requireNonNull(getObjectComposition(lootChest.getId())).getId() == TOB_CHEST_UNLOOTED && !chestHasLoot)
			{
				chestHasLoot = true;
				client.setHintArrow(lootChest.getWorldLocation());
			}

			// Clear the arrow if the loot is taken
			if (lootChest != null && Objects.requireNonNull(getObjectComposition(lootChest.getId())).getId() == TOB_CHEST_LOOTED && chestHasLoot)
			{
				chestHasLoot = false;
				client.clearHintArrow();
			}
		}
		else
		{
			if (lootChest != null)
			{
				lootChest = null;
			}
		}
	}

	@Subscribe
	private void onClientTick(ClientTick e)
	{
		if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen())
		{
			return;
		}

		if (config.supplyChestMES() != SupplyChestPreference.OFF)
		{
			MenuEntry[] menuEntries = client.getMenuEntries();

			// Build option map for quick lookup in findIndex
			int index = 0;
			optionIndexes.clear();

			for (MenuEntry entry : menuEntries)
			{
				String option = Text.removeTags(entry.getOption());
				optionIndexes.put(option, index++);
			}

			// Perform swaps
			index = 0;

			for (MenuEntry entry : menuEntries)
			{
				swapMenuEntry(index++, entry);
			}
		}

		if (client.getGameState() == GameState.LOGGED_IN && !client.isMenuOpen() && config.bankAllMES() && (isInVerSinhaza() || isInLootRoom()))
		{
			MenuEntry[] entries = client.getMenuEntries();

			for (MenuEntry entry : entries)
			{
				if (entry.getOption().equals("Bank-all"))
				{
					entry.setForceLeftClick(true);
					break;
				}
			}

			client.setMenuEntries(entries);
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		switch (event.getGameObject().getId())
		{
			case TOB_BANK_CHEST:
				lootChest = event.getGameObject();
				break;
			case TOB_ENTRANCE:
				entrance = event.getGameObject();
				break;
		}
	}

	@Nullable
	private ObjectComposition getObjectComposition(int id)
	{
		ObjectComposition objectComposition = client.getObjectDefinition(id);
		return objectComposition.getImpostorIds() == null ? objectComposition : objectComposition.getImpostor();
	}

	protected void hideDarkness(boolean hide)
	{
		Widget darkness = client.getWidget(28, 1);
		if (darkness != null)
		{
			darknessHidden = hide;
			darkness.setHidden(hide);
		}
	}

	public boolean isInVerSinhaza()
	{
		return VER_SINHAZA_REGIONS.contains(client.getLocalPlayer().getWorldLocation().getRegionID());
	}

	private boolean isInLootRoom()
	{
		LocalPoint local = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
		return WorldPoint.fromLocalInstance(client, local).getRegionID() == 12867;
	}

	private void swapMenuEntry(int index, MenuEntry menuEntry)
	{
		final String option = Text.removeTags(menuEntry.getOption());
		final String target = Text.removeTags(menuEntry.getTarget());

		// Swap the "Value" option with "Buy-1" for the given target if it's not off
		if (option.equals("Value") && !config.supplyChestMES().toString().equals("Value"))
		{
			if (TOB_CHEST_TARGETS.contains(target))
			{
				swap(option, target, index);
			}
		}
	}

	private void swap(String option, String target, int index)
	{
		MenuEntry[] menuEntries = client.getMenuEntries();

		int thisIndex = findIndex(menuEntries, index, option, target);
		int optionIdx = findIndex(menuEntries, thisIndex, config.supplyChestMES().toString(), target);

		if (thisIndex >= 0 && optionIdx >= 0)
		{
			swap(optionIndexes, menuEntries, optionIdx, thisIndex);
		}
	}

	private int findIndex(MenuEntry[] entries, int limit, String option, String target)
	{
		List<Integer> indexes = optionIndexes.get(option);

		// We want the last index which matches the target, as that is what is top-most on the menu
		for (int i = indexes.size() - 1; i >= 0; i--)
		{
			int index = indexes.get(i);
			MenuEntry entry = entries[index];
			String entryTarget = Text.removeTags(entry.getTarget());

			// Limit to the last index which is prior to the current entry
			if (index <= limit && entryTarget.equals(target))
			{
				return index;
			}
		}

		return -1;
	}

	private void swap(ArrayListMultimap<String, Integer> optionIndexes, MenuEntry[] entries, int index1, int index2)
	{
		MenuEntry entry = entries[index1];
		entries[index1] = entries[index2];
		entries[index2] = entry;

		client.setMenuEntries(entries);

		// Rebuild option indexes
		optionIndexes.clear();
		int idx = 0;
		for (MenuEntry menuEntry : entries)
		{
			String option = Text.removeTags(menuEntry.getOption());
			optionIndexes.put(option, idx++);
		}
	}
}
