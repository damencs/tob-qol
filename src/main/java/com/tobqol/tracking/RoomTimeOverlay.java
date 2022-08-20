package com.tobqol.tracking;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.inject.Inject;
import java.awt.*;

@Slf4j
public class RoomTimeOverlay extends Overlay
{
    private TheatreQOLPlugin plugin;
    private TheatreQOLConfig config;

    @Getter
    protected PanelComponent panelComponent = new PanelComponent();

    @Inject
    public RoomTimeOverlay(TheatreQOLPlugin plugin, TheatreQOLConfig config)
    {
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.displayRoomTimes().isLiveOverlay() || !plugin.getInstanceService().isInRaid())
        {
            return null;
        }

        this.panelComponent = plugin.getDataHandler().preRenderRoomTimes();

        return panelComponent.render(graphics);
    }
}
