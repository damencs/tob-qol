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
package com.tobqol.rooms.xarpus;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Instance;
import com.tobqol.rooms.RoomSceneOverlay;
import com.tobqol.rooms.xarpus.commons.ExhumedTracker;
import com.tobqol.rooms.xarpus.commons.XarpusPhase;
import net.runelite.api.Client;
import net.runelite.api.GroundObject;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;

public class XarpusSceneOverlay extends RoomSceneOverlay<XarpusHandler>
{
	@Inject
	protected XarpusSceneOverlay(
			Client client,
			Instance instance,
			XarpusHandler room,
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

		setLayer(OverlayLayer.ABOVE_SCENE);
		graphics.setFont(plugin.getInstanceTimerFont());

		XarpusPhase phase = room.getPhase();

		if (config.displayXarpusInstanceTimer() && phase.isInactiveOrP1())
		{
			drawInstanceTimer(graphics, room.getXarpusNpc(), null);
		}

        if(config.xarpusExhumedMarker() && phase.isInactiveOrP1())
        {
            markExhumeds(graphics);
        }

        return null;
	}

    private void markExhumeds(Graphics2D graphics)
    {
        ExhumedTracker exhumedTracker = room.getExhumedTracker();
        for(GroundObject e:exhumedTracker.getExhumeds())
        {
            Color color = config.xarpusMarkedExhumedColor();
            OverlayUtil.renderPolygon(graphics, e.getCanvasTilePoly(), color);
        }
    }
}
