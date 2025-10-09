/*
 * Copyright (c) 2022, Damen <gh: damencs>
 * Copyright (c) 2022, WLoumakis <gh: WLoumakis> - Portions of "MES Options"
 * Copyright (c) 2021, BickusDiggus <gh: BickusDiggus> - Portions of "Loot Reminder"
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
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.tobqol.api.game.Instance;
import com.tobqol.api.game.RaidConstants;
import com.tobqol.api.game.Region;
import com.tobqol.api.util.CustomChatClient;
import com.tobqol.config.SupplyChestPreference;
import com.tobqol.loottracking.LootItems;
import com.tobqol.loottracking.LootTrackingHandler;
import com.tobqol.loottracking.LootTrackingMemory;
import com.tobqol.rooms.RemovableOverlay;
import com.tobqol.rooms.RoomHandler;
import com.tobqol.rooms.bloat.BloatHandler;
import com.tobqol.rooms.maiden.MaidenHandler;
import com.tobqol.rooms.nylocas.NylocasHandler;
import com.tobqol.rooms.sotetseg.SotetsegHandler;
import com.tobqol.rooms.verzik.VerzikHandler;
import com.tobqol.rooms.xarpus.XarpusHandler;
import com.tobqol.timetracking.RoomDataHandler;
import com.tobqol.timetracking.TotalTimeInfoBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.chat.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ChatInput;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static com.tobqol.rooms.bloat.commons.BloatConstants.BOSS_IMAGE;

@PluginDescriptor(
		name = "ToB QoL",
		description = "Theatre of Blood Quality of Life Enhancement Features to be used throughout a raid",
		tags = { "tob", "tobqol", "of", "blood", "maiden", "bloat", "nylo", "nylocas", "sotetseg", "xarpus", "verzik", "combat", "bosses", "pvm", "pve", "damen" },
		enabledByDefault = true,
		loadInSafeMode = false
)
@Slf4j
public class TheatreQOLPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	public OverlayManager overlayManager;

	@Inject
	private EventManager eventManager;

	@Inject
	protected ItemManager itemManager;

	@Inject
	@Getter
	private InstanceService instanceService;

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

	@Inject
	public InfoBoxManager infoBoxManager;

	@Inject
	public ChatMessageManager chatMessageManager;

	@Inject
	public ConfigManager configManager;

	@Inject
	public Gson gson;

	@Inject
	public ChatCommandManager chatCommandManager;

	@Inject
	private CustomChatClient chatClient;

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

	@Getter
	private RoomDataHandler dataHandler;

	@Getter
	private LootTrackingHandler lootTrackingHandler;

	private TotalTimeInfoBox totalTimeInfoBox;

	private boolean darknessHidden;

	@Getter
	private GameObject entrance;

	@Getter
	private GameObject lootChest;

	@Getter
	private boolean chestHasLoot = false;
	private boolean chestViewed = false;

	private final ArrayListMultimap<String, Integer> optionIndexes = ArrayListMultimap.create();

	@Getter
	public Font pluginFont;

	@Getter
	public Font instanceTimerFont;

	@Getter
	public int previousRegion;

	@Inject
	private ScheduledExecutorService executor;

	@Override
	public void configure(Binder binder)
	{
		binder.bind(Instance.class).to(com.tobqol.InstanceService.class);
	}

	@Override
	protected void startUp()
	{
		dataHandler = new RoomDataHandler(client, this, config);
		dataHandler.load();

		lootTrackingHandler = new LootTrackingHandler(this, config, configManager, gson);
		lootTrackingHandler.load();

		buildFont(false); // build standard font
		buildFont(true); // build instance timer font

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

		chatCommandManager.registerCommandAsync(RaidConstants.TOB_DRY_COMMAND, this::getDryStreak, this::submitData);
		chatCommandManager.registerCommandAsync(RaidConstants.TOB_DRY_STREAK_COMMAND, this::getDryStreak, this::submitData);
		chatCommandManager.registerCommandAsync(RaidConstants.TOB_LAST_COMMAND, this::getLastItem, this::submitData);
		chatCommandManager.registerCommandAsync(RaidConstants.TOB_LAST_ITEM_COMMAND, this::getLastItem, this::submitData);
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

		dataHandler.unload();

		chatCommandManager.unregisterCommand(RaidConstants.TOB_DRY_COMMAND);
		chatCommandManager.unregisterCommand(RaidConstants.TOB_DRY_STREAK_COMMAND);
		chatCommandManager.unregisterCommand(RaidConstants.TOB_LAST_COMMAND);
		chatCommandManager.unregisterCommand(RaidConstants.TOB_LAST_ITEM_COMMAND);
	}

	void reset(boolean global)
	{
		dataHandler.getData().clear();

		if (rooms != null)
		{
			for (RoomHandler room : rooms)
			{
				room.reset();
				log.debug("Resetting {}", room.getClass().getSimpleName());
			}
		}

		// Remove total time infobox when leaving raid or resetting
		if (instanceService.getRaidStatus() <= 1)
		{
			removeTotalTimeInfoBox();
		}

		if (global)
		{
			darknessHidden = false;
			hideDarkness(false);

			entrance = null;
			lootChest = null;
			chestHasLoot = false;
			chestViewed = false;
			client.clearHintArrow();

			lootTrackingHandler.reset();
			instanceService.reset();
			eventManager.getInstance().reset();
			dataHandler.resetTotalTime();
			removeTotalTimeInfoBox();
		}
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

		switch (e.getKey())
		{
			case "lightUp":
			{
				hideDarkness(Boolean.valueOf(e.getNewValue()));
			}

			case "lootReminder":
			{
				if (client.hasHintArrow() && Boolean.valueOf(e.getNewValue()) == false)
				{
					client.clearHintArrow();
				}
			}

			case "fontType":
			case "fontSize":
			case "fontStyle":
			{
				buildFont(false);
			}

			case "instanceTimerSize":
			{
				buildFont(true);
			}

			case "displayTimeSplits":
			{
				dataHandler.updateHiddenItems(!Boolean.valueOf(e.getNewValue()));
				break;
			}
		}
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if ((instanceService.getCurrentRegion() != instanceService.getPreviousRegion()))
		{
			if ((instanceService.getCurrentRegion().isSotetsegUnderworld() && instanceService.getPreviousRegion().isSotetseg()
				|| (instanceService.getCurrentRegion().isMaiden() && instanceService.getPreviousRegion().isUnknown())))
			{
				return;
			}

			dataHandler.getData().clear();
			instanceService.setPreviousRegion(instanceService.getCurrentRegion());
		}

		if (((isInVerSinhaza() && config.lightUp()) || (isInSotetseg() && config.hideSotetsegWhiteScreen())) && !darknessHidden)
		{
			hideDarkness(true);
		}
		else if ((!isInVerSinhaza() && !isInSotetseg()) && darknessHidden)
		{
			hideDarkness(false);
		}

		if (isInVerSinhaza())
		{
			// Determine if chest has loot and draw an arrow overhead
			if (lootChest != null && Objects.requireNonNull(getObjectComposition(lootChest.getId())).getId() == RaidConstants.TOB_BANK_CHEST_UNLOOTED && !chestHasLoot)
			{
				chestHasLoot = true;
				if (config.lootReminder())
				{
					client.setHintArrow(lootChest.getWorldLocation());
				}
			}

			// Clear the arrow if the loot is taken
			if (lootChest != null && Objects.requireNonNull(getObjectComposition(lootChest.getId())).getId() == RaidConstants.TOB_BANK_CHEST_LOOTED && chestHasLoot)
			{
				chestHasLoot = false;
				client.clearHintArrow();

				// Try to reset here as a backup
				if (lootTrackingHandler.isLootHandled())
				{
					lootTrackingHandler.reset();
					chestViewed = false;
				}
			}

			if (chestViewed)
			{
				chestViewed = false;
				lootTrackingHandler.reset();
				instanceService.reset();
				eventManager.getInstance().reset();
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
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == InterfaceID.TOB_REWARD)
		{
			if (!chestViewed && lootTrackingHandler.isChestsHandled() && !lootTrackingHandler.isLootHandled())
			{
				final ItemContainer container = client.getItemContainer(InventoryID.THEATRE_OF_BLOOD_CHEST);

				if (container != null)
				{
					final Collection<ItemStack> items = Arrays.stream(container.getItems())
							.filter(item -> item.getId() > 0)
							.map(item -> new ItemStack(item.getId(), item.getQuantity()))
							.collect(Collectors.toList());

					items.forEach(item ->
					{
						if (LootItems.getItemLookup().containsKey(item.getId()))
						{
							lootTrackingHandler.processLootItem(LootItems.getItemLookup().get(item.getId()));
						}
					});

					chestViewed = true;
				}
			}
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		int objectId = event.getGameObject().getId();

		switch (objectId)
		{
			case RaidConstants.TOB_BANK_CHEST:
				lootChest = event.getGameObject();
				break;
			case RaidConstants.TOB_ENTRANCE:
				entrance = event.getGameObject();
				break;
		}

		if (RaidConstants.LOOT_ROOM_ALL_CHEST_IDS.contains(objectId) && instanceService.getRaiders().contains(client.getLocalPlayer().getName().toLowerCase()))
		{
			int imposterId = client.getObjectDefinition(objectId).getImpostor().getId();
			log.debug("chest spawned: {}, imposterId: {}", objectId, imposterId);

			if (imposterId > 0)
			{
				lootTrackingHandler.handleChest(imposterId);
			}
			else
			{
				lootTrackingHandler.handleChest(objectId);
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.HOPPING)
		{
			hideDarkness(false);
		}

		if (event.getGameState() == GameState.LOGGED_IN)
		{
			// Upon login, create a memory for the account if it does not already exist.
			getLootTrackingHandler().getExistingMemory();
			log.debug("tobqol: logged in memory check - memory: {}", getLootTrackingHandler().getExistingMemory());
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (instanceService.isInRaid() && event.getType().equals(ChatMessageType.GAMEMESSAGE) && event.getMessage().contains("You have failed. The vampyres take pity on you and allow you to try again."))
		{
			reset(false);
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
		Widget darkness = client.getWidget(28 << 16 | 1);
		if (darkness != null)
		{
			darknessHidden = hide;
			darkness.setHidden(hide);
		}
	}

	public boolean isInVerSinhaza()
	{
		return RaidConstants.VER_SINHAZA_REGIONS.contains(client.getLocalPlayer().getWorldLocation().getRegionID());
	}

	public boolean isInSotetseg()
	{
		LocalPoint local = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
		int region = WorldPoint.fromLocalInstance(client, local).getRegionID();
		return region == Region.SOTETSEG.regionId() || region == Region.SOTETSEG_MAZE.regionId();
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
			if (RaidConstants.TOB_CHEST_TARGETS.contains(target))
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

	private Font buildFont(boolean timer)
	{
		if (timer)
		{
			return instanceTimerFont = new Font(pluginFont.getName(), pluginFont.getStyle(), config.instanceTimerSize());
		}
		else
		{
			String font = null;
			int style = config.fontStyle().getValue();

			switch (config.fontType().getName())
			{
				case "RS Regular":
					font = FontManager.getRunescapeFont().getName();
					style = FontManager.getRunescapeFont().getStyle();
					break;

				case "RS Bold":
					font = FontManager.getRunescapeBoldFont().getName();
					style = FontManager.getRunescapeBoldFont().getStyle();
					break;

				case "RS Small":
					font = FontManager.getRunescapeSmallFont().getName();
					style = FontManager.getRunescapeSmallFont().getStyle();
					break;
			}

			return pluginFont = new Font(font == null ? config.fontStyle().getStyle() : font, style, font == null ? config.fontSize() : 16);
		}
	}

	public boolean hasSalve()
	{
		return containsSalve(client.getItemContainer(InventoryID.INVENTORY)) ||
				containsSalve(client.getItemContainer(InventoryID.EQUIPMENT));
	}

	private boolean containsSalve(ItemContainer container)
	{
		if (container != null)
		{
			int[] salveItemIds = {
					ItemID.SALVE_AMULET, ItemID.SALVE_AMULET_E, ItemID.SALVE_AMULETI,
					ItemID.SALVE_AMULETEI,ItemID.SALVE_AMULETEI_25278,ItemID.SALVE_AMULETEI_26782,
					ItemID.SALVE_AMULETI_25250, ItemID.SALVE_AMULETI_26763
			};

			for (int id : salveItemIds)
				if (container.contains(id))
					return true;
		}

		return false;
	}

	private static final int QUIVER_ITEM_COUNT = 4141;
	private static final int QUIVER_ITEM_ID = 4142;

	public boolean hasAmmo()
	{
		// Get quiver ammo details from VarPlayers
		final int quiverAmmoId = client.getVarpValue(VarPlayer.DIZANAS_QUIVER_ITEM_ID);
		final int quiverAmmoCount = client.getVarpValue(VarPlayer.DIZANAS_QUIVER_ITEM_COUNT);

		// Check the ammo slot in the equipment container
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment != null)
		{
			Item ammoItem = equipment.getItem(EquipmentInventorySlot.AMMO.getSlotIdx());
			if (ammoItem != null && ammoItem.getId() > -1)
			{
				return true; // Ammo is equipped
			}
		}

		// Check the quiver contents using VarPlayers
		int quiverItemId = client.getVarpValue(QUIVER_ITEM_ID);
		int quiverItemQuantity = client.getVarpValue(QUIVER_ITEM_COUNT);

		if (quiverItemId > -1 && quiverItemQuantity > 0)
		{
			return true; // Ammo is in the quiver
		}

		return false; // No ammo found in either slot
	}

	public boolean hasBadSurgePotionDose()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null)
			return false;

		int highestDoseFound = 0;
		// get highest dose of surge potion
		for (Item item : inventory.getItems())
		{
			if (item.getId() == net.runelite.api.gameval.ItemID._1DOSESURGE)
				if (highestDoseFound <= 1)
					highestDoseFound = 1;
			if (item.getId() == net.runelite.api.gameval.ItemID._2DOSESURGE)
				if (highestDoseFound <= 2)
					highestDoseFound = 2;
			if (item.getId() == net.runelite.api.gameval.ItemID._3DOSESURGE)
				if (highestDoseFound <= 3)
					highestDoseFound = 3;
			if (item.getId() == net.runelite.api.gameval.ItemID._4DOSESURGE)
					highestDoseFound = 4;
		}

		int wantedDose = config.surgePotionReminder().getDose();
		return wantedDose > highestDoseFound;
	}


	public String getSpellbook()
	{
		int spellbookId = client.getVarbitValue(4070);

		switch (spellbookId)
		{
			case 0: return "Standard";
			case 1: return "Ancient";
			case 2: return "Lunar";
			case 3: return "Arceuus";
			default: return "n/a";
		}
	}

	public void queueChatMessage(String message)
	{
		chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.GAMEMESSAGE).value(message).build());
	}

	private LootTrackingMemory getCommandData(ChatMessage chatMessage, String s)
	{
		final String player;
		if (chatMessage.getType().equals(ChatMessageType.PRIVATECHATOUT))
		{
			player = client.getLocalPlayer().getName();
		}
		else
		{
			player = Text.sanitize(chatMessage.getName());
		}

		try
		{
			return chatClient.getMemory(player);
		}
		catch (IOException ex)
		{
			log.debug("unable to lookup player memory", ex);
			return null;
		}
	}

	private void getDryStreak(ChatMessage chatMessage, String s)
	{
		LootTrackingMemory memory = getCommandData(chatMessage, s);

		if (memory == null)
			return;

		String message = new ChatMessageBuilder()
				.append(new Color(128, 0, 128), "TOB Dry Streak")
				.append(ChatColorType.HIGHLIGHT)
				.append(" - Personal: ")
				.append(Color.RED, Integer.toString(memory.getCountSincePersonal()))
				.append(ChatColorType.HIGHLIGHT)
				.append(" / Team: ")
				.append(Color.RED, Integer.toString(memory.getCountSinceOther()))
				.build();

		final MessageNode messageNode = chatMessage.getMessageNode();
		messageNode.setRuneLiteFormatMessage(message);
		client.refreshChat();
	}

	private void getLastItem(ChatMessage chatMessage, String s)
	{
		LootTrackingMemory memory = getCommandData(chatMessage, s);

		if (memory == null)
			return;

		String message = new ChatMessageBuilder()
				.append(new Color(128, 0, 128), "TOB Last Item: ")
				.append(Color.RED, memory.getLastPersonalItem())
				.build();

		final MessageNode messageNode = chatMessage.getMessageNode();
		messageNode.setRuneLiteFormatMessage(message);
		client.refreshChat();
	}

	private boolean submitData(ChatInput chatInput, String value)
	{
		final String playerName = client.getLocalPlayer().getName();

		LootTrackingMemory memory = getLootTrackingHandler().getExistingMemory();

		if (memory == null)
			return false;

		executor.execute(() ->
		{
			try
			{
				chatClient.submitMemory(playerName, memory);
			}
			catch (Exception ex)
			{
				log.warn("unable to submit memory", ex);
			}
			finally
			{
				chatInput.resume();
			}
		});

		return true;
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted command)
	{
		if (command.getCommand().equalsIgnoreCase("resettobdry"))
		{
			LootTrackingMemory memory = getLootTrackingHandler().getExistingMemory();
			lootTrackingHandler.saveMemory(new LootTrackingMemory(0, memory.getLastPersonalItem(), 0));
			queueChatMessage("You have successfully reset your TOB Loot Tracking Dry Streak.");
		}
	}

	public void updateTotalTimeInfoBox()
	{
		if (!config.displayTotalTime())
		{
			removeTotalTimeInfoBox();
			return;
		}

		// Add current room time to total if room is completed
		dataHandler.addRoomTimeToTotal();

		// Create or update the total time infobox
		removeTotalTimeInfoBox();
		
		if (dataHandler.getTotalTime() > 0)
		{
			String totalTimeFormatted = com.tobqol.timetracking.RoomInfoUtil.formatTime(dataHandler.getTotalTime());
			String tooltip = "Challenge Time";
			
			totalTimeInfoBox = new TotalTimeInfoBox(
					itemManager.getImage(ItemID.WATCH),
				this,
				config,
				totalTimeFormatted,
				tooltip
			);
			
			infoBoxManager.addInfoBox(totalTimeInfoBox);
		}
	}

	private void removeTotalTimeInfoBox()
	{
		if (totalTimeInfoBox != null)
		{
			infoBoxManager.removeInfoBox(totalTimeInfoBox);
			totalTimeInfoBox = null;
		}
	}
}