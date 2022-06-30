package com.tobqol.rooms;

import lombok.Getter;
import static lombok.AccessLevel.PROTECTED;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.NPC;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.ui.overlay.OverlayManager;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Instance;
import com.tobqol.api.game.Region;
import com.tobqol.api.game.SceneManager;
import static com.google.common.base.Strings.isNullOrEmpty;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Singleton
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
}