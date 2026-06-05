/*
 * Copyright (c) 2022, Damen <gh: damencs>
 * Copyright (c) 2022, WLoumakis <gh: WLoumakis> - Portions of "MES Options"
 * Copyright (c) 2022, Boris - Portions of "Xarpus Sheesh and HM Entry"
 * Copyright (c) 2021, BickusDiggus <gh: BickusDiggus> - Portions of "Loot Reminder"
 * Copyright (c) 2020, Broooklyn <gh: Broooklyn> - "ToB Light Up" Relevant Code
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

import com.tobqol.config.HPDisplayTypes;
import com.tobqol.config.SupplyChestPreference;
import com.tobqol.config.SurgePotionPreference;
import com.tobqol.config.font.FontStyles;
import com.tobqol.config.font.FontTypes;
import com.tobqol.config.times.TimeDisplayType;
import com.tobqol.rooms.nylocas.config.NylocasObjects;
import com.tobqol.rooms.sotetseg.config.SotetsegInstanceTimerTypes;
import com.tobqol.rooms.sotetseg.config.SotetsegProjectileTheme;
import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup(TheatreQOLConfig.GROUP_NAME)
public interface TheatreQOLConfig extends Config
{
	String GROUP_NAME = "tobqol";

	/**
	 * Sections
	 */
	@ConfigSection(
			name = "General",
			description = "Configuration settings for things that are not room-specific",
			position = 1,
			closedByDefault = true
	)
	String GENERAL_SECTION = "generalSection";

	@ConfigSection(
			name = "The Maiden of Sugadinti",
			description = "Configuration settings for The Maiden of Sugadinti",
			position = 2,
			closedByDefault = true
	)
	String MAIDEN_SECTION = "maidenSection";

	@ConfigSection(
			name = "Pestilent Bloat",
			description = "Configuration settings for Pestilent Bloat",
			position = 3,
			closedByDefault = true
	)
	String BLOAT_SECTION = "bloatSection";

	@ConfigSection(
			name = "Nylocas",
			description = "Configuration settings for Nylocas",
			position = 4,
			closedByDefault = true
	)
	String NYLO_SECTION = "nyloSection";

	@ConfigSection(
			name = "Sotetseg",
			description = "Configuration settings for Sotetseg",
			position = 5,
			closedByDefault = true
	)
	String SOTETSEG_SECTION = "sotetsegSection";

	@ConfigSection(
			name = "Xarpus",
			description = "Configuration settings for Xarpus",
			position = 6,
			closedByDefault = true
	)
	String XARPUS_SECTION = "xarpusSection";

	@ConfigSection(
			name = "Verzik Vitur",
			description = "Configuration settings for Verzik Vitur",
			position = 7,
			closedByDefault = true
	)
	String VERZIK_SECTION = "verzikSection";

	@ConfigSection(
			name = "Font Settings",
			description = "Configuration settings for the plugin's text overlays",
			position = 8,
			closedByDefault = true
	)
	String FONT_SECTION = "fontSection";

	@ConfigSection(
			name = "Time Tracking Settings",
			description = "Configuration settings for the plugin's time tracking features",
			position = 9,
			closedByDefault = true
	)
	String TIME_SECTION = "timeSection";

	@ConfigSection(
			name = "Loot Tracking Settings",
			description = "Configuration settings for the plugin's loot tracking features",
			position = 10,
			closedByDefault = true
	)
	String LOOT_TRACKING_SECTION = "lootTrackingSection";

	/**
	 * General Section
	 */
	@ConfigItem(
			name = "Bank-all MES Loot Chest",
			keyName = "bankAllMES",
			description = "- Removes the 'Force Right Click' flag from the [Bank-all] option inside the Monumental Chest in the Loot Room",
			position = 1,
			section = GENERAL_SECTION
	)
	default boolean bankAllMES()
	{
		return false;
	}

	@ConfigItem(
			name = "Supply Chest MES",
			keyName = "supplyChestMES",
			description = "- Swaps to the preferred purchasing quantity within the supply chests<br>" +
							"~ Credit - gh: WLoumakis",
			position = 2,
			section = GENERAL_SECTION
	)
	default SupplyChestPreference supplyChestMES()
	{
		return SupplyChestPreference.OFF;
	}

	@ConfigItem(
			name = "Light Up Ver Sinhaza",
			keyName = "lightUp",
			description = "- Removes the darkness lighting within Ver Sinhaza (ToB Bank Area)<br>" +
							"~ Credit - gh: Broooklyn",
			position = 3,
			section = GENERAL_SECTION
	)
	default boolean lightUp()
	{
		return false;
	}

	@ConfigItem(
			name = "Loot Reminder",
			keyName = "lootReminder",
			description = "- Indicates whether or not the chest has loot in it by highlighting the chest and putting a message on the entrance<br>" +
							"~ Credit - gh: BickusDiggus",
			position = 4,
			section = GENERAL_SECTION
	)
	default boolean lootReminder()
	{
		return false;
	}

	@ConfigItem(
			name = "Loot Reminder Color",
			keyName = "lootReminderColor",
			description = "- Set a color for the Loot Reminder overlay<br>" +
							"~ Credit - gh: BickusDiggus",
			position = 5,
			section = GENERAL_SECTION
	)
	@Alpha
	default Color lootReminderColor()
	{
		return new Color(196, 89, 89, 200);
	}

	@ConfigItem(
			name = "Salve Reminder",
			keyName = "salveReminder",
			description = "- Indicates whether or not you have salve in your inventory prior to entering the raid and putting a message on the entrance<br>",
			position = 6,
			section = GENERAL_SECTION
	)
	default boolean salveReminder()
	{
		return true;
	}

	@ConfigItem(
			name = "Salve Reminder Color",
			keyName = "salveReminderColor",
			description = "- Set a color for the Salve Reminder overlay<br>",
			position = 7,
			section = GENERAL_SECTION
	)
	@Alpha
	default Color salveReminderColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			name = "Spellbook Reminder",
			keyName = "spellbookReminder",
			description = "- Indicates what spellbook you are on by putting a message on the entrance<br>",
			position = 8,
			section = GENERAL_SECTION
	)
	default boolean spellbookReminder()
	{
		return true;
	}

	@ConfigItem(
			name = "Spellbook Reminder Color",
			keyName = "spellbookReminderColor",
			description = "- Set a color for the Spellbook Reminder overlay<br>",
			position = 9,
			section = GENERAL_SECTION
	)
	@Alpha
	default Color spellbookReminderColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
			name = "Ammo Reminder",
			keyName = "ammoReminder",
			description = "- Indicates whether or not you have ammo in your equipment slot prior to entering the raid and putting a message on the entrance<br>",
			position = 10,
			section = GENERAL_SECTION
	)
	default boolean ammoReminder() { return false; }

	@ConfigItem(
			name = "Ammo Reminder Color",
			keyName = "ammoReminderColor",
			description = "- Set a color for the Ammo Reminder overlay<br>",
			position = 11,
			section = GENERAL_SECTION
	)
	@Alpha
	default Color ammoReminderColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
			name = "Surge Potion Reminder",
			keyName = "surgePotionReminder",
			description = "- Indicates whether or not you have sufficient surge potion doses and putting a message on the entrance<br>",
			position = 12,
			section = GENERAL_SECTION
	)
	default SurgePotionPreference surgePotionReminder() { return SurgePotionPreference.OFF; }

	@ConfigItem(
			name = "Surge Potion Reminder Color",
			keyName = "surgePotionReminderColor",
			description = "- Set a color for the Surge Potion Reminder overlay<br>",
			position = 13,
			section = GENERAL_SECTION
	)
	@Alpha
	default Color surgePotionReminderColor()
	{
		return Color.GREEN;
	}

	/**
	 * Maiden Configs
	 */
	@ConfigItem(
			name = "Display Crabs Health",
			keyName = "maidenCrabHPDisplayType",
			description = "- Displays the hitpoints percentage or the hitpoints of each alive Nylocas Matomenos",
			position = 1,
			section = MAIDEN_SECTION
	)
	default HPDisplayTypes getMaidenCrabHPType()
	{
		return HPDisplayTypes.OFF;
	}

	@ConfigItem(
			name = "Display Crab Proc",
			keyName = "maidenCrabProcDisplay",
			description = "- Displays what proc a crab is from in the overlay",
			position = 2,
			section = MAIDEN_SECTION
	)
	default boolean displayCrabProc() { return false; }

	@ConfigItem(
			name = "70s Crab Color",
			keyName = "maiden70sColor",
			description = "Crab health color for 70s crabs",
			position = 3,
			section = MAIDEN_SECTION
	)
	@Alpha
	default Color maiden70sColor() { return new Color(255, 255, 255); }

	@ConfigItem(
			name = "50s Crab Color",
			keyName = "maiden50sColor",
			description = "Crab health color for 50s crabs",
			position = 4,
			section = MAIDEN_SECTION
	)
	@Alpha
	default Color maiden50sColor() { return new Color(255, 255, 255); }
	@ConfigItem(
			name = "30s Crab Color",
			keyName = "maiden30sColor",
			description = "Crab overlay color for 30s crabs",
			position = 5,
			section = MAIDEN_SECTION
	)
	@Alpha
	default Color maiden30sColor()
	{
		return new Color(255, 255, 255);
	}

	@ConfigItem(
			name = "Show Leaks",
			keyName = "maidenLeaks",
			description = "- Sends a client message per leak showing what leaked, their hp and on what Maiden phase",
			position = 6,
			section = MAIDEN_SECTION
	)
	default boolean displayMaidenLeaks()
	{
		return false;
	}

	/**
	 * Bloat Configs
	 */
	@ConfigItem(
			name = "Hide Ceiling Chains",
			keyName = "hideCeilingChains",
			description = "- Hides the chains hanging from the ceiling in the Bloat room<br>" +
							"* Disabling this feature whilst in Bloat will cause a stutter to refresh the scene",
			position = 1,
			section = BLOAT_SECTION
	)
	default boolean shouldNullCeilingChains()
	{
		return false;
	}

	@ConfigItem(
			name = "Track Downs in Chat/Infobox",
			keyName = "trackDowns",
			description = "- Track the Bloat downs in chat/tooltip after room completion<br>" +
					"* Must have the Chat/Infobox Data Tracking enabled",
			position = 2,
			section = BLOAT_SECTION
	)
	default boolean trackDowns()
	{
		return false;
	}

	/**
	 * Nylocas Configs
	 */
	@ConfigItem(
			name = "Pillar HP",
			keyName = "nyloPillarHP",
			description = "- Display the health of each pillar",
			position = 1,
			section = NYLO_SECTION
	)
	default boolean showNylocasPillarHP()
	{
		return false;
	}

	@ConfigItem(
			name = "Hide Objects",
			keyName = "nyloHideObjects",
			description = "- Hide the Nylocas Pillars, Spectator Webs, and/or Walls if desired (none provide clickboxes when present)<br>" +
							"* Disabling this feature whilst in the Nylocas room will cause a stutter to refresh the scene",
			position = 2,
			section = NYLO_SECTION
	)
	default NylocasObjects nyloHideObjects()
	{
		return NylocasObjects.OFF;
	}

	@ConfigItem(
			name = "Recolor Menu",
			keyName = "nyloWavesRecolorMenu",
			description = "- Recolors each entry in the menu to their respective color<br>" +
					"* Gray: Melee (Nylocas Ischyros)<br>" +
					"* Green: Range (Nylocas Toxobolos)<br>" +
					"* Blue: Magic (Nylocas Hagios)",
			position = 3,
			section = NYLO_SECTION
	)
	default boolean nyloWavesRecolorMenu()
	{
		return false;
	}

	@ConfigItem(
			name = "Recolor Bigs Menu Darker",
			keyName = "nyloWavesRecolorBigsMenuDarker",
			description = "- Darkens the color on the menu if the Nylocas is big",
			position = 4,
			section = NYLO_SECTION
	)
	default boolean nyloWavesRecolorBigsMenuDarker()
	{
		return false;
	}

	@ConfigItem(
			name = "Role Selector",
			keyName = "displayNyloRoleSelector",
			description = "- Shows the Nylocas Room Role Selection Overlay that you can use to highlight the tiles of your role's nylos",
			position = 5,
			section = NYLO_SECTION
	)
	default boolean displayNyloRoleSelector()
	{
		return false;
	}

	@ConfigItem(
			name = "",
			keyName = "nyloRoleSelectedMage",
			description = "",
			hidden = true
	)
	default boolean nyloRoleSelectedMage()
	{
		return false;
	}

	@ConfigItem(
			name = "",
			keyName = "nyloRoleSelectedMage",
			description = "",
			hidden = true
	)
	void nyloSetRoleSelectedMage(boolean enabled);

	@ConfigItem(
			name = "",
			keyName = "nyloRoleSelectedMelee",
			description = "",
			hidden = true
	)
	default boolean nyloRoleSelectedMelee()
	{
		return false;
	}

	@ConfigItem(
			name = "",
			keyName = "nyloRoleSelectedMelee",
			description = "",
			hidden = true
	)
	void nyloSetRoleSelectedMelee(boolean enabled);

	@ConfigItem(
			name = "",
			keyName = "nyloRoleSelectedRange",
			description = "",
			hidden = true
	)
	default boolean nyloRoleSelectedRange()
	{
		return false;
	}

	@ConfigItem(
			name = "",
			keyName = "nyloRoleSelectedRange",
			description = "",
			hidden = true
	)
	void nyloSetRoleSelectedRange(boolean enabled);

	@ConfigItem(
			name = "Bigs SW Tile",
			keyName = "nyloWavesBigsSWTile",
			description = "- Display the SW Tile of big nylos",
			position = 6,
			section = NYLO_SECTION
	)
	default boolean nyloWavesBigsSWTile()
	{
		return false;
	}

	@ConfigItem(
			name = "Instance Timer",
			keyName = "nyloInstanceTimer",
			description = "- Displays the tick cycle for Nylocas' instance",
			position = 7,
			section = NYLO_SECTION
	)
	default boolean nyloInstanceTimer()
	{
		return false;
	}

	@ConfigItem(
			name = "Low Detail",
			keyName = "nyloLowDetail",
			description = "- Removes the Graphics Objects that spawn when a Nylo dies<br>" +
						"* Entity hider removes them from being displayed, but still allows them to spawn and linger",
			position = 8,
			section = NYLO_SECTION
	)
	default boolean nyloLowDetail()
	{
		return false;
	}

	/**
	 * Sotetseg Configs
	 */
	@ConfigItem(
			name = "Instance Timer",
			keyName = "sotetsegInstanceTimer",
			description = "- Displays the tick cycle for Sotetsegs' instance<br>" +
					"* Pre-Start: Displays before the room starts<br>" +
					"* Maze: Displays during the maze",
			position = 1,
			section = SOTETSEG_SECTION
	)
	default SotetsegInstanceTimerTypes getSotetsegInstanceTimerType()
	{
		return SotetsegInstanceTimerTypes.OFF;
	}

	@ConfigItem(
			name = "Projectile Theme",
			keyName = "getSotetsegProjectileTheme",
			description = "- Modifies the Projectile to appear in a specific theme to assist with colorblind users<br>" +
					"* Inferno: Utilizes the blob mage and range projectiles<br>" +
					"* TOA: Utilizes the red skull and rock projectiles from Wardens",
			position = 2,
			section = SOTETSEG_SECTION
	)
	default SotetsegProjectileTheme getSotetsegProjectileTheme()
	{
		return SotetsegProjectileTheme.DEFAULT;
	}

	@ConfigItem(
			name = "Themed Death Ball",
			keyName = "themedDeathBall",
			description = "- Use the theme's ball to replace Sotetseg's death ball when a theme is selected",
			position = 3,
			section = SOTETSEG_SECTION
	)
	default boolean themedDeathBall()
	{
		return true;
	}

	@ConfigItem(
			name = "Hide White Screen",
			keyName = "sotetsegHideWhiteScreen",
			description = "- Hides the transitional white screen during Sotetseg maze phase procs.",
			position = 4,
			section = SOTETSEG_SECTION
	)
	default boolean hideSotetsegWhiteScreen()
	{
		return false;
	}

	@ConfigItem(
			name = "Show Chosen Text",
			keyName = "showSotetsegChosenText",
			description = "- Displays a simulated version of 'You have been chosen.' text when Hide White Screen is enabled",
			position = 5,
			section = SOTETSEG_SECTION
	)
	default boolean showSotetsegChosenText()
	{
		return true;
	}

	@Range(min=-50, max=50)
	@ConfigItem(
			name = "Chosen Text Offset",
			keyName = "sotetsegChosenTextOffset",
			description = "- Adjusts the height of 'You have been chosen.' text\n" +
					"when Show Chosen Text AND Hide White Screen options are enabled",
			position = 6,
			section = SOTETSEG_SECTION
	)
	default int sotetsegChosenTextOffset()
	{
		return 0;
	}

	@ConfigItem(
			name = "Debug Chosen Text",
			keyName = "debugSotetsegChosenText",
			description = "- Displays a simulated version of 'You have been chosen.' text to see height of text",
			position = 7,
			section = SOTETSEG_SECTION
	)
	default boolean debugSotetsegChosenText()
	{
		return false;
	}

	@ConfigItem(
			name = "Hide Underworld Rocks",
			keyName = "sotetsegHideUnderworldRocks",
			description = "- Hides the rocks surrounding the Sotetseg maze",
			position = 8,
			section = SOTETSEG_SECTION
	)
	default boolean sotetsegHideUnderworldRocks()
	{
		return false;
	}

	@ConfigItem(
			name = "Hide Underworld Tornado",
			keyName = "sotetsegHideUnderworldTornado",
			description = "- Hides the tornado surrounding your player in the Sotetseg maze",
			position = 9,
			section = SOTETSEG_SECTION
	)
	default boolean sotetsegHideUnderworldTornado()
	{
		return false;
	}

	@ConfigItem(
			name = "Sotetseg Death Ball Alarm",
			keyName = "sotetsegSoundClip",
			description = "- Replaces the Death Ball sound effect with a wee-woo sound clip<br>" +
					"* Thank you Hoyaa for providing this sound clip for the project",
			position = 10,
			section = SOTETSEG_SECTION
	)
	default boolean sotetsegSoundClip()
	{
		return false;
	}

	@Range(max = 100)
	@ConfigItem(
			name = "Death Ball Alarm Volume",
			keyName = "sotetsegSoundClipVolume",
			description = "- Sets the volume of the sound clip",
			position = 11,
			section = SOTETSEG_SECTION
	)
	default int sotetsegSoundClipVolume()
	{
		return 65;
	}

	/**
	 * Xarpus Configs
	 */
	@ConfigItem(
			name = "Instance Timer",
			keyName = "xarpusInstanceTimer",
			description = "- Displays Xarpus's tick cycle for the initial exhumed spawn. Enter on 0 to start exhumeds as soon as possible.",
			position = 1,
			section = XARPUS_SECTION
	)
	default boolean displayXarpusInstanceTimer()
	{
		return false;
	}

	@ConfigItem(
			name = "Mute Xarpus HM Entry",
			keyName = "muteXarpusHMEntry",
			description = "- Mutes the Xarpus hardmode entrance noise when poison splats are thrown",
			position = 2,
			section = XARPUS_SECTION
	)
	default boolean muteXarpusHMEntry()
	{
		return false;
	}

	@ConfigItem(
			name = "Xarpus Sheesh Screech",
			keyName = "xarpusSoundClip",
			description = "- Replaces the Screech sound effect with a Sheesh sound clip<br>" +
							"~ Credit - Boris<br>" +
							"* Thank you Hoyaa for providing this sound clip for the project",
			position = 3,
			section = XARPUS_SECTION
	)
	default boolean xarpusSoundClip()
	{
		return false;
	}

	@Range(max = 100)
	@ConfigItem(
			name = "Sheesh Volume",
			keyName = "xarpusSoundClipVolume",
			description = "- Sets the volume of the sound clip",
			position = 4,
			section = XARPUS_SECTION
	)
	default int xarpusSoundClipVolume()
	{
		return 65;
	}

    @ConfigItem(
            name = "Xarpus Exhumed Marker",
            keyName = "xarpusExhumedMarker",
            description = "- Marks the tile of a spawned exhumed",
            position = 5,
            section = XARPUS_SECTION
    )
    default boolean xarpusExhumedMarker()
    {
        return false;
    }

    @ConfigItem(
            name = "Marked Exhumed Color",
            keyName = "xarpusMarkedExhumedColor",
            description = "- Set the color of the marked exhumed overlay",
            position = 6,
            section = XARPUS_SECTION
    )
    @Alpha
    default Color xarpusMarkedExhumedColor()
    {
        return new Color(215, 122, 97);
    }

	/**
	 * Verzik Configs
	 */
	@ConfigItem(
			name = "Verzik Reds Health Overlay",
			keyName = "verzikReds",
			description = "- Displays the health of red crabs during Verzik",
			position = 1,
			section = VERZIK_SECTION
	)
	default boolean verzikReds()
	{
		return false;
	}

	@ConfigItem(
			name = "Mark Tornadoes",
			keyName = "verzikTornadoes",
			description = "- Highlight Verzik tornadoes",
			position = 2,
			section = VERZIK_SECTION
	)
	default boolean shouldMarkVerzikTornadoes()
	{
		return false;
	}

	@ConfigItem(
			name = "Marked Tornado Color",
			keyName = "verzikMarkedTornadoColor",
			description = "- Set the color of the marked tornadoes overlay",
			position = 3,
			section = VERZIK_SECTION
	)
	@Alpha
	default Color verzikMarkedTornadoColor()
	{
		return new Color(215, 122, 97);
	}

	@ConfigItem(
			name = "Mute Verzik Sounds",
			keyName = "muteVerzikSounds",
			description = "- Mute Verzik's sounds such as P2 area affect and her walking in P3",
			position = 4,
			section = VERZIK_SECTION
	)
	default boolean muteVerzikSounds()
	{
		return false;
	}

	@ConfigItem(
			name = "Verzik Death Ball Alarm",
			keyName = "verzikSoundClip",
			description = "- Notifies you of the green ball with a wee-woo sound clip on chat message received<br>" +
					"* Thank you Hoyaa for providing this sound clip for the project",
			position = 5,
			section = VERZIK_SECTION
	)
	default boolean verzikSoundClip()
	{
		return false;
	}

	@Range(max = 100)
	@ConfigItem(
			name = "Death Ball Alarm Volume",
			keyName = "verzikSoundClipVolume",
			description = "- Sets the volume of the sound clip",
			position = 6,
			section = VERZIK_SECTION
	)
	default int verzikSoundClipVolume()
	{
		return 65;
	}

	/**
	 * Font Configs
	 */
	@ConfigItem(
			name = "Font Type",
			keyName = "fontType",
			description = "Dynamically change the font for all ToB QoL Overlays",
			position = 1,
			section = FONT_SECTION
	)
	default FontTypes fontType() { return FontTypes.REGULAR; }

	@ConfigItem(
			name = "Font Style",
			keyName = "fontStyle",
			description = "Dynamically change the font style for all ToB QoL Overlays<br>" +
						"* This will not alter the base RS font styles",
			position = 2,
			section = FONT_SECTION
	)
	default FontStyles fontStyle()
	{
		return FontStyles.PLAIN;
	}

	@Range(min = 12, max = 20)
	@ConfigItem(
			name = "Font Size",
			keyName = "fontSize",
			description = "Dynamically change the font size for all ToB QoL Overlays<br>" +
					"* This will not alter the base RS font styles",
			position = 3,
			section = FONT_SECTION
	)
	default int fontSize()
	{
		return 16;
	}

	@Range(min = 12, max = 42)
	@ConfigItem(
			name = "Instance Timer Size",
			keyName = "instanceTimerSize",
			description = "Dynamically change the font size for all the Instance Timers<br>" +
					"* This inherits the font type and style selected above<br>" +
					"* This will not alter the base RS font styles",
			position = 3,
			section = FONT_SECTION
	)
	default int instanceTimerSize()
	{
		return 16;
	}

	@Range(min = -12, max = 12)
	@ConfigItem(
			name = "Instance Timer Offset",
			keyName = "instanceTimerOffset",
			description = "Dynamically change the vertical offset for all the Instance Timers displayed over a character",
			position = 4,
			section = FONT_SECTION
	)
	default int instanceTimerOffset()
	{
		return 0;
	}

	/**
	 * Time Tracking Configs
	 */
	@ConfigItem(
			name = "Display Room Times",
			keyName = "displayRoomTimes",
			description = "- Display live time for each room's boss<br>" +
						"* Please note that this will not show splits on rooms that reveal mechanics until the room has ended (Bloat downs)",
			position = 1,
			section = TIME_SECTION
	)
	default TimeDisplayType displayRoomTimes()
	{
		return TimeDisplayType.OFF;
	}

	@ConfigItem(
			name = "Display Splits",
			keyName = "displayTimeSplits",
			description = "- Displays the splits in the room where tracked<br>" +
						  "- Example: [50s - 1:02.4]",
			position = 2,
			section = TIME_SECTION
	)
	default boolean displayTimeSplits()
	{
		return true;
	}

	@ConfigItem(
			name = "Display Split Differences",
			keyName = "displayTimeSplitDifferences",
			description = "- Displays the difference in time between each split<br>" +
					"- Example: [50s - 1:02.4 (0:24.6)]",
			position = 3,
			section = TIME_SECTION
	)
	default boolean displayTimeSplitDifferences()
	{
		return false;
	}

	@ConfigItem(
			name = "Validate Room Completion",
			keyName = "roomTimeValidation",
			description = "- Displays the room completion time that the plugin tracks independently to validate plugin timing",
			position = 4,
			section = TIME_SECTION
	)
	default boolean roomTimeValidation()
	{
		return false;
	}

	@ConfigItem(
			name = "Shrunk Live Timer Size",
			keyName = "shrunkLiveTimerDesign",
			description = "- Live timer overlay with Plain San Serif at a font size of 11 to minimize the overlay size<br>" +
						"Note: You can use RuneLite's overlay settings in Core 'RuneLite' Settings to configure the overlay",
			position = 5,
			section = TIME_SECTION
	)
	default boolean shrunkLiveTimerDesign()
	{
		return false;
	}

	@ConfigItem(
			name = "Display Total Time",
			keyName = "displayTotalTime",
			description = "- Display an infobox showing the cumulative time of all completed rooms",
			position = 6,
			section = TIME_SECTION
	)
	default boolean displayTotalTime()
	{
		return false;
	}

	/**
	 * Loot Tracking Configs
	 */
	@ConfigItem(
			name = "Dry Loot Tracking",
			keyName = "dryLootTracking",
			description = "- Tracks how many TOB raids you have completed without seeing a purple<br>- This will track regardless of being displayed or not",
			position = 1,
			section = LOOT_TRACKING_SECTION
	)
	default boolean displayDryLootTracking()
	{
		return true;
	}

	@ConfigItem(
			name = "Simplify Loot Tracking",
			keyName = "simplifyLootTracking",
			description = "- Simplifies the chat outputs of each dry loot tracking metrics into a single message",
			position = 2,
			section = LOOT_TRACKING_SECTION
	)
	default boolean simplifyLootTracking()
	{
		return false;
	}
}
