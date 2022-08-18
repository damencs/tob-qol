package com.tobqol.tracking;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.rooms.RoomHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.inject.Inject;
import java.awt.*;

@Slf4j
public class RoomTimeOverlay extends Overlay
{
    @Getter
    @Setter
    private RoomHandler room;

    private TheatreQOLConfig config;

    @Getter
    protected final PanelComponent panelComponent = new PanelComponent();

    @Inject
    public RoomTimeOverlay(RoomHandler room, TheatreQOLConfig config)
    {
        this.room = room;
        this.config = config;

        setPosition(OverlayPosition.TOP_LEFT);
    }

    int ticks = 0;
    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (room == null || !config.displayRoomTimes().isLiveOverlay() || room.getData().isEmpty())
        {
            return null;
        }

        if (ticks < 100)
        {
            ticks++;
        }
        else
        {
            ticks = 0;
        }

        room.preRenderRoomTimes(this);
        return panelComponent.render(graphics);
    }
}
