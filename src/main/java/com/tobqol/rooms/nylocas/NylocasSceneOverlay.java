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
package com.tobqol.rooms.nylocas;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Instance;
import com.tobqol.rooms.RoomSceneOverlay;
import com.tobqol.rooms.nylocas.commons.NylocasConstants;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;
import java.util.Map;

@Slf4j
public class NylocasSceneOverlay extends RoomSceneOverlay<NylocasHandler>
{
	@Inject
	protected NylocasSceneOverlay(
			Client client,
			Instance instance,
			NylocasHandler room,
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

		drawPillarsHP(graphics);
		renderRoleOverlays(graphics);

		if (room.isDisplayInstanceTimer())
		{
			graphics.setFont(plugin.getInstanceTimerFont());
			drawInstanceTimer(graphics, null, null);
		}

		return null;
	}

	private void drawPillarsHP(Graphics2D graphics)
	{
		if (!config.showNylocasPillarHP())
		{
			return;
		}

		Map<NPC, Integer> pillars = room.getPillars();

		if (pillars.isEmpty())
		{
			return;
		}

		pillars.forEach((pillar, hp) ->
		{
			String str = hp + "%";
			double rMod = 130.0 * hp / 100.0, gMod = 255.0 * hp / 100.0, bMod = 125.0 * hp / 100.0;
			Point textLocation = Perspective.getCanvasTextLocation(client, graphics, pillar.getLocalLocation(), str, 65);
			OverlayUtil.renderTextLocation(graphics, textLocation, str, new Color((int) (255 - rMod), (int) (0 + gMod), (int) (0 + bMod)));
		});
	}

	private void renderRoleOverlays(Graphics2D graphics)
	{
		// Determine config options rather than consistently drawing data on each render as it can be used multiple times within method
		boolean displaySWTile = config.nyloWavesBigsSWTile();

		if (room.isAnyRole() && !room.getWavesMap().isEmpty())
		{
			room.getWavesMap().forEach((npc, ticks) ->
			{
				if (npc.getName() != null && !npc.isDead())
				{
					Color color = null;

					// Determine whether or not the role even matches prior to matching nylocas name
					if (room.isDisplayRoleMage() && npc.getName().equals("Nylocas Hagios"))
					{
						color = NylocasConstants.MAGIC_COLOR;
					}
					else if (room.isDisplayRoleMelee() && npc.getName().equals("Nylocas Ischyros"))
					{
						color = NylocasConstants.MELEE_COLOR;
					}
					else if (room.isDisplayRoleRange() && npc.getName().equals("Nylocas Toxobolos"))
					{
						color = NylocasConstants.RANGE_COLOR;
					}

					if (color != null)
					{
						final LocalPoint localPoint = npc.getLocalLocation();
						Polygon polygon = npc.getCanvasTilePoly();
						OverlayUtil.renderPolygon(graphics, polygon, color);

						if (room.getBigsMap().containsKey(npc) && displaySWTile)
						{
							polygon = Perspective.getCanvasTilePoly(client, new LocalPoint(localPoint.getX() - (Perspective.LOCAL_TILE_SIZE / 2), localPoint.getY() - (Perspective.LOCAL_TILE_SIZE / 2)));
							OverlayUtil.renderPolygon(graphics, polygon, color);
						}
					}
				}
			});
		}
	}
}
