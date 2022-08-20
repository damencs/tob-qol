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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.tobqol.api.game.Region.inRegion;
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

	@Inject
	protected RoomHandler(TheatreQOLPlugin plugin, TheatreQOLConfig config)
	{
		this.plugin = plugin;
		this.config = config;
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

	public static boolean crossedLine(Region region, Point start, Point end, boolean vertical, Client client)
	{
		if (inRegion(client, region))
		{
			for (Player p : client.getPlayers())
			{
				WorldPoint wp = p.getWorldLocation();

				if (vertical)
				{
					for (int i = start.getY(); i < end.getY() + 1; i++)
					{
						if (wp.getRegionY() == i && wp.getRegionX() == start.getX())
						{
							return true;
						}
					}
				}
				else
				{
					for (int i = start.getX(); i < end.getX() + 1; i++)
					{
						if (wp.getRegionX() == i && wp.getRegionY() == start.getY())
						{
							return true;
						}
					}
				}
			}
		}

		return false;
	}
}