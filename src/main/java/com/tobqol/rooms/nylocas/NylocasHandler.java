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
package com.tobqol.rooms.nylocas;

import com.google.common.collect.ImmutableList;
import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Region;
import com.tobqol.api.util.TheatreInputListener;
import com.tobqol.rooms.RoomHandler;
import com.tobqol.rooms.nylocas.commons.NyloBoss;
import com.tobqol.rooms.nylocas.commons.NyloSelectionBox;
import com.tobqol.rooms.nylocas.commons.NyloSelectionManager;
import com.tobqol.rooms.nylocas.commons.NylocasConstants;
import com.tobqol.timetracking.RoomDataHandler;
import com.tobqol.timetracking.RoomDataItem;
import com.tobqol.timetracking.RoomInfoBox;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.ui.overlay.components.InfoBoxComponent;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static com.tobqol.api.game.Region.NYLOCAS;
import static com.tobqol.api.game.Region.inRegion;
import static com.tobqol.rooms.nylocas.commons.NylocasConstants.*;
import static com.tobqol.timetracking.RoomInfoUtil.createInfoBox;
import static com.tobqol.timetracking.RoomInfoUtil.formatTime;

@Slf4j
public class NylocasHandler extends RoomHandler
{
	@Inject
	private NylocasSceneOverlay sceneOverlay;

	private RoomDataHandler dataHandler;

	@Inject
	private SkillIconManager skillIconManager;

	@Getter
	private NyloSelectionManager nyloSelectionManager;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private TheatreInputListener theatreInputListener;

	@Getter
	private boolean displayRoleSelector;

	@Getter
	@Setter
	private boolean displayRoleMage;

	@Getter
	@Setter
	private boolean displayRoleMelee;

	@Getter
	@Setter
	private boolean displayRoleRange;

	@Getter
	private boolean displayInstanceTimer;

	private RoomInfoBox nylocasInfoBox;

	private int wave = 0;
	private boolean waveSpawned = false;

	@Getter
	private NyloBoss boss = null;

	@Getter
	private NyloBoss demiBoss = null;
	private int demiCount = 0;

	@Getter
	private final Map<NPC, Integer> pillars = new HashMap<>();

	@Getter
	private final Map<NPC, Integer> wavesMap = new HashMap<>();

	@Getter
	private final Map<NPC, Integer> bigsMap = new HashMap<>();

	@Getter
	private final Map<NPC, Integer> splitsMap = new HashMap<>();

	@Inject
	protected NylocasHandler(TheatreQOLPlugin plugin, TheatreQOLConfig config)
	{
		super(plugin, config);
		setRoomRegion(NYLOCAS);

		dataHandler = plugin.getDataHandler();
	}

	@Override
	public void init()
	{
		displayRoleSelector = config.displayNyloRoleSelector();
		displayRoleMage = config.nyloRoleSelectedMage();
		displayRoleMelee = config.nyloRoleSelectedMelee();
		displayRoleRange = config.nyloRoleSelectedRange();

		InfoBoxComponent box = new InfoBoxComponent();
		box.setImage(skillIconManager.getSkillImage(Skill.ATTACK));
		NyloSelectionBox nyloMeleeOverlay = new NyloSelectionBox(box);
		nyloMeleeOverlay.setSelected(displayRoleMelee);

		box = new InfoBoxComponent();
		box.setImage(skillIconManager.getSkillImage(Skill.MAGIC));
		NyloSelectionBox nyloMageOverlay = new NyloSelectionBox(box);
		nyloMageOverlay.setSelected(displayRoleMage);

		box = new InfoBoxComponent();
		box.setImage(skillIconManager.getSkillImage(Skill.RANGED));
		NyloSelectionBox nyloRangeOverlay = new NyloSelectionBox(box);
		nyloRangeOverlay.setSelected(displayRoleRange);

		nyloSelectionManager = new NyloSelectionManager(config, nyloMeleeOverlay, nyloMageOverlay, nyloRangeOverlay);
		nyloSelectionManager.setHidden(!displayRoleSelector);
	}

	@Override
	public void load()
	{
		overlayManager.add(sceneOverlay);
		startupNyloOverlay();
	}

	@Override
	public void unload()
	{
		overlayManager.remove(sceneOverlay);
		shutdownNyloOverlay();
		reset();
	}

	@Override
	public void reset()
	{
		boss = null;
		demiBoss = null;
		softReset();
		displayInstanceTimer = config.nyloInstanceTimer();

		if (instance.getRaidStatus() <= 1)
		{
			wave = 0;
			waveSpawned = false;
			demiCount = 0;
			infoBoxManager.removeInfoBox(nylocasInfoBox);
		}
	}

	private void softReset()
	{
		pillars.clear();
		wavesMap.clear();
		bigsMap.clear();
		splitsMap.clear();
	}

	@Override
	public boolean active()
	{
		return instance.getCurrentRegion().isNylocas();
	}

	private void startupNyloOverlay()
	{
		mouseManager.registerMouseListener(theatreInputListener);

		if (nyloSelectionManager != null)
		{
			overlayManager.add(nyloSelectionManager);
			nyloSelectionManager.setHidden(true);
		}
	}

	private void shutdownNyloOverlay() {
		mouseManager.unregisterMouseListener(theatreInputListener);

		if (nyloSelectionManager != null)
		{
			overlayManager.remove(nyloSelectionManager);
			nyloSelectionManager.setHidden(true);
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged e)
	{
		if (!e.getGroup().equalsIgnoreCase(TheatreQOLConfig.GROUP_NAME))
		{
			return;
		}

		switch (e.getKey())
		{
			case "nyloHideObjects":
				clientThread.invokeLater(() ->
				{
					if (inRegion(client, Region.NYLOCAS) && client.getGameState() == GameState.LOGGED_IN)
					{
						sceneManager.refreshScene();

						if (config.nyloHideObjects().isAnyOrAll())
						{
							hideRoomObjects(config.nyloHideObjects().toString());
						}
						else
						{
							client.setGameState(GameState.LOADING);
						}
					}
				});
				break;
			case "displayNyloRoleSelector":
			{
				displayRoleSelector = Boolean.valueOf(e.getNewValue());
				nyloSelectionManager.setHidden(!displayRoleSelector);
				break;
			}
			case "nyloInstanceTimer":
			{
				displayInstanceTimer = Boolean.valueOf(e.getNewValue());
				break;
			}
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOGGED_IN && active())
		{
			hideRoomObjects(config.nyloHideObjects().toString());

			nyloSelectionManager.setHidden(!displayRoleSelector);
			displayInstanceTimer = config.nyloInstanceTimer();
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned e)
	{
		if (!active())
		{
			return;
		}

		NPC npc = e.getNpc();
		int id = npc.getId();

		if (NylocasConstants.matchesAnyMode(BOSS_DROPPING_MELEE, id))
		{
			dataHandler.getData().add(new RoomDataItem("Boss", dataHandler.getTime(), 6, !config.displayTimeSplits(), "Cleanup"));
			return;
		}

		if (isNpcFromName(npc, BOSS_NAME) && NylocasConstants.matchesAnyMode(NylocasConstants.BOSS_MELEE, id))
		{
			instance.lazySetMode(() -> NylocasConstants.findMode(id));
			boss = NyloBoss.spawned(npc, instance.mode());
			softReset();
			return;
		}

		if (isNpcFromName(npc, DEMI_BOSS_NAME) && NylocasConstants.matchesAnyMode(NylocasConstants.DEMI_BOSS_MELEE, id))
		{
			instance.lazySetMode(() -> NylocasConstants.findMode(id));
			demiBoss = NyloBoss.spawned(npc, instance.mode());

			demiCount++;
			dataHandler.getData().add(new RoomDataItem("Demi " + demiCount, dataHandler.getTime(), demiCount, true, demiCount > 1 ? "Demi " + (demiCount - 1) : ""));
			return;
		}

		if (NylocasConstants.matchesAnyMode(NylocasConstants.PILLAR, id))
		{
			if (pillars.size() > 3)
			{
				pillars.clear();
			}

			pillars.putIfAbsent(npc, 100);

			if (!dataHandler.Find("Starting Tick").isPresent())
			{
				dataHandler.getData().add(new RoomDataItem("Starting Tick", client.getTickCount(), true));
				dataHandler.setShouldTrack(true);
			}
			return;
		}

		if (isNpcFromName(npc, MELEE_NAME) || isNpcFromName(npc, RANGE_NAME) || isNpcFromName(npc, MAGIC_NAME))
		{
			instance.lazySetMode(() -> NylocasConstants.findMode(id));
			wavesMap.put(npc, 52);

			NPCComposition comp = npc.getTransformedComposition();
			if ((comp == null ? 1 : comp.getSize()) > 1)
			{
				bigsMap.put(npc, 1);
			}

			if (displayInstanceTimer)
			{
				displayInstanceTimer = false;
			}

			// TODO -> Eventually convert this to track split data for nylocas spawns for post-room splits
			if (!waveSpawned)
			{
				WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, npc.getLocalLocation());
				Point spawnPoint = new Point(worldPoint.getRegionX(), worldPoint.getRegionY());

				if (NYLOCAS_VALID_SPAWNS.contains(spawnPoint))
				{
					wave++;
					waveSpawned = true;

					if (wave == NYLOCAS_WAVES_TOTAL)
					{
						dataHandler.getData().add(new RoomDataItem("Waves", dataHandler.getTime(), 4, !config.displayTimeSplits(), dataHandler.Find("Demi 3").isPresent() ? "Demi 3" : ""));
					}
				}
			}
		}
	}

	@Subscribe
	private void onNpcChanged(NpcChanged e)
	{
		if (!active() && boss == null && demiBoss == null)
		{
			return;
		}

		NPC npc = e.getNpc();

		if (npc != null)
		{
			if (boss != null)
			{
				isNpcFromName(npc, BOSS_NAME, n -> boss.changed());
			}
			if (demiBoss != null)
			{
				isNpcFromName(npc, DEMI_BOSS_NAME, n -> demiBoss.changed());
			}
		}
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned e)
	{
		if (!active())
		{
			return;
		}

		NPC npc = e.getNpc();
		int id = npc.getId();

		if (isNpcFromName(npc, BOSS_NAME) && !NylocasConstants.matchesAnyMode(NylocasConstants.BOSS_DROPPING_MELEE, id))
		{
			reset();
			return;
		}

		if (isNpcFromName(npc, DEMI_BOSS_NAME) && !NylocasConstants.matchesAnyMode(NylocasConstants.DEMI_BOSS_DROPPING_MELEE, id))
		{
			demiBoss = null;
			return;
		}

		pillars.remove(npc);
		wavesMap.remove(npc);

		if (wavesMap.isEmpty() && wave == NYLOCAS_WAVES_TOTAL && !dataHandler.Find("Cleanup").isPresent())
		{
			dataHandler.getData().add(new RoomDataItem("Cleanup", dataHandler.getTime(), 5, false, "Waves"));
		}
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded e)
	{
		if (!active() || !config.nyloWavesRecolorMenu() || wavesMap.isEmpty())
		{
			return;
		}

		if (e.getOption().equals("Attack") || e.getType() == MenuAction.WIDGET_TARGET_ON_NPC.getId())
		{
			NPC npc = client.getCachedNPCs()[e.getIdentifier()];

			if (npc == null)
			{
				return;
			}

			String target = e.getTarget();
			MenuEntry[] entries = client.getMenuEntries();
			MenuEntry head = entries[entries.length - 1];

			boolean darker = config.nyloWavesRecolorBigsMenuDarker() && npc.getTransformedComposition() != null && npc.getTransformedComposition().getSize() > 1;
			int id = npc.getId();
			target = Text.removeTags(target);
			Color color = null;

			if (target.contains(MELEE_NAME) || id == NylocasConstants.DEMI_BOSS_MELEE.hm())
			{
				color = darker ? MELEE_COLOR.darker() : MELEE_COLOR;
			}
			else if (target.contains(RANGE_NAME) || id == NylocasConstants.DEMI_BOSS_RANGE.hm())
			{
				color = darker ? RANGE_COLOR.darker() : RANGE_COLOR;
			}
			else if (target.contains(MAGIC_NAME) || id == NylocasConstants.DEMI_BOSS_MAGIC.hm())
			{
				color = darker ? MAGIC_COLOR.darker() : MAGIC_COLOR;
			}

			if (color != null)
			{
				target = ColorUtil.prependColorTag(target, color);
			}

			head.setTarget(target);
			client.setMenuEntries(entries);
		}
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if (instance.isInRaid() && instance.getCurrentRegion().isNylocas())
		{
			if (instance.getRoomStatus() == 1 && !dataHandler.Find("Starting Tick").isPresent())
			{
				dataHandler.getData().add(new RoomDataItem("Starting Tick", client.getTickCount(), true, true));
				dataHandler.setShouldTrack(true);

				dataHandler.getData().add(new RoomDataItem("Room", dataHandler.getTime(), 99, false, "Boss"));
			}

			if (dataHandler.isShouldTrack() && !dataHandler.getData().isEmpty())
			{
				dataHandler.updateTotalTime();
			}
		}

		if (!active())
		{
			if (!nyloSelectionManager.isHidden())
			{
				nyloSelectionManager.setHidden(true);
			}
			return;
		}

		if (!pillars.isEmpty())
		{
			for (NPC pillar : pillars.keySet())
			{
				int ratio = pillar.getHealthRatio();

				if (ratio > -1)
				{
					pillars.replace(pillar, ratio);
				}
			}
		}

		if (!wavesMap.isEmpty())
		{
			wavesMap.values().removeIf(VALUE_IS_ZERO);
			wavesMap.replaceAll(DECREMENT_VALUE);
			waveSpawned = false;
		}

		if (!bigsMap.isEmpty())
		{
			bigsMap.entrySet().removeIf(entry ->
			{
				NPC big = entry.getKey();
				if (big.getHealthRatio() == 0 || entry.getValue() >= 52)
				{
					splitsMap.putIfAbsent(big, 0xFF);
					return true;
				}

				return false;
			});
		}

		if (!splitsMap.isEmpty())
		{
			splitsMap.values().removeIf(VALUE_IS_ZERO);
			splitsMap.replaceAll(DECREMENT_VALUE);
		}
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged e)
	{
		if (!active() || !(e.getActor() instanceof NPC))
		{
			return;
		}

		NPC npc = (NPC) e.getActor();

		if (NylocasConstants.isBigNylo(npc.getId()))
		{
			switch (npc.getAnimation())
			{
				case 7991:
				case 7998:
				case 8005:
					splitsMap.merge(npc, 5, (o, n) -> n);
					break;
				case 7992:
				case 8000:
				case 8006:
					splitsMap.merge(npc, 3, (o, n) -> n);
					break;
			}
		}
	}

	@Subscribe
	private void onGraphicsObjectCreated(GraphicsObjectCreated e)
	{
		if (!active() || !config.nyloLowDetail())
		{
			return;
		}

		GraphicsObject graphic = e.getGraphicsObject();
		int id = e.getGraphicsObject().getId();

		if ((id >= UNK_DESPAWN_GRAPHIC_1 && id <= MAGIC_SMALL_DESPAWN_GRAPHIC) || (id >= UNK_DESPAWN_GRAPHIC_2 && id <= UNK_DESPAWN_GRAPHIC_5))
		{
			graphic.setFinished(true);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (instance.getCurrentRegion() != NYLOCAS && event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String stripped = Text.removeTags(event.getMessage());

		if (NYLOCAS_WAVE.matcher(stripped).find())
		{
			dataHandler.setShouldTrack(false);
			dataHandler.Find("Room").get().setValue(dataHandler.getTime());

			if (config.displayRoomTimes().isInfobox())
			{
				buildInfobox();
			}

			if (config.displayRoomTimes().isChat())
			{
				sendChatTimes();
			}
		}
	}

	private void buildInfobox()
	{
		if (!dataHandler.getData().isEmpty())
		{
			String tooltip;

			if (!dataHandler.Find("Starting Tick").get().isException())
			{
				tooltip = "Waves - " + formatTime(dataHandler.FindValue("Waves")) + "</br>" +
						"Cleanup - " + formatTime(dataHandler.FindValue("Cleanup")) + formatTime(dataHandler.FindValue("Cleanup"), dataHandler.FindValue("Waves")) + "</br>" +
						"Boss - " + formatTime(dataHandler.FindValue("Boss")) + formatTime(dataHandler.FindValue("Boss"), dataHandler.FindValue("Cleanup")) + "</br>" +
						"Complete - " + formatTime(dataHandler.FindValue("Room")) + formatTime(dataHandler.FindValue("Room"), dataHandler.FindValue("Boss"));
			}
			else
			{
				tooltip = "Complete - " + formatTime(dataHandler.FindValue("Room")) + "*";
			}

			nylocasInfoBox = createInfoBox(plugin, config, itemManager.getImage(BOSS_IMAGE), "Nylocas", formatTime(dataHandler.FindValue("Room")), tooltip);
			infoBoxManager.addInfoBox(nylocasInfoBox);
		}
	}

	private void sendChatTimes()
	{
		if (!dataHandler.getData().isEmpty())
		{
			if (!dataHandler.Find("Starting Tick").get().isException())
			{
				enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
						.append(Color.RED, "Waves")
						.append(ChatColorType.NORMAL)
						.append(" - " + formatTime(dataHandler.FindValue("Waves")) + " - ")
						.append(Color.RED, "Cleanup")
						.append(ChatColorType.NORMAL)
						.append(" - " + formatTime(dataHandler.FindValue("Cleanup")) + formatTime(dataHandler.FindValue("Cleanup"), dataHandler.FindValue("Waves")) + " - ")
						.append(Color.RED, "Boss")
						.append(ChatColorType.NORMAL)
						.append(" - " + formatTime(dataHandler.FindValue("Boss")) + formatTime(dataHandler.FindValue("Boss"), dataHandler.FindValue("Cleanup"))));

				if (config.roomTimeValidation())
				{
					enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
							.append(Color.RED, "Nylocas - Room Complete")
							.append(ChatColorType.NORMAL)
							.append(" - " + formatTime(dataHandler.FindValue("Room")) + formatTime(dataHandler.FindValue("Room"), dataHandler.FindValue("Boss"))));
				}
			}
			else
			{
				if (config.roomTimeValidation())
				{
					enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
							.append(Color.RED, "Nylocas - Room Complete")
							.append(ChatColorType.NORMAL)
							.append(" - " + formatTime(dataHandler.FindValue("Room")) + "*"));
				}
			}
		}
	}

	private void hideRoomObjects(String option)
	{
		switch (option)
		{
			case "Pillars":
				sceneManager.removeTheseGameObjects(client.getPlane(), ImmutableList.of(PILLAR_GO_ID));
				break;
			case "Spectator Webs":
				sceneManager.removeTheseGameObjects(client.getPlane(), ImmutableList.of(SPECTATOR_WEB_1, SPECTATOR_WEB_2, SPECTATOR_WEB_3));
				break;
			case "Walls":
				sceneManager.removeTheseGameObjects(client.getPlane(), ImmutableList.of(WALL_1, WALL_2));
				break;
			case "Pillars and Webs":
				sceneManager.removeTheseGameObjects(client.getPlane(), ImmutableList.of(PILLAR_GO_ID, SPECTATOR_WEB_1, SPECTATOR_WEB_2, SPECTATOR_WEB_3));
				break;
			case "All":
				sceneManager.removeTheseGameObjects(client.getPlane(), ImmutableList.of(PILLAR_GO_ID, SPECTATOR_WEB_1, SPECTATOR_WEB_2, SPECTATOR_WEB_3, WALL_1, WALL_2));
				break;
		}
	}

	public boolean isAnyRole()
	{
		return displayRoleMage || displayRoleMelee || displayRoleRange;
	}
}
