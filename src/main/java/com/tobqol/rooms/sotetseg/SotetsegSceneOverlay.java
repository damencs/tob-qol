package com.tobqol.rooms.sotetseg;

import net.runelite.api.Client;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.api.game.Instance;
import com.tobqol.rooms.RoomSceneOverlay;
import com.tobqol.rooms.sotetseg.config.SotetsegInstanceTimerTypes;

import javax.inject.Inject;
import java.awt.*;

public class SotetsegSceneOverlay extends RoomSceneOverlay<SotetsegHandler>
{
	@Inject
	protected SotetsegSceneOverlay(
			Client client,
			TheatreQOLConfig config,
			Instance instance,
			SotetsegHandler room
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

		drawSoteInstanceTimers(graphics);

		return null;
	}

	private void drawSoteInstanceTimers(Graphics2D graphics)
	{
		if (!room.isClickable())
		{
			SotetsegInstanceTimerTypes type = config.getSotetsegInstanceTimerType();

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
	}
}
