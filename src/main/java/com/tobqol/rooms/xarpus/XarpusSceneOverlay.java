package com.tobqol.rooms.xarpus;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayUtil;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.api.game.Instance;
import com.tobqol.config.RenderType;
import com.tobqol.rooms.RoomSceneOverlay;
import com.tobqol.rooms.xarpus.commons.XarpusPhase;

import javax.inject.Inject;
import java.awt.*;

public class XarpusSceneOverlay extends RoomSceneOverlay<XarpusHandler>
{
	@Inject
	protected XarpusSceneOverlay(
			Client client,
			Instance instance,
			XarpusHandler room,
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

		XarpusPhase phase = room.getPhase();

		if (config.displayXarpusInstanceTimer() && phase.isInactiveOrP1())
		{
			drawInstanceTimer(graphics, room.getXarpusNpc(), null);
		}

		drawExhumedsOverlays(graphics, phase);

		return null;
	}

	private void drawExhumedsOverlays(Graphics2D graphics, XarpusPhase phase)
	{
		RenderType markerType = config.xarpusExhumedMarkerType();

		if (markerType.isOff() || !phase.isP1())
		{
			return;
		}

		Color color = config.getXarpusMarkedExhumedsColor();

		room.getExhumedTracker().forEachExhumed((exhumed, ignore) ->
		{
			switch (markerType)
			{
				case TILE:
					OverlayUtil.renderPolygon(graphics, exhumed.getCanvasTilePoly(), color);
					break;
				case HULL:
					OverlayUtil.renderPolygon(graphics, exhumed.getConvexHull(), color);
					break;
			}
		});
	}
}
