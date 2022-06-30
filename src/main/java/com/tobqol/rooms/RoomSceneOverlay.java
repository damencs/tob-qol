package com.tobqol.rooms;

import com.google.common.collect.Multimap;
import com.tobqol.api.util.TriConsumer;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.api.game.Instance;

import java.awt.*;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import javax.inject.Inject;

@Slf4j
public abstract class RoomSceneOverlay<R extends RoomHandler> extends Overlay
{
	protected final Client client;
	protected final Instance instance;
	protected final R room;
	protected final TheatreQOLConfig config;

	@Inject
	protected RoomSceneOverlay(
			Client client,
			Instance instance,
			R room,
			TheatreQOLConfig config
	)
	{
		this.client = client;
		this.instance = instance;
		this.room = room;
		this.config = config;

		setPriority(OverlayPriority.HIGH);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	protected final void drawInstanceTimer(Graphics2D graphics, @Nullable NPC npc, @Nullable TileObject tileObject)
	{
		int tickCycle = instance.getTickCycle();

		if (tickCycle == -1)
		{
			return;
		}

		Player player = client.getLocalPlayer();

		if (player == null)
		{
			return;
		}

		String text = Integer.toString(tickCycle);
		Color color = tickCycle > 0 ? Color.RED.brighter() : Color.GREEN.brighter();

		Point textLocation = player.getCanvasTextLocation(graphics, text, player.getLogicalHeight() + 60);
		OverlayUtil.renderTextLocation(graphics, textLocation, text, color);

		if (tileObject != null)
		{
			Point tileObjectLocation = tileObject.getCanvasTextLocation(graphics, text, 50);
			OverlayUtil.renderTextLocation(graphics, tileObjectLocation, text, color);
		}

		if (npc != null)
		{
			Point npcLocation = npc.getCanvasTextLocation(graphics, text, 50);
			OverlayUtil.renderTextLocation(graphics, npcLocation, text, color);
		}
	}

	public static <K, V> void traverseMultimap(Graphics2D graphics, Multimap<K, V> multimap, BiConsumer<K, Integer> before, TriConsumer<K, V, Integer> after)
	{
		if (multimap == null || multimap.isEmpty())
		{
			return;
		}

		for (K k : multimap.keys())
		{
			int offset = 0;
			if (k != null && before != null)
			{
				before.accept(k, offset);
			}

			for (V v : multimap.get(k))
			{
				if (k != null && v != null && after != null)
				{
					after.accept(k, v, offset);
				}
				offset += graphics.getFontMetrics().getHeight();
			}
		}
	}
}
