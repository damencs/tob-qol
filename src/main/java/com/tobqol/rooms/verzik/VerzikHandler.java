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
package com.tobqol.rooms.verzik;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Region;
import com.tobqol.rooms.RoomHandler;
import com.tobqol.rooms.verzik.commons.Tornado;
import com.tobqol.rooms.verzik.commons.VerzikMap;
import com.tobqol.timetracking.RoomDataHandler;
import com.tobqol.timetracking.RoomDataItem;
import com.tobqol.timetracking.RoomInfoBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.tobqol.api.game.Region.XARPUS;
import static com.tobqol.rooms.verzik.commons.VerzikMap.*;
import static com.tobqol.timetracking.RoomInfoUtil.createInfoBox;
import static com.tobqol.timetracking.RoomInfoUtil.formatTime;

@Slf4j
public class VerzikHandler extends RoomHandler
{
	@Inject
	private VerzikOverlay overlay;

	private RoomDataHandler dataHandler;

	@Getter
	private NPC verzikNpc = null;

	private RoomInfoBox verzikInfoBox;

	@Getter
	private final Map<NPC, Pair<Integer, Integer>> verzikReds = new HashMap<>();

	@Getter
	private final ArrayList<Tornado> tornadoes = new ArrayList<>();

	@Getter
	private final List<WorldPoint> yellows = new ArrayList<>();

	private boolean allYellowsSpawned = false;

	@Getter
	private byte ticksLeft = -1;
	private static Clip soundClip;
	private boolean deathBallSpawned = false;
	private int deathBallSafetyNet = 0;

	@Inject
	protected VerzikHandler(TheatreQOLPlugin plugin, TheatreQOLConfig config)
	{
		super(plugin, config);
		setRoomRegion(Region.VERZIK);

		dataHandler = plugin.getDataHandler();
	}

	@Override
	public void load()
	{
		overlayManager.add(overlay);
		soundClip = generateSoundClip("weewoo-hoyaa.wav", config.sotetsegSoundClipVolume());
	}

	@Override
	public void unload()
	{
		overlayManager.remove(overlay);
		soundClip.close();
		reset();
	}

	@Override
	public boolean active()
	{
		return instance.getCurrentRegion().isVerzik();
	}

	@Override
	public void reset()
	{
		verzikNpc = null;
		verzikReds.clear();
		tornadoes.clear();
		ticksLeft = -1;
		yellows.clear();
		deathBallSpawned = false;
		deathBallSafetyNet = 0;

		if (instance.getRaidStatus() <= 1)
		{
			infoBoxManager.removeInfoBox(verzikInfoBox);
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
			case "verzikSoundClipVolume":
			{
				if (soundClip != null && config.verzikSoundClip())
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
	private void onNpcSpawned(NpcSpawned e)
	{
		if (!active())
		{
			return;
		}

		NPC npc = e.getNpc();

		if (npc == null)
		{
			return;
		}

		int id = npc.getId();
		String name = npc.getName();

		if (VerzikMap.matchesAnyMode(VerzikMap.RED_NYLO, npc.getId()))
		{
			verzikReds.putIfAbsent(npc, MutablePair.of(npc.getHealthRatio(), npc.getHealthScale()));

			if (!dataHandler.Find("Reds").isPresent())
			{
				dataHandler.getData().add(new RoomDataItem("Reds", dataHandler.getTime(), false));
			}
		}

		if (VerzikMap.matchesAnyMode(VerzikMap.TORNADO, id))
		{
			tornadoes.add(new Tornado(npc));
		}

		if (name == null)
		{
			return;
		}

		switch (name)
		{
			case VerzikMap.BOSS_NAME:
				instance.lazySetMode(() -> VerzikMap.findMode(id));
				reset();
				verzikNpc = npc;
				break;
		}
	}

	@Subscribe
	private void onNpcChanged(NpcChanged e)
	{
		if (!active())
		{
			return;
		}

		VerzikMap def = VerzikMap.queryTable(e.getNpc().getId());

		if (def == null)
		{
			return;
		}

        if (def == VERZIK_P1)
        {
            if (!dataHandler.Find("Starting Tick").isPresent())
            {
                dataHandler.getData().add(new RoomDataItem("Starting Tick", client.getTickCount(), true));
                dataHandler.setShouldTrack(true);
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
		String name = npc.getName();

		verzikReds.remove(npc);
        tornadoes.remove(npc);

		if(VerzikMap.queryTable(npc.getId()) == VERZIK_P1)
		{
			if (!dataHandler.Find("P1").isPresent())
			{
				dataHandler.getData().add(new RoomDataItem("P1", dataHandler.getTime(), !config.displayTimeSplits()));
			}
		}

		if (name == null)
		{
			return;
		}

		switch (name)
		{
			case VerzikMap.BOSS_NAME:
				reset();
				break;
		}
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if (instance.isInRaid() && instance.getCurrentRegion().isVerzik())
		{
			if (instance.getRoomStatus() == 1 && !dataHandler.Find("Starting Tick").isPresent())
			{
				dataHandler.getData().add(new RoomDataItem("Starting Tick", client.getTickCount(), true, true));
				dataHandler.setShouldTrack(true);

				dataHandler.getData().add(new RoomDataItem("Room", dataHandler.getTime(), 99, false, "P2"));
			}

			if (dataHandler.isShouldTrack() && !dataHandler.getData().isEmpty())
			{
				dataHandler.updateTotalTime();
			}
		}

		if (!active() || verzikNpc == null)
		{
			return;
		}

		VerzikMap def = VerzikMap.queryTable(verzikNpc.getId());

		if (def == null)
		{
			return;
		}

		switch (def)
		{
			case VERZIK_P3:
				tornadoes.forEach(t -> t.shift());

				if (!yellows.isEmpty() && !allYellowsSpawned)
				{
					ticksLeft = 14;

					if (instance.isHardMode())
					{
						ticksLeft = 17;
					}

					allYellowsSpawned = true;
				}

				if (ticksLeft <= 0)
				{
					yellows.clear();
					return;
				}

				ticksLeft--;

				if (deathBallSpawned && deathBallSafetyNet++ == 10)
				{
					deathBallSafetyNet = 0;
					deathBallSpawned = false;
				}
				break;
		}
	}

	@Subscribe
	private void onGraphicsObjectCreated(GraphicsObjectCreated e)
	{
		if (!active() || allYellowsSpawned)
		{
			return;
		}

		GraphicsObject obj = e.getGraphicsObject();

		if (obj.getId() == VerzikMap.YELLOW_POOL)
		{
			WorldPoint wp = WorldPoint.fromLocal(client, obj.getLocation());
			yellows.add(wp);
		}
	}

	@Subscribe
	private void onGraphicChanged(GraphicChanged e)
	{
		if (!active() || yellows.isEmpty() || !(e.getActor() instanceof Player))
		{
			return;
		}

		Player player = (Player) e.getActor();

		if (player.getGraphic() != VerzikMap.YELLOW_GRAPHIC)
		{
			return;
		}

		WorldPoint wp = WorldPoint.fromLocal(client, player.getLocalLocation());
		Predicate<WorldPoint> filter = _wp -> _wp.equals(wp) || _wp.distanceTo2D(wp) <= 1;
		yellows.removeIf(filter);
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor().getAnimation() == VERZIK_P2_TRANSITION)
		{
			if (!dataHandler.Find("P2").isPresent())
			{
				dataHandler.getData().add(new RoomDataItem("P2", dataHandler.getTime(), !config.displayTimeSplits()));
			}
		}
	}

	@Subscribe
	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event)
	{
		if (event.getSource() != null && event.getSource().getName() != null && verzikNpc != null && config.muteVerzikSounds())
		{
			if (event.getSoundId() == 3991 || event.getSoundId() == 3987)
			{
				event.consume();
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (instance.getCurrentRegion() != XARPUS && event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String stripped = Text.removeTags(event.getMessage());

		if (VERZIK_WAVE.matcher(stripped).find())
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

		if (event.getMessage().contains(GREEN_BALL_TEXT) || event.getMessage().contains(GREEN_BALL_BOUNCE_TEXT))
		{
			if (!deathBallSpawned)
			{
				if (config.verzikSoundClip())
				{
					soundClip.setFramePosition(0);
					soundClip.start();
				}

				deathBallSpawned = true;
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
				tooltip = "P1 - " + formatTime(dataHandler.FindValue("P1")) + "</br>" +
					"Reds - " + formatTime(dataHandler.FindValue("Reds")) + formatTime(dataHandler.FindValue("Reds"), dataHandler.FindValue("P1")) + "</br>" +
					"P2 - " + formatTime(dataHandler.FindValue("P2")) + formatTime(dataHandler.FindValue("P2"), dataHandler.FindValue("Reds")) + "</br>" +
					"Complete - " + formatTime(dataHandler.FindValue("Room")) + formatTime(dataHandler.FindValue("Room"), dataHandler.FindValue("P2"));
			}
			else
			{
				tooltip = "Complete - " + formatTime(dataHandler.FindValue("Room")) + "*";
			}

			verzikInfoBox = createInfoBox(plugin, config, itemManager.getImage(BOSS_IMAGE), "Verzik", formatTime(dataHandler.FindValue("Room")), tooltip);
			infoBoxManager.addInfoBox(verzikInfoBox);
		}

		// Update total time infobox
		plugin.updateTotalTimeInfoBox();
	}

	private void sendChatTimes()
	{
		if (!dataHandler.getData().isEmpty())
		{
			if (!dataHandler.Find("Starting Tick").get().isException())
			{
				enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
						.append(Color.RED, "P1")
						.append(ChatColorType.NORMAL)
						.append(" - " + formatTime(dataHandler.FindValue("P1")) + " - ")
						.append(Color.RED, "Reds")
						.append(ChatColorType.NORMAL)
						.append(" - " + formatTime(dataHandler.FindValue("Reds")) + " - " + formatTime(dataHandler.FindValue("Reds"), dataHandler.FindValue("P1")) + " - ")
						.append(Color.RED, "P2")
						.append(ChatColorType.NORMAL)
						.append(" - " + formatTime(dataHandler.FindValue("P2")) + " - " + formatTime(dataHandler.FindValue("P2"), dataHandler.FindValue("Reds"))));

				if (config.roomTimeValidation())
				{
					enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
							.append(Color.RED, "Verzik - Room Complete")
							.append(ChatColorType.NORMAL)
							.append(" - " + formatTime(dataHandler.FindValue("Room")) + formatTime(dataHandler.FindValue("Room"), dataHandler.FindValue("P2"))));
				}
			}
			else
			{
				if (config.roomTimeValidation())
				{
					enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
							.append(Color.RED, "Verzik - Room Complete")
							.append(ChatColorType.NORMAL)
							.append(" - " + formatTime(dataHandler.FindValue("Room")) + "*"));
				}
			}
		}
	}
}
