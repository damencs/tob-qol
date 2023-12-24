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
import com.tobqol.api.game.Instance;
import com.tobqol.rooms.RoomSceneOverlay;
import com.tobqol.rooms.sotetseg.config.SotetsegInstanceTimerTypes;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;

@Slf4j
public class SotetsegSceneOverlay extends RoomSceneOverlay<SotetsegHandler>
{
	@Inject
	protected SotetsegSceneOverlay(
			Client client,
			Instance instance,
			SotetsegHandler room,
			TheatreQOLPlugin plugin,
			TheatreQOLConfig config
	)
	{
		super(client, instance, room, plugin, config);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!room.active() || room.isClickable())
		{
			if (config.debugSotetsegChosenText())
			{
				drawChosenOverlay(graphics);
			}
			return null;
		}

		setLayer(OverlayLayer.UNDER_WIDGETS);
		graphics.setFont(plugin.getInstanceTimerFont());

		drawSoteInstanceTimers(graphics);
		drawChosenOverlay(graphics);

		return null;
	}

	private void drawSoteInstanceTimers(Graphics2D graphics)
	{
		SotetsegInstanceTimerTypes type = config.getSotetsegInstanceTimerType();
		graphics.setFont(plugin.getInstanceTimerFont());

		switch (instance.getRoomStatus())
		{
			case 0:
				if (type.showOnlyForEntrance() || type.showForAll())
				{
					drawInstanceTimer(graphics, room.getSotetsegNpc(), room.getPortal());
				}
				break;
			case 1:
			case 2:
				if (type.showOnlyForMaze() || type.showForAll())
				{
					drawInstanceTimer(graphics, room.getSotetsegNpc(), room.getPortal());
				}
				break;
		}
	}

	private void drawChosenOverlay(Graphics2D graphics)
	{
		if ((room.isChosen() || config.debugSotetsegChosenText()) && config.hideSotetsegWhiteScreen() && config.showSotetsegChosenText())
		{
			String text = "You have been chosen.";
			graphics.setFont(new Font(config.fontType().getName(), config.fontStyle().getValue(), 20));
			int width = graphics.getFontMetrics().stringWidth(text);
			int drawX = client.getViewportWidth() / 2 - width / 2;
			int drawY = client.getViewportHeight() - (client.getViewportHeight() / 2) + (config.sotetsegChosenTextOffset() * 10);
			OverlayUtil.renderTextLocation(graphics, new net.runelite.api.Point(drawX, drawY), text, Color.WHITE);
		}
	}
}
