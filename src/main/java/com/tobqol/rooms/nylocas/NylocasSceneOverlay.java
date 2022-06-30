package com.tobqol.rooms.nylocas;

import com.tobqol.rooms.nylocas.config.NylocasRoleSelectionType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.OverlayUtil;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.api.game.Instance;
import com.tobqol.rooms.RoomSceneOverlay;
import com.tobqol.rooms.nylocas.commons.NylocasMap;

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
			TheatreQOLConfig config
	)
	{
		super(client, instance, room, config);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!room.active())
		{
			return null;
		}

		if (config.nyloInstanceTimer())
		{
			drawInstanceTimer(graphics, null, null);
		}

		drawPillarsHP(graphics);
		renderRoleOverlays(graphics);

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
		NylocasRoleSelectionType role = room.getCurrentRoleSelection();
		boolean displaySWTile = config.nyloWavesBigsSWTile();

		if (role.isAnyRole() && !room.getWavesMap().isEmpty())
		{
			room.getWavesMap().forEach((npc, ticks) ->
			{
				if (npc.getName() != null && !npc.isDead())
				{
					Color color = null;

					// Determine whether or not the role even matches prior to matching nylocas name
					if (role.isMage() && npc.getName().equals("Nylocas Hagios"))
					{
						color = NylocasMap.MAGIC_COLOR;
					}
					else if (role.isMelee() && npc.getName().equals("Nylocas Ischyros"))
					{
						color = NylocasMap.MELEE_COLOR;
					}
					else if (role.isRange() && npc.getName().equals("Nylocas Toxobolos"))
					{
						color = NylocasMap.RANGE_COLOR;
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
