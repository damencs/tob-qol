/*
 * Copyright (c) 2022, Damen <gh: damencs>
 * Copyright (c) 2022, Boris - Portions of Area Sound Effects Played
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
package com.tobqol.rooms.xarpus;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Region;
import com.tobqol.rooms.RoomHandler;
import com.tobqol.rooms.xarpus.commons.ExhumedTracker;
import com.tobqol.rooms.xarpus.commons.XarpusConstants;
import com.tobqol.rooms.xarpus.commons.XarpusPhase;
import com.tobqol.rooms.xarpus.commons.XarpusTable;
import com.tobqol.timetracking.RoomDataHandler;
import com.tobqol.timetracking.RoomDataItem;
import com.tobqol.timetracking.RoomInfoBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.NPC;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.Text;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.awt.*;

import static com.tobqol.api.game.Region.XARPUS;
import static com.tobqol.rooms.xarpus.commons.XarpusConstants.BOSS_IMAGE;
import static com.tobqol.rooms.xarpus.commons.XarpusConstants.XARPUS_WAVE;
import static com.tobqol.timetracking.RoomInfoUtil.createInfoBox;
import static com.tobqol.timetracking.RoomInfoUtil.formatTime;

@Slf4j
public class XarpusHandler extends RoomHandler
{
	@Inject
	private XarpusSceneOverlay sceneOverlay;

	private RoomDataHandler dataHandler;

	@Getter
	@CheckForNull
	private NPC xarpusNpc = null;

	private RoomInfoBox xarpuInfoBox;

	@Getter
	private XarpusPhase phase = XarpusPhase.UNKNOWN;

	@Getter
	@CheckForNull
	private ExhumedTracker exhumedTracker = null;

	private static Clip soundClip;

	@Inject
	protected XarpusHandler(TheatreQOLPlugin plugin, TheatreQOLConfig config)
	{
		super(plugin, config);
		setRoomRegion(Region.XARPUS);

		dataHandler = plugin.getDataHandler();
	}

	@Override
	public void load()
	{
		overlayManager.add(sceneOverlay);
		soundClip = generateSoundClip("sheesh-hoyaa.wav", config.xarpusSoundClipVolume());
	}

	@Override
	public void unload()
	{
		overlayManager.remove(sceneOverlay);
		soundClip.close();
		reset();
	}

	@Override
	public boolean active()
	{
		return instance.getCurrentRegion().isXarpus() && xarpusNpc != null && !xarpusNpc.isDead();
	}

	@Override
	public void reset()
	{
		xarpusNpc = null;
		phase = XarpusPhase.UNKNOWN;

		if (exhumedTracker != null)
		{
			exhumedTracker = null;
		}

		if (instance.getRaidStatus() <= 1)
		{
			infoBoxManager.removeInfoBox(xarpuInfoBox);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("tobqol"))
		{
			if (event.getKey().equals("xarpusSoundClipVolume") && config.xarpusSoundClip())
			{
				if (soundClip != null)
				{
					FloatControl control = (FloatControl) soundClip.getControl(FloatControl.Type.MASTER_GAIN);

					if (control != null)
					{
						control.setValue((float)(config.xarpusSoundClipVolume() / 2 - 45));
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
		if (active())
		{
			return;
		}

		isNpcFromName(e.getNpc(), XarpusConstants.BOSS_NAME, n ->
		{
			instance.lazySetMode(() -> XarpusTable.findMode(n.getId()));
			xarpusNpc = n;
			phase = XarpusPhase.compose(n);

			if (phase == XarpusPhase.P2 && (n.getOverheadText() != null || client.getVarbitValue(6448) <= 250))
			{
				phase = XarpusPhase.P3;
			}

			if (XarpusTable.anyMatch(XarpusTable.XARPUS_P1, n.getId()))
			{
				if (!dataHandler.Find("Starting Tick").isPresent())
				{
					dataHandler.getData().add(new RoomDataItem("Starting Tick", client.getTickCount(), true));
					dataHandler.setShouldTrack(true);
					return;
				}
			}

			exhumedTracker = new ExhumedTracker();
		});
	}

	@Subscribe
	private void onNpcChanged(NpcChanged e)
	{
		if (!active())
		{
			return;
		}

		isNpcFromName(e.getNpc(), XarpusConstants.BOSS_NAME, n ->
		{
			phase = XarpusPhase.compose(n);

			if (XarpusTable.anyMatch(XarpusTable.XARPUS_P1, n.getId()))
			{
				if (!dataHandler.Find("Starting Tick").isPresent())
				{
					dataHandler.getData().add(new RoomDataItem("Starting Tick", client.getTickCount(), true));
					dataHandler.setShouldTrack(true);
					return;
				}
			}
			else if (XarpusTable.anyMatch(XarpusTable.XARPUS_P23, n.getId()))
			{
				if (!dataHandler.Find("Exhumeds").isPresent())
				{
					dataHandler.getData().add(new RoomDataItem("Exhumeds", dataHandler.getTime(), 1, false));
					return;
				}
			}
		});
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned e)
	{
		if (!active() || xarpusNpc == null || !isNpcFromName(e.getNpc(), XarpusConstants.BOSS_NAME))
		{
			return;
		}

		reset();
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if (instance.isInRaid() && instance.getCurrentRegion().isXarpus())
		{
			if (instance.getRoomStatus() == 1 && !dataHandler.Find("Starting Tick").isPresent())
			{
				dataHandler.getData().add(new RoomDataItem("Starting Tick", dataHandler.getTime(), true, true));
				dataHandler.setShouldTrack(true);
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

        if (!phase.isInactiveOrP1())
        {
            exhumedTracker = null;
        }

		if (xarpusNpc.getOverheadText() != null && !phase.isP3())
		{
			phase = XarpusPhase.P3;
		}
	}

	@Subscribe
	private void onGroundObjectSpawned(GroundObjectSpawned e)
	{
		if (!active() || exhumedTracker == null)
		{
			return;
		}

		if (exhumedTracker.track(e.getGroundObject()) && instance.getTickCycle() > -1)
		{
			instance.resetTickCycle();
		}
	}

    @Subscribe
    public void onGroundObjectDespawned(GroundObjectDespawned e)
    {
        if (!active() || exhumedTracker == null)
        {
            return;
        }
        exhumedTracker.untrack(e.getGroundObject());
    }

	@Subscribe
	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event)
	{
		if(xarpusNpc != null && active())
		{
			if (event.getSoundId() == 4005 && instance.isHardMode() && config.muteXarpusHMEntry())
			{
				event.consume();
			}
			else if (event.getSoundId() == 4007 && config.xarpusSoundClip())
			{
				event.consume();
			}
		}
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged event)
	{
		if (active() && event.getActor() instanceof NPC && config.xarpusSoundClip())
		{
			if (xarpusNpc == event.getActor())
			{
				event.getActor().setOverheadText("Sheeeeeesh!");
				soundClip.setFramePosition(0);
				soundClip.start();

				dataHandler.getData().add(new RoomDataItem("Screech", dataHandler.getTime(), 2, false));
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

		if (XARPUS_WAVE.matcher(stripped).find())
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

    public ExhumedTracker getExhumedTracker(){
        if(exhumedTracker != null)
        {
            return exhumedTracker;
        }
        return null;
    }

	private void buildInfobox()
	{
		if (!dataHandler.getData().isEmpty())
		{
			String tooltip;

			if (!dataHandler.Find("Starting Tick").get().isException())
			{
				tooltip = "Exhumeds - " + formatTime(dataHandler.FindValue("Exhumeds")) + "</br>" +
					"Screech - " + formatTime(dataHandler.FindValue("Screech")) + formatTime(dataHandler.FindValue("Screech"), dataHandler.FindValue("Exhumeds")) + "</br>" +
					"Complete - " + formatTime(dataHandler.FindValue("Room")) + formatTime(dataHandler.FindValue("Room"), dataHandler.FindValue("Screech"));
			}
			else
			{
				tooltip = "Complete - " + formatTime(dataHandler.FindValue("Room")) + "*";
			}

			xarpuInfoBox = createInfoBox(plugin, config, itemManager.getImage(BOSS_IMAGE), "Xarpus", formatTime(dataHandler.FindValue("Room")), tooltip);
			infoBoxManager.addInfoBox(xarpuInfoBox);
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
						.append(Color.RED, "Exhumeds")
						.append(ChatColorType.NORMAL)
						.append(" - " + formatTime(dataHandler.FindValue("Exhumeds")) + " - ")
						.append(Color.RED, "Screech")
						.append(ChatColorType.NORMAL)
						.append(" - " + formatTime(dataHandler.FindValue("Screech")) + formatTime(dataHandler.FindValue("Screech"), dataHandler.FindValue("Exhumeds"))));

				if (config.roomTimeValidation())
				{
					enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
							.append(Color.RED, "Xarpus - Room Complete")
							.append(ChatColorType.NORMAL)
							.append(" - " + formatTime(dataHandler.FindValue("Room")) + formatTime(dataHandler.FindValue("Room"), dataHandler.FindValue("Screech"))));
				}
			}
			else
			{
				if (config.roomTimeValidation())
				{
					enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
							.append(Color.RED, "Xarpus - Room Complete")
							.append(ChatColorType.NORMAL)
							.append(" - " + formatTime(dataHandler.FindValue("Room")) + "*"));
				}
			}
		}
	}
}
