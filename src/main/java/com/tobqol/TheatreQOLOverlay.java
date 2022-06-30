package com.tobqol;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import com.google.inject.Inject;

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
