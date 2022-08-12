package com.tobqol.tracking;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RoomInfoBox extends InfoBox
{
    private final TheatreQOLConfig config;
    private final String room;
    private final String time;
    private final String tooltip;

    public RoomInfoBox(
            BufferedImage image,
            TheatreQOLPlugin plugin,
            TheatreQOLConfig config,
            String room,
            String time,
            String tooltip
    )
    {
        super(image, plugin);

        this.config = config;
        this.room = room;
        this.time = time;
        this.tooltip = tooltip;

        setPriority(InfoBoxPriority.LOW);
    }

    @Override
    public String getName()
    {
        return room;
    }

    @Override
    public String getText()
    {
        return getTime(true);
    }

    @Override
    public Color getTextColor()
    {
        return Color.GREEN;
    }

    @Override
    public String getTooltip()
    {
        return tooltip;
    }

    @Override
    public boolean render()
    {
        return config.displayRoomTimes().isInfobox();
    }

    private String getTime(boolean simple)
    {
        return simple ? StringUtils.substringBefore(time, ".") : time;
    }
}
