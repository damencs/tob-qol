/*
 * Copyright (c) 2022, Damen <gh: damencs>
 * Copyright (c) 2022, WLoumakis <gh: WLoumakis> - Portions of "Loot Reminder" and "MES Options"
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
package com.tobqol;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.*;

import static net.runelite.client.ui.overlay.OverlayUtil.renderTextLocation;

@Slf4j
public class TheatreQOLOverlay extends Overlay
{
    private final TheatreQOLPlugin plugin;
    private final TheatreQOLConfig config;

    @Inject
    private TheatreQOLOverlay(TheatreQOLPlugin plugin, TheatreQOLConfig config)
    {
        this.config = config;
        this.plugin = plugin;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (config.lootReminder() && plugin.isInVerSinhaza() && plugin.getLootChest() != null && plugin.isChestHasLoot())
        {
            Shape poly = plugin.getLootChest().getConvexHull();

            if (poly != null)
            {
                OverlayUtil.renderPolygon(graphics, poly, config.lootReminderColor());
            }

            String text = "You have loot in your chest.";
            Point textLocation = plugin.getEntrance().getCanvasTextLocation(graphics, text, 10);

            if (textLocation != null)
            {
                renderTextLocation(graphics, new Point(textLocation.getX(), textLocation.getY()), text, config.lootReminderColor());
            }
        }

        return null;
    }
}
