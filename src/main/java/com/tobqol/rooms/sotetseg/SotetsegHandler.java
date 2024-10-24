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
package com.tobqol.rooms.sotetseg;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Region;
import com.tobqol.rooms.RoomHandler;
import com.tobqol.rooms.sotetseg.commons.MutableMaze;
import com.tobqol.rooms.sotetseg.commons.SotetsegNotification;
import com.tobqol.rooms.sotetseg.commons.SotetsegTable;
import com.tobqol.rooms.sotetseg.config.SotetsegProjectileTheme;
import com.tobqol.tracking.RoomDataHandler;
import com.tobqol.tracking.RoomDataItem;
import com.tobqol.tracking.RoomInfoBox;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.util.Text;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.awt.*;

import static com.tobqol.api.game.Region.SOTETSEG;
import static com.tobqol.rooms.sotetseg.commons.SotetsegConstants.*;
import static com.tobqol.rooms.sotetseg.commons.SotetsegTable.SOTETSEG_CLICKABLE;
import static com.tobqol.tracking.RoomInfoUtil.createInfoBox;
import static com.tobqol.tracking.RoomInfoUtil.formatTime;

@Slf4j
public class SotetsegHandler extends RoomHandler
{
	@Inject
	private SotetsegSceneOverlay sceneOverlay;

	private RoomDataHandler dataHandler;

	@Getter
	@CheckForNull
	private NPC sotetsegNpc = null;

	private RoomInfoBox sotetsegInfoBox;

	@Getter
	private boolean clickable = false;

	@Getter
	@CheckForNull
	private MutableMaze maze = null;

	@Getter
	@CheckForNull
	private GameObject portal = null;

	private boolean considerTeleport = true;

	private static Clip soundClip;
	private boolean deathBallSpawned = false;
	private int deathBallSafetyNet = 0;

	@Getter
	@Setter
	private boolean chosen = false;
	public int chosenTextTimeout;

	@Inject
	private WSClient wsClient;

	@Inject
	private PartyService party;

	@Inject
	protected SotetsegHandler(TheatreQOLPlugin plugin, TheatreQOLConfig config)
	{
		super(plugin, config);
		setRoomRegion(Region.SOTETSEG);

		dataHandler = plugin.getDataHandler();
	}

	@Override
	public void load()
	{
		overlayManager.add(sceneOverlay);
		soundClip = generateSoundClip("weewoo-hoyaa.wav", config.sotetsegSoundClipVolume());

		wsClient.registerMessage(SotetsegNotification.class);
	}

	@Override
	public void unload()
	{
		overlayManager.remove(sceneOverlay);
		soundClip.close();
		wsClient.unregisterMessage(SotetsegNotification.class);
		reset();
	}

	@Override
	public boolean active()
	{
		return instance.getCurrentRegion().isSotetseg() && sotetsegNpc != null && !sotetsegNpc.isDead();
	}

	@Override
	public void reset()
	{
		sotetsegNpc = null;
		clickable = false;
		maze = null;
		portal = null;
		considerTeleport = true;
		deathBallSpawned = false;
		deathBallSafetyNet = 0;
		chosen = false;
		chosenTextTimeout = 0;

		if (instance.getRaidStatus() <= 1)
		{
			infoBoxManager.removeInfoBox(sotetsegInfoBox);
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged e)
	{
		if (!e.getGroup().equals(TheatreQOLConfig.GROUP_NAME) || !instance.getCurrentRegion().isSotetsegUnderworld())
		{
			return;
		}

		switch (e.getKey())
		{
			case "sotetsegHideUnderworldRocks":
			{
				when(config.sotetsegHideUnderworldRocks(), this::hideUnderworldRocks, sceneManager::refreshScene);
				break;
			}

			case "sotetsegSoundClipVolume":
			{
				if (soundClip != null && config.sotetsegSoundClip())
				{
					FloatControl control = (FloatControl) soundClip.getControl(FloatControl.Type.MASTER_GAIN);

					if (control != null)
					{
						control.setValue((float)(config.sotetsegSoundClipVolume() / 2 - 45));
					}

					soundClip.setFramePosition(0);
					soundClip.start();
				}
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (instance.isInRaid() && instance.getCurrentRegion().isSotetseg())
		{
			if (instance.getRoomStatus() == 1 && !dataHandler.Find("Starting Tick").isPresent())
			{
				dataHandler.getData().add(new RoomDataItem("Starting Tick", client.getTickCount(), true, true));
				dataHandler.setShouldTrack(true);

				dataHandler.getData().add(new RoomDataItem("Room", dataHandler.getTime(), 99, false, "33%"));
			}

			if (dataHandler.isShouldTrack() && !dataHandler.getData().isEmpty())
			{
				dataHandler.updateTotalTime();
			}
		}

		if (!active())
		{
			return;
		}

		if (!considerTeleport)
		{
			considerTeleport = true;
		}

		if (deathBallSpawned && deathBallSafetyNet++ == 35)
		{
			deathBallSafetyNet = 0;
			deathBallSpawned = false;
		}

		Widget[] widgetsOfSotetseg = client.getWidget(28, 1).getChildren();
		if (!chosen && config.hideSotetsegWhiteScreen() && client.getWidget(28, 1) != null)
		{
			for (Widget widget : widgetsOfSotetseg)
			{
				if (!widget.getText().isEmpty())
				{
					if (widget.getText().contains("Sotetseg chooses you"))
					{
						chosen = true;
						widget.setText("");
					}
				}
			}
		}
		else if (chosen && chosenTextTimeout++ == 5)
		{
			for (Widget widget : widgetsOfSotetseg)
			{
				if (!widget.getText().isEmpty())
				{
					chosen = false;
					chosenTextTimeout = 0;
					widget.setText("");
				}
			}
		}
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (!active())
		{
			return;
		}

		when(config.sotetsegHideUnderworldRocks(), this::hideUnderworldRocks, null);
		when(config.sotetsegHideUnderworldTornado(), this::hideUnderworldTornado, null);
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned e)
	{
		isNpcFromName(e.getNpc(), BOSS_NAME, n ->
		{
			instance.lazySetMode(() -> SotetsegTable.findMode(n.getId()));
			sotetsegNpc = n;
			clickable = SotetsegTable.anyMatch(SOTETSEG_CLICKABLE, n.getId());
		});
	}

	@Subscribe
	private void onNpcChanged(NpcChanged e)
	{
		if (!active())
		{
			return;
		}

		isNpcFromName(e.getNpc(), BOSS_NAME, n ->
		{
			if (clickable = SotetsegTable.anyMatch(SOTETSEG_CLICKABLE, n.getId()))
			{
				if (!dataHandler.Find("Starting Tick").isPresent())
				{
					dataHandler.getData().add(new RoomDataItem("Starting Tick", client.getTickCount(), true));
					dataHandler.setShouldTrack(true);
				}

				log.debug("[{}] - Sotetseg Changed NPC IDs -> Clickable: {}", client.getTickCount(), clickable);
			}
		});
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned e)
	{
		if (!active() || client.getPlane() != 0)
		{
			return;
		}

		isNpcFromName(e.getNpc(), BOSS_NAME, n -> reset());
	}

	@Subscribe
	private void onGroundObjectSpawned(GroundObjectSpawned e)
	{
		if (!active())
		{
			return;
		}

		GroundObject obj = e.getGroundObject();

		if (SotetsegTable.isActiveMazeObject(obj))
		{
			if (maze == null)
			{
				maze = new MutableMaze(instance);
			}

			WorldPoint wp = WorldPoint.fromLocal(client, e.getTile().getLocalLocation());
			maze.addPoint(wp.getRegionX(), wp.getRegionY());
		}
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned e)
	{
		if (!active() || portal != null)
		{
			return;
		}

		GameObject obj = e.getGameObject();

		if (obj.getId() == SotetsegTable.MAZE_UNDERWORLD_PORTAL)
		{
			portal = obj;
		}

		if (UNDERWORLD_ROCKS.contains(obj.getId()))
		{
			when(config.sotetsegHideUnderworldRocks(), this::hideUnderworldRocks, null);
		}
	}

	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned e)
	{
		if (!active() || portal == null)
		{
			return;
		}

		if (e.getGameObject().getId() == SotetsegTable.MAZE_UNDERWORLD_PORTAL)
		{
			portal = null;
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved projectileMoved)
	{
		if (!instance.getCurrentRegion().isSotetseg())
		{
			return;
		}

		SotetsegProjectileTheme theme = config.getSotetsegProjectileTheme();

		Projectile projectile = projectileMoved.getProjectile();
		int projectileId = projectile.getId();

		if (projectileId == DEATH_BALL && !deathBallSpawned)
		{
			if (config.sotetsegSoundClip())
			{
				soundClip.setFramePosition(0);
				soundClip.start();
			}

			deathBallSpawned = true;
		}

		if (!theme.isDefault())
		{
			int replacement = -1;

			switch (projectileId)
			{
				case RANGE_ORB:
				{
					switch (theme)
					{
						case INFERNO:
							replacement = INFERNO_RANGE;
							break;

						case TOA:
							replacement = TOA_RANGE;
							break;
					}
					break;
				}
				case MAGIC_ORB:
				{
					switch (theme)
					{
						case INFERNO:
							replacement = INFERNO_MAGE;
							break;

						case TOA:
							replacement = TOA_MAGE;
							break;
					}
					break;
				}
				case DEATH_BALL:
				{
					if (config.themedDeathBall())
					{
						switch (theme)
						{
							case INFERNO:
								replacement = INFERNO_DEATH_BALL;
								break;

							case TOA:
								replacement = TOA_DEATH_BALL;
								break;
						}
					}
				}
			}

			if (replacement == -1)
			{
				return;
			}

			Projectile p = client.createProjectile(replacement,
					projectile.getFloor(),
					projectile.getX1(), projectile.getY1(),
					projectile.getHeight(),
					projectile.getStartCycle(), projectile.getEndCycle(),
					projectile.getSlope(),
					projectile.getStartHeight(), projectile.getEndHeight(),
					projectile.getInteracting(),
					projectile.getTarget().getX(), projectile.getTarget().getY());

			client.getProjectiles().addLast(p);
			projectile.setEndCycle(0);
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (active())
		{
			if (considerTeleport && event.getActor().getAnimation() == MAZE_TELE_ANIM)
			{
				boolean phase = dataHandler.Find("66%").isPresent();
				dataHandler.getData().add(new RoomDataItem(phase ? "33%" : "66%", dataHandler.getTime(), phase ? 2 : 1, !config.displayTimeSplits()));
				considerTeleport = false;

				if (deathBallSpawned)
				{
					deathBallSpawned = false;
				}
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (instance.getCurrentRegion() != SOTETSEG && event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String stripped = Text.removeTags(event.getMessage());

		if (stripped.equals(SOTETSEG_DEATHBALL) && party.isInParty())
		{
			clientThread.invokeLater(() -> party.send(new SotetsegNotification(client.getLocalPlayer().getName(), true)));
		}

		if (SOTETSEG_WAVE.matcher(stripped).find())
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

	@Subscribe
	public void onSotetsegNotification(SotetsegNotification event)
	{
		if (!active())
		{
			return;
		}

		clientThread.invokeLater(() -> {
			if (!deathBallSpawned)
			{
				if (config.sotetsegSoundClip())
				{
					soundClip.setFramePosition(0);
					soundClip.start();
				}

				deathBallSpawned = true;
				enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
						.append(new Color(167, 112, 225), event.getName() + " has discovered a large ball of energy shot their way..."));
			}
		});
	}

	private void buildInfobox()
	{
		if (!dataHandler.getData().isEmpty())
		{
			String tooltip;

			if (!dataHandler.Find("Starting Tick").get().isException())
			{
				tooltip = "66% - " + formatTime(dataHandler.FindValue("66%")) + "</br>" +
						"33% - " + formatTime(dataHandler.FindValue("33%")) + formatTime(dataHandler.FindValue("33%"), dataHandler.FindValue("66%")) + "</br>" +
						"Complete - " + formatTime(dataHandler.FindValue("Room")) + formatTime(dataHandler.FindValue("Room"), dataHandler.FindValue("33%"));
			}
			else
			{
				tooltip = "Complete - " + formatTime(dataHandler.FindValue("Room")) + "*";
			}

			sotetsegInfoBox = createInfoBox(plugin, config, itemManager.getImage(BOSS_IMAGE), "Sotetseg", formatTime(dataHandler.FindValue("Room")), tooltip);
			infoBoxManager.addInfoBox(sotetsegInfoBox);
		}
	}

	private void sendChatTimes()
	{
		if (!dataHandler.getData().isEmpty())
		{
			if (!dataHandler.Find("Starting Tick").get().isException())
			{
				enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
						.append(Color.RED, "66%")
						.append(ChatColorType.NORMAL)
						.append(" - " + formatTime(dataHandler.FindValue("66%")) + " - ")
						.append(Color.RED, "33%")
						.append(ChatColorType.NORMAL)
						.append(" - " + formatTime(dataHandler.FindValue("33%")) + formatTime(dataHandler.FindValue("33%"), dataHandler.FindValue("66%"))));

				if (config.roomTimeValidation())
				{
					enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
							.append(Color.RED, "Sotetseg - Room Complete")
							.append(ChatColorType.NORMAL)
							.append(" - " + formatTime(dataHandler.FindValue("Room")) + formatTime(dataHandler.FindValue("Room"), dataHandler.FindValue("33%"))));
				}
			}
			else
			{
				if (config.roomTimeValidation())
				{
					enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
							.append(Color.RED, "Sotetseg - Room Complete")
							.append(ChatColorType.NORMAL)
							.append(" - " + formatTime(dataHandler.FindValue("Room")) + "*"));
				}
			}
		}
	}

	private void hideUnderworldRocks()
	{
		if (instance.getCurrentRegion().isSotetsegUnderworld())
		{
			sceneManager.removeTheseGameObjects(3, UNDERWORLD_ROCKS);
		}
	}

	private void hideUnderworldTornado()
	{
		if (instance.getCurrentRegion().isSotetsegUnderworld())
		{
			Player player = client.getLocalPlayer();
			if (player == null)
			{
				return;
			}
			player.clearSpotAnims();
		}
	}
}
