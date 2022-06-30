package com.tobqol.rooms.nylocas;

import com.tobqol.api.util.TheatreInputListener;
import com.tobqol.rooms.nylocas.commons.NyloSelectionBox;
import com.tobqol.rooms.nylocas.commons.NyloSelectionManager;
import com.tobqol.rooms.nylocas.config.NylocasRoleSelectionType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.ui.overlay.components.InfoBoxComponent;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

import com.google.common.collect.ImmutableList;
import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Region;
import com.tobqol.rooms.RoomHandler;
import com.tobqol.rooms.nylocas.commons.NyloBoss;
import com.tobqol.rooms.nylocas.commons.NylocasMap;

import static com.tobqol.api.game.Region.*;
import static com.tobqol.rooms.nylocas.commons.NylocasMap.*;

import javax.inject.Inject;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class NylocasHandler extends RoomHandler
{
	@Inject
	private NylocasSceneOverlay sceneOverlay;

	@Inject
	private SkillIconManager skillIconManager;

	@Getter
	private NyloSelectionManager nyloSelectionManager;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private TheatreInputListener theatreInputListener;

	@Getter
	private NylocasRoleSelectionType currentRoleSelection;

	@Getter
	private NyloBoss boss = null;

	@Getter
	private NyloBoss demiBoss = null;

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
	}

	@Override
	public void init()
	{
		currentRoleSelection = config.nyloRoleSelector();

		InfoBoxComponent box = new InfoBoxComponent();
		box.setImage(skillIconManager.getSkillImage(Skill.ATTACK));
		NyloSelectionBox nyloMeleeOverlay = new NyloSelectionBox(box);
		nyloMeleeOverlay.setSelected(currentRoleSelection.isMelee());

		box = new InfoBoxComponent();
		box.setImage(skillIconManager.getSkillImage(Skill.MAGIC));
		NyloSelectionBox nyloMageOverlay = new NyloSelectionBox(box);
		nyloMageOverlay.setSelected(currentRoleSelection.isMage());

		box = new InfoBoxComponent();
		box.setImage(skillIconManager.getSkillImage(Skill.RANGED));
		NyloSelectionBox nyloRangeOverlay = new NyloSelectionBox(box);
		nyloRangeOverlay.setSelected(currentRoleSelection.isRange());

		nyloSelectionManager = new NyloSelectionManager(nyloMeleeOverlay, nyloMageOverlay, nyloRangeOverlay);
		nyloSelectionManager.setHidden(currentRoleSelection.isOff());

	}

	@Override
	public void load()
	{
		currentRoleSelection = config.nyloRoleSelector();

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
			case "nyloHidePillars":
				clientThread.invokeLater(() ->
				{
					if (inRegion(client, Region.NYLOCAS) && client.getGameState() == GameState.LOGGED_IN)
					{
						if (config.nyloHidePillars())
						{
							sceneManager.removeTheseGameObjects(client.getPlane(), ImmutableList.of(NylocasMap.PILLAR_GO_ID));
						}
						else
						{
							client.setGameState(GameState.LOADING);
						}
					}
				});
				break;
			case "nyloRoleSelector":
			{
				NylocasRoleSelectionType selection = convertFromString(e.getNewValue());

				if (selection != null)
				{
					determineSelection(selection);
				}
			}
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOGGED_IN && active())
		{
			if (config.nyloHidePillars())
			{
				sceneManager.removeTheseGameObjects(client.getPlane(), ImmutableList.of(NylocasMap.PILLAR_GO_ID));
			}

			if (currentRoleSelection.isAnyOrOn())
			{
				nyloSelectionManager.setHidden(false);
			}
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

		if (isNpcFromName(npc, BOSS_NAME) && NylocasMap.matchesAnyMode(NylocasMap.BOSS_MELEE, id))
		{
			instance.lazySetMode(() -> NylocasMap.findMode(id));
			boss = NyloBoss.spawned(npc, instance.mode());
			softReset();
			return;
		}

		if (isNpcFromName(npc, DEMI_BOSS_NAME) && NylocasMap.matchesAnyMode(NylocasMap.DEMI_BOSS_MELEE, id))
		{
			instance.lazySetMode(() -> NylocasMap.findMode(id));
			demiBoss = NyloBoss.spawned(npc, instance.mode());
			return;
		}

		if (NylocasMap.matchesAnyMode(NylocasMap.PILLAR, id))
		{
			if (pillars.size() > 3)
			{
				pillars.clear();
			}

			pillars.putIfAbsent(npc, 100);
			return;
		}

		if (isNpcFromName(npc, MELEE_NAME) || isNpcFromName(npc, RANGE_NAME) || isNpcFromName(npc, MAGIC_NAME))
		{
			instance.lazySetMode(() -> NylocasMap.findMode(id));
			wavesMap.put(npc, 52);

			NPCComposition comp = npc.getTransformedComposition();
			if ((comp == null ? 1 : comp.getSize()) > 1)
			{
				bigsMap.put(npc, 1);
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
		isNpcFromName(npc, BOSS_NAME, n -> boss.changed());
		isNpcFromName(npc, DEMI_BOSS_NAME, n -> demiBoss.changed());
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

		if (isNpcFromName(npc, BOSS_NAME) && !NylocasMap.matchesAnyMode(NylocasMap.BOSS_DROPPING_MELEE, id))
		{
			reset();
			return;
		}

		if (isNpcFromName(npc, DEMI_BOSS_NAME) && !NylocasMap.matchesAnyMode(NylocasMap.DEMI_BOSS_DROPPING_MELEE, id))
		{
			demiBoss = null;
			return;
		}

		pillars.remove(npc);
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded e)
	{
		if (!active() || !config.nyloWavesRecolorMenu() || wavesMap.isEmpty())
		{
			return;
		}

		if (e.getOption().equals("Attack"))
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

			if (target.startsWith(MELEE_NAME) || id == NylocasMap.DEMI_BOSS_MELEE.hm())
			{
				color = darker ? MELEE_COLOR.darker() : MELEE_COLOR;
			}
			else if (target.startsWith(RANGE_NAME) || id == NylocasMap.DEMI_BOSS_RANGE.hm())
			{
				color = darker ? RANGE_COLOR.darker() : RANGE_COLOR;
			}
			else if (target.startsWith(MAGIC_NAME) || id == NylocasMap.DEMI_BOSS_MAGIC.hm())
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

		if (NylocasMap.isBigNylo(npc.getId()))
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

	public void determineSelection(NylocasRoleSelectionType selection)
	{
		NylocasRoleSelectionType current = currentRoleSelection;
		NylocasRoleSelectionType updated = current == selection ? NylocasRoleSelectionType.ON : selection;

		if (active())
		{
			if (current == NylocasRoleSelectionType.OFF && updated != NylocasRoleSelectionType.OFF)
			{
				nyloSelectionManager.setHidden(false);
			}
			else if (updated == NylocasRoleSelectionType.OFF)
			{
				nyloSelectionManager.setHidden(true);
			}
		}
		config.setNyloRoleSelector(updated);
		currentRoleSelection = updated;

		nyloSelectionManager.getMage().setSelected(updated == NylocasRoleSelectionType.MAGE ? true : false);
		nyloSelectionManager.getMelee().setSelected(updated == NylocasRoleSelectionType.MELEE ? true : false);
		nyloSelectionManager.getRange().setSelected(updated == NylocasRoleSelectionType.RANGE ? true : false);
	}

	public NylocasRoleSelectionType convertFromString(String string)
	{
		if (string.equals(NylocasRoleSelectionType.OFF.toString()))
		{
			return NylocasRoleSelectionType.OFF;
		}
		else if (string.equals(NylocasRoleSelectionType.ON.toString()))
		{
			return NylocasRoleSelectionType.ON;
		}
		else if (string.equals(NylocasRoleSelectionType.MAGE.toString()))
		{
			return NylocasRoleSelectionType.MAGE;
		}
		else if (string.equals(NylocasRoleSelectionType.MELEE.toString()))
		{
			return NylocasRoleSelectionType.MELEE;
		}
		else if (string.equals(NylocasRoleSelectionType.RANGE.toString()))
		{
			return NylocasRoleSelectionType.RANGE;
		}

		log.debug("Could not determine what selection was intended. {}", string);
		return null;
	}
}
