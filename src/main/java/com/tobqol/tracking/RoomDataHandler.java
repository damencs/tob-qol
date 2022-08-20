package com.tobqol.tracking;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.config.times.TimeDisplayDetail;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static com.tobqol.tracking.RoomInfoUtil.formatTime;

@Slf4j
public class RoomDataHandler
{
    private Client client;
    private TheatreQOLPlugin plugin;
    private TheatreQOLConfig config;

    @Getter
    @Setter
    private RoomTimeOverlay timeOverlay;

    @Getter
    private ArrayList<RoomDataItem> data = new ArrayList<>();

    @Getter
    @Setter
    private boolean shouldTrack = false;

    public RoomDataHandler(Client client, TheatreQOLPlugin plugin, TheatreQOLConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        timeOverlay = new RoomTimeOverlay(plugin, config);
    }

    public void load()
    {
        plugin.overlayManager.add(timeOverlay);
    }

    public void unload()
    {
        plugin.overlayManager.remove(timeOverlay);
    }

    public PanelComponent preRenderRoomTimes()
    {
        timeOverlay.getPanelComponent().getChildren().clear();

        boolean detailed = config.displayRoomTimesDetail() == TimeDisplayDetail.DETAILED;

        if (data.isEmpty())
        {
            LineComponent lineComponent = LineComponent.builder().left("Total Time").right(formatTime(0, detailed)).build();
            timeOverlay.getPanelComponent().getChildren().add(lineComponent);

            return timeOverlay.getPanelComponent();
        }

        boolean splitDifferences = config.displayTimeSplitDifferences();

        Collections.sort(data);

        data.forEach((item) ->
        {
            if (item.isHidden() || (!detailed && item.getName() != "Total Time"))
            {
                return;
            }

            boolean hasComparable = !item.getCompareName().equals("");

            LineComponent lineComponent = LineComponent.builder().left(item.getName()).right(formatTime(item.getValue(), detailed) +
                    (splitDifferences && hasComparable ? formatTime(item.getValue(), FindValue(item.getCompareName()), detailed) : "")).build();
            timeOverlay.getPanelComponent().getChildren().add(lineComponent);
        });

        return timeOverlay.getPanelComponent();
    }

    public Optional<RoomDataItem> Find(String name)
    {
        return data.stream().filter(f -> f.getName().equals(name)).findFirst();
    }

    public int FindValue(String name)
    {
        if (!Find(name).isPresent())
        {
            return 0;
        }

        return data.stream().filter(f -> f.getName().equals(name)).findFirst().get().getValue();
    }

    public int getTime()
    {
        return client.getTickCount() - FindValue("Starting Tick");
    }

    public void updateTotalTime()
    {
        if (!Find("Total Time").isPresent())
        {
            getData().add(new RoomDataItem("Total Time", getTime(), 99, false));
        }
        else
        {
            Find("Total Time").get().setValue(getTime());
        }
    }
}
