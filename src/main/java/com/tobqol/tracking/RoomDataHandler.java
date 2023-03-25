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
package com.tobqol.tracking;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
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

        if (data.isEmpty())
        {
            LineComponent lineComponent = LineComponent.builder().left("Room").right(formatTime(0)).build();
            timeOverlay.getPanelComponent().getChildren().add(lineComponent);

            return timeOverlay.getPanelComponent();
        }

        if (Find("Starting Tick").get().isException())
        {
            LineComponent lineComponent = LineComponent.builder().left("Room").right(formatTime(FindValue("Room")) + '*').build();
            timeOverlay.getPanelComponent().getChildren().add(lineComponent);

            return timeOverlay.getPanelComponent();
        }

        boolean splitDifferences = config.displayTimeSplitDifferences();

        Collections.sort(data);

        data.forEach((item) ->
        {
            if (item.isHidden() || (item.isHidden() && item.getName() != "Room"))
            {
                return;
            }

            boolean hasComparable = (item.getCompareName().equals("") || (isShouldTrack() && item.getName().equals("Room"))) ? false : Find(item.getCompareName()).isPresent();

            LineComponent lineComponent = LineComponent.builder().left(item.getName()).right(formatTime(item.getValue()) +
                    (splitDifferences && hasComparable ? formatTime(item.getValue(), FindValue(item.getCompareName())) : "")).build();
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
        if (!Find("Room").isPresent())
        {
            getData().add(new RoomDataItem("Room", getTime(), 99, false));
        }
        else
        {
            Find("Room").get().setValue(getTime());
        }
    }

    public void updateHiddenItems(boolean set)
    {
        data.forEach(item ->
        {
            if (item.getName() != "Starting Tick" && item.getName() != "Room")
            {
                item.setHidden(set);
            }
        });
    }
}
