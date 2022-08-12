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
package com.tobqol.rooms;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Instance;
import com.tobqol.api.game.Region;
import com.tobqol.api.game.SceneManager;
import com.tobqol.config.times.TimeDisplayDetail;
import com.tobqol.tracking.RoomDataItem;
import com.tobqol.tracking.RoomTimeOverlay;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.NPC;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.tobqol.tracking.RoomInfoUtil.formatTime;
import static lombok.AccessLevel.PROTECTED;

@Singleton
@Slf4j
public abstract class RoomHandler
{
	public static final Predicate<Integer> VALUE_IS_ZERO = v -> v <= 0;

	public static final BiFunction<Object, Integer, Integer> INCREMENT_VALUE = (k, v) -> ++v;
	public static final BiFunction<Object, Integer, Integer> DECREMENT_VALUE = (k, v) -> --v;

	protected final TheatreQOLPlugin plugin;
	protected final TheatreQOLConfig config;

	@Inject
	protected Client client;

	@Inject
	protected ClientThread clientThread;

	@Inject
	protected OverlayManager overlayManager;

	@Inject
	protected Instance instance;

	@Inject
	protected SceneManager sceneManager;

	@Inject
	protected ChatMessageManager chatMessageManager;

	@Inject
	protected ItemManager itemManager;

	@Inject
	protected InfoBoxManager infoBoxManager;

	@Getter(PROTECTED)
	private Region roomRegion = Region.UNKNOWN;

	@Getter
	@Setter
	private RoomTimeOverlay timeOverlay;

	@Getter
	private ArrayList<RoomDataItem> data = new ArrayList<>();

	@Getter
	@Setter
	private boolean shouldTrack = false;

	@Inject
	protected RoomHandler(TheatreQOLPlugin plugin, TheatreQOLConfig config)
	{
		this.plugin = plugin;
		this.config = config;

		timeOverlay = new RoomTimeOverlay(this, config);
	}

	public void init()
	{
	}

	public abstract void load();

	public abstract void unload();

	public boolean active()
	{
		return false;
	}

	public abstract void reset();

	protected final void setRoomRegion(Region region)
	{
		if (!roomRegion.isUnknown())
		{
			return;
		}

		if (region == null)
		{
			roomRegion = Region.UNKNOWN;
			return;
		}

		switch (region)
		{
			case LOBBY:
				roomRegion = Region.MAIDEN;
				break;
			case SOTETSEG_MAZE:
				roomRegion = Region.SOTETSEG;
				break;
			default:
				roomRegion = region;
				break;
		}
	}

	protected final boolean isInRoomRegion()
	{
		if (roomRegion.isUnknown())
		{
			return false;
		}

		Region current = instance.getCurrentRegion();
		return roomRegion.equals(current) || (roomRegion.isSotetseg() && current.isSotetseg());
	}

	protected static boolean isNpcFromName(NPC npc, String name)
	{
		if (npc == null || isNullOrEmpty(name))
		{
			return false;
		}

		String _name = npc.getName();
		return !isNullOrEmpty(_name) && _name.equals(name);
	}

	protected static boolean isNpcFromName(NPC npc, String name, Consumer<NPC> action)
	{
		if (isNpcFromName(npc, name))
		{
			if (action != null)
			{
				action.accept(npc);
			}

			return true;
		}

		return false;
	}

	protected static void when(boolean condition, Runnable success, Runnable failure)
	{
		if (condition)
		{
			Optional.ofNullable(success).ifPresent(Runnable::run);
			return;
		}

		Optional.ofNullable(failure).ifPresent(Runnable::run);
	}

	@Nullable
	protected final MessageNode sendChatMessage(ChatMessageType type, String message)
	{
		if (type == null || isNullOrEmpty(message))
		{
			return null;
		}

		return client.addChatMessage(type, "", message, "", false);
	}

	protected final void enqueueChatMessage(ChatMessageType type, Consumer<ChatMessageBuilder> user)
	{
		if (type == null || user == null)
		{
			return;
		}

		ChatMessageBuilder builder = new ChatMessageBuilder();
		user.accept(builder);

		String message = builder.build();

		if (isNullOrEmpty(message))
		{
			return;
		}

		chatMessageManager.queue(QueuedMessage.builder().type(type).runeLiteFormattedMessage(message).build());
	}

	protected final void enqueueChatMessage(ChatMessageType type, ChatMessageBuilder builder)
	{
		if (type == null || builder == null)
		{
			return;
		}

		String message = builder.build();

		if (isNullOrEmpty(message))
		{
			return;
		}

		chatMessageManager.queue(QueuedMessage.builder().type(type).runeLiteFormattedMessage(message).build());
	}

	public final void preRenderRoomTimes(RoomTimeOverlay overlay)
	{
		boolean detailed = config.displayRoomTimesDetail() == TimeDisplayDetail.DETAILED;
		boolean splitDifferences = config.displayTimeSplitDifferences();

		timeOverlay.getPanelComponent().getChildren().clear();

		Collections.sort(data);

		data.forEach((item) ->
		{
			if (item.isHidden() || (!detailed && item.getName() != "Total Time"))
			{
				return;
			}

			boolean hasComparable = !item.getCompareName().equals("");

			LineComponent lineComponent = LineComponent.builder().left(item.getName()).right(formatTime(item.getValue(), detailed) +
					(splitDifferences && hasComparable ? formatTime(item.getValue(), FindValue(item.getCompareName()), detailed) : "")).build();
			timeOverlay.getPanelComponent().getChildren().add(lineComponent);
		});
	}

	public Optional<RoomDataItem> Find(String name)
	{
		return data.stream().filter(f -> f.getName().equals(name)).findFirst();
	}

	public int FindValue(String name)
	{
		return data.stream().filter(f -> f.getName().equals(name)).findFirst().get().getValue();
	}

	protected int getTime()
	{
		return client.getTickCount() - FindValue("Starting Tick");
	}

	protected void updateTotalTime()
	{
		if (!Find("Total Time").isPresent())
		{
			getData().add(new RoomDataItem("Total Time", getTime(), 99, false));
		}
		else
		{
			Find("Total Time").get().setValue(getTime());
		}
	}
}