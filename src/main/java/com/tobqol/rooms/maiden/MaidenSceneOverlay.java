package com.tobqol.rooms.maiden;

import lombok.extern.slf4j.Slf4j;
import joptsimple.internal.Strings;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayUtil;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.tobqol.TheatreQOLConfig;
import com.tobqol.api.game.Health;
import com.tobqol.api.game.Instance;
import com.tobqol.config.HPDisplayTypes;
import com.tobqol.rooms.RoomSceneOverlay;
import com.tobqol.rooms.maiden.commons.MaidenRedCrab;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MaidenSceneOverlay extends RoomSceneOverlay<MaidenHandler>
{
	@Inject
	protected MaidenSceneOverlay(
			Client client,
			Instance instance,
			MaidenHandler room,
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

		Color color = Optional.ofNullable(Color.WHITE).orElse(crab.health().color());
		OverlayUtil.renderTextLocation(graphics, new Point(textLocation.getX(), textLocation.getY() - offset), text, color);
	}
}
