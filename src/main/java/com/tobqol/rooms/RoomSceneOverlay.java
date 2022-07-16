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

import com.google.common.collect.Multimap;
import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Instance;
import com.tobqol.api.util.TriConsumer;
import com.tobqol.rooms.sotetseg.commons.util.SotetsegTable;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.client.ui.overlay.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;
import java.util.function.BiConsumer;

import static com.tobqol.rooms.sotetseg.commons.util.SotetsegTable.SOTETSEG_CLICKABLE;
import static com.tobqol.rooms.sotetseg.commons.util.SotetsegTable.SOTETSEG_NOT_CLICKABLE;

@Slf4j
public abstract class RoomSceneOverlay<R extends RoomHandler> extends Overlay
{
	protected final Client client;
	protected final Instance instance;
	protected final R room;
	protected final TheatreQOLPlugin plugin;
	protected final TheatreQOLConfig config;

	@Inject
	protected RoomSceneOverlay(
			Client client,
			Instance instance,
			R room,
			TheatreQOLPlugin plugin,
			TheatreQOLConfig config
	)
	{
		this.client = client;
		this.instance = instance;
		this.room = room;
		this.plugin = plugin;
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

		Color color;
		if (npc != null && (SotetsegTable.anyMatch(SOTETSEG_CLICKABLE, npc.getId()) || SotetsegTable.anyMatch(SOTETSEG_NOT_CLICKABLE, npc.getId())))
		{
			color = tickCycle == 3 ? Color.GREEN.brighter() : Color.RED.brighter();
		}
		else
		{
			color = tickCycle > 0 ? Color.RED.brighter() : Color.GREEN.brighter();
		}

		Point textLocation = player.getCanvasTextLocation(graphics, text, player.getLogicalHeight() + 60 + (config.instanceTimerOffset() * 10));

		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, text, color);
		}

		if (tileObject != null)
		{
			Point tileObjectLocation = tileObject.getCanvasTextLocation(graphics, text, 50);
			if (tileObjectLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, tileObjectLocation, text, color);
			}
		}

		if (npc != null)
		{
			Point npcLocation = npc.getCanvasTextLocation(graphics, text, 50);
			if (npcLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, npcLocation, text, color);
			}
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
