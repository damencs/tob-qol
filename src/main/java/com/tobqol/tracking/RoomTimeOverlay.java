package com.tobqol.tracking;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.rooms.RoomHandler;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.inject.Inject;
import java.awt.*;

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

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (room == null || !config.displayRoomTimes().isLiveOverlay() || room.getData().isEmpty())
        {
            return null;
        }


        room.preRenderRoomTimes(this);
        return panelComponent.render(graphics);
    }
}
