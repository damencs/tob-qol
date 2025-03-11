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
package com.tobqol.rooms.maiden;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Health;
import com.tobqol.api.game.Instance;
import com.tobqol.config.HPDisplayTypes;
import com.tobqol.rooms.RoomSceneOverlay;
import com.tobqol.rooms.maiden.commons.MaidenPhase;
import com.tobqol.rooms.maiden.commons.MaidenRedCrab;
import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MaidenSceneOverlay extends RoomSceneOverlay<MaidenHandler>
{
	@Inject
	protected MaidenSceneOverlay(
			Client client,
			Instance instance,
			MaidenHandler room,
			TheatreQOLPlugin plugin,
			TheatreQOLConfig config
	)
	{
		super(client, instance, room, plugin, config);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!room.active())
		{
			return null;
		}

		graphics.setFont(plugin.getPluginFont());

		drawCrabOverlays(graphics);

		return null;
	}

	private void drawCrabOverlays(Graphics2D graphics)
	{
		HPDisplayTypes hpDisplayType = config.getMaidenCrabHPType();

		if (hpDisplayType.off())
		{
			return;
		}

		Multimap<WorldPoint, MaidenRedCrab> group = Multimaps.filterValues(
				Multimaps.index(room.getCrabsMap().values(), crab -> crab.npc().getWorldLocation()),
				crab -> !crab.health().zero()
		);

		traverseMultimap(graphics, group, null, (wp, crab, i) ->
		{
			drawCrabTextOverlays(graphics, crab, i);
		});
	}

	private void drawCrabTextOverlays(Graphics2D graphics, MaidenRedCrab crab, int offset)
	{
		HPDisplayTypes hpDisplayType = config.getMaidenCrabHPType();

		List<String> pieces = new ArrayList<>();
		NPC npc = crab.npc();

		if (!hpDisplayType.off())
		{
			Health health = crab.health();
			pieces.add(hpDisplayType.showAsPercent() ? Double.toString(health.truncatedPercent()) : Integer.toString(health.getCurrent()));
		}

		if (config.displayCrabProc())
		{
			pieces.add(crab.phaseKey());
		}

		if (pieces.isEmpty())
		{
			return;
		}

		String text = Strings.join(pieces, " | ");
		Point textLocation = npc.getCanvasTextLocation(graphics, text, 0);

		if (textLocation == null)
		{
			return;
		}
		Color color = getHpColorFromPhaseKey(crab.phaseKey());
		OverlayUtil.renderTextLocation(graphics, new Point(textLocation.getX(), textLocation.getY() - offset), text, color);
	}

	private Color getHpColorFromPhaseKey(String phaseKey)
	{
		if (phaseKey.equals(MaidenPhase.P1.key()))
		{
			return config.maiden70sColor();
		}
		else if (phaseKey.equals(MaidenPhase.P2.key()))
		{
			return config.maiden50sColor();
		}
		else if (phaseKey.equals(MaidenPhase.P3.key()))
		{
			return config.maiden30sColor();
		}
		return Color.WHITE;
	}
}
