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
package com.tobqol.loottracking;

import com.google.gson.Gson;
import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.RaidConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LootTrackingHandler
{
    private TheatreQOLPlugin plugin;
    private TheatreQOLConfig config;
    private ConfigManager configManager;
    private Gson gson;

    public LootTrackingHandler(TheatreQOLPlugin plugin, TheatreQOLConfig config, ConfigManager configManager, Gson gson)
    {
        this.plugin = plugin;
        this.config = config;
        this.configManager = configManager;
        this.gson = gson;
    }

    @Getter
    @Setter
    private boolean chestsHandled = false;

    @Getter
    @Setter
    private boolean lootHandled = false;

    @Getter
    @Setter
    private List<Integer> loadedChests = new ArrayList<>();

    public void load()
    {

    }

    public void unload()
    {
        reset();
    }

    public void reset()
    {
        chestsHandled = false;
        lootHandled = false;
        loadedChests.clear();
    }

    public void handleChest(int chestId)
    {
        if (chestsHandled)
        {
            return;
        }

        loadedChests.add(chestId);

        if (loadedChests.size() == plugin.getInstanceService().getPartySize())
        {
            boolean isPurple = loadedChests.stream().anyMatch(chest -> RaidConstants.LOOT_ROOM_PURPLE_CHEST_IDS.contains(chestId));
            boolean isPersonal = loadedChests.contains(RaidConstants.TOB_LOOT_ROOM_CHEST_PURPLE_PERSONAL);

            processPurple(isPurple, isPersonal);
            chestsHandled = true;
        }
    }

    public void processPurple(boolean isPurple, boolean isPersonal)
    {
        // Get existing memory, if available, otherwise create new
        LootTrackingMemory memory = getExistingMemory();

        if (isPurple && isPersonal)
        {
            // Announce loot tracking data before we reset to track what streaks have been broken
            announceLootTracking(memory, true, true);

            // Reset both if it is a personal
            memory.countSincePersonal = 0;
            memory.countSinceOther = 0;
        }
        // Reset other if not personal purple, as others want to know how many since their purple
        else if (isPurple)
        {
            memory.countSincePersonal++;
            memory.countSinceOther = 0;

            // Announce loot tracking data
            announceLootTracking(memory, true, false);
        }
        // If no-ones purple, increase both
        else
        {
            memory.countSincePersonal++;
            memory.countSinceOther++;

            // Announce loot tracking data
            announceLootTracking(memory, false, false);
        }

        // Save updated memory
        saveMemory(memory);
    }

    // Process loot item separately because purple chests will be detected before an item in the chest will be
    public void processLootItem(String itemName)
    {
        // Get existing memory, if available, otherwise create new
        LootTrackingMemory memory = getExistingMemory();

        // If the last item matches the current, just ignore legwork
        if (!memory.getLastPersonalItem().equalsIgnoreCase(itemName))
        {
            memory.lastPersonalItem = itemName;
            saveMemory(memory);
        }

        lootHandled = true;
    }

    public LootTrackingMemory getExistingMemory()
    {
        String existingMemoryJson = configManager.getRSProfileConfiguration(TheatreQOLConfig.GROUP_NAME, "loottracking");

        if (existingMemoryJson != null)
        {
            return gson.fromJson(existingMemoryJson, LootTrackingMemory.class);
        }
        else
        {
            return new LootTrackingMemory(0, null, 0);
        }
    }

    public void saveMemory(LootTrackingMemory updatedMemory)
    {
        String json = gson.toJson(updatedMemory);
        configManager.setRSProfileConfiguration(TheatreQOLConfig.GROUP_NAME, "loottracking", json);
        log.info("tobqol: update loot tracking memory (countSincePersonal: {}, lastPersonalItem: {}, countSinceOther: {}",
                updatedMemory.countSincePersonal, updatedMemory.lastPersonalItem, updatedMemory.countSinceOther);
    }

    public void announceLootTracking(LootTrackingMemory memory, boolean isPurple, boolean isPersonal)
    {
        if (!config.dryLootTracking())
        {
            return;
        }

        String personalCount = "<col=cf3f21>" + memory.countSincePersonal + "</col>";
        String otherCount = "<col=cf3f21>" + memory.countSinceOther + "</col>";
        String lastItem = memory.getLastPersonalItem() != null ? "(Last Drop: <col=cf3f21>" + memory.lastPersonalItem + "</col>)" : "";

        if (config.simplifyLootTracking())
        {
            plugin.queueChatMessage("<col=800080>Dry Streak:</col> Personal " + personalCount
                    + (lastItem != null ? " " + lastItem : "")
                    + " / Overall " + otherCount);
        }
        else
        {
            if (isPurple)
            {
                // If personal purple
                if (isPersonal)
                {
                    // If a player's personal count is 0, it means they have seen a purple last raid
                    if (memory.countSincePersonal == 0)
                    {
                        plugin.queueChatMessage("<col=#800080>You have received back-to-back purple chests. Congratulations!");
                        plugin.queueChatMessage("Your last purple was a <col=cf3f21>" + memory.lastPersonalItem + "</col>.");
                    }
                    // If personal purple breaking dry streak
                    else
                    {
                        plugin.queueChatMessage("You have broken your personal dry streak of " + personalCount + ". " + lastItem);
                        plugin.queueChatMessage("It has been " + otherCount + " raids since you've seen any purple.");
                    }
                }
                // If other purple
                else
                {
                    plugin.queueChatMessage("You are on a personal dry streak of " + personalCount + ". " + lastItem);
                    plugin.queueChatMessage("You have broken your dry streak of " + otherCount + " raids since you've seen any purple.");
                }
            }
            // If no purple, announce dry streaks
            else
            {
                plugin.queueChatMessage("You are on a personal dry streak of " + personalCount + ". " + lastItem);
                plugin.queueChatMessage("You have completed " + otherCount + " raids since you've seen any purple.");
            }
        }
    }
}
