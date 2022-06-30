package com.tobqol.rooms.verzik;

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
import com.tobqol.rooms.verzik.commons.VerzikMap;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.awt.*;
import java.text.DecimalFormat;

@Slf4j
public class VerzikOverlay extends RoomSceneOverlay<VerzikHandler>
{
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

	@Inject
	protected VerzikOverlay(
			Client client,
			Instance instance,
			VerzikHandler room,
			TheatreQOLConfig config
	)
	{
		super(client, instance, room, config);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!instance.getCurrentRegion().isVerzik())
		{
			return null;
		}

		NPC verzikNpc = room.getVerzikNpc();

		if (verzikNpc == null)
		{
			return null;
		}

		VerzikMap def = VerzikMap.queryTable(verzikNpc.getId());

		if (def == null)
		{
			return null;
		}

		switch (def)
		{
			case VERZIK_P2:
				displayRedCrabs(graphics);
				break;
			case VERZIK_P3:
				displayTornadoes(graphics);
				displayYellowPools(graphics);
		}

		return null;
	}

	private void displayTornadoes(Graphics2D graphics)
	{
		if (!config.shouldMarkVerzikTornadoes() || room.getTornadoes().isEmpty())
		{
			return;
		}

		room.getTornadoes().forEach(t ->
		{
			Color color = config.verzikMarkedTornadoColor();
			t.first(client).ifPresent(p -> OverlayUtil.renderPolygon(graphics, p, color));
			t.second(client).ifPresent(p -> OverlayUtil.renderPolygon(graphics, p, color.darker()));
		});
	}

	private void displayRedCrabs(Graphics2D graphics)
	{
		if (config.verzikReds())
		{
			room.getVerzikReds().forEach((crab, v) ->
			{
				int v_health = v.getValue();
				int v_healthRation = v.getKey();
				if (crab.getName() != null && crab.getHealthScale() > 0)
				{
					v_health = crab.getHealthScale();
					v_healthRation = Math.min(v_healthRation, crab.getHealthRatio());
				}
				String percentage = String.valueOf(DECIMAL_FORMAT.format(((float) v_healthRation / (float) v_health) * 100f));

				Point textLocation = crab.getCanvasTextLocation(graphics, percentage, 80);

				if (!crab.isDead() && textLocation != null)
				{
					OverlayUtil.renderTextLocation(graphics, textLocation, percentage, Color.WHITE);
				}
			});

			NPC[] reds = room.getVerzikReds().keySet().toArray(new NPC[0]);
			for (NPC npc : reds)
			{
				if (npc.getName() != null && npc.getHealthScale() > 0 && npc.getHealthRatio() < 100)
				{
					Pair<Integer, Integer> newVal = new MutablePair<>(npc.getHealthRatio(), npc.getHealthScale());
					if (room.getVerzikReds().containsKey(npc))
					{
						room.getVerzikReds().put(npc, newVal);
					}
				}
			}
		}
	}

	private void displayYellowPools(Graphics2D graphics)
	{
		if (!config.shouldMarkVerzikYellows())
		{
			return;
		}

		room.getYellows().forEach(wp ->
		{
			LocalPoint lp = LocalPoint.fromWorld(client, wp);

			if (lp == null)
			{
				return;
			}

			Polygon tile = Perspective.getCanvasTilePoly(client, lp);

			if (tile != null)
			{
				graphics.setColor(config.verzikMarkedYellowsColor());
				graphics.setStroke(new BasicStroke(2));
				graphics.draw(tile);
				graphics.fill(tile);
			}
		});
	}
}
