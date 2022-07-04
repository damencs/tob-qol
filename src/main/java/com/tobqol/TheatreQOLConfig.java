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
import com.tobqol.rooms.nylocas.config.NylocasRoleSelectionType;
import com.tobqol.rooms.sotetseg.config.SotetsegInstanceTimerTypes;
import com.tobqol.rooms.sotetseg.config.SotetsegProjectileTheme;
import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup(TheatreQOLConfig.GROUP_NAME)
public interface TheatreQOLConfig extends Config
{
	String GROUP_NAME = "tobqol";
	String PLUGIN_VERSION = "1.0.1";
	String GITHUB_LINK = "damencs/tob-qol/issues";

	@ConfigItem(
			name = "<html><b><font color=#ff6961>Version: <font color=#00aeef>" + PLUGIN_VERSION + "-PH</b><br><br></html>",
			keyName = "version",
			description = "",
			position = 0
	)
	void version();

	/**
	 * Sections
	 */
	@ConfigSection(
			name = "<html><font color=#ff6961>General",
			description = "<font color=#d3d3d3>Configuration settings for things that are not room-specific",
			position = 1,
			closedByDefault = true
	)
	String GENERAL_SECTION = "generalSection";

	@ConfigSection(
			name = "<html><font color=#FF6961>The Maiden of Sugadinti",
			description = "<font color=#D3D3D3>Configuration settings for The Maiden of Sugadinti</html>",
			position = 2,
			closedByDefault = true
	)
	String MAIDEN_SECTION = "maidenSection";

	@ConfigSection(
			name = "<html><font color=#ff6961>Pestilent Bloat",
			description = "<font color=#d3d3d3>Configuration settings for Pestilent Bloat",
			position = 3,
			closedByDefault = true
	)
	String BLOAT_SECTION = "bloatSection";

	@ConfigSection(
			name = "<html><font color=#ff6961>Nylocas",
			description = "<font color=#d3d3d3>Configuration settings for Nylocas",
			position = 4,
			closedByDefault = true
	)
	String NYLO_SECTION = "nyloSection";

	@ConfigSection(
			name = "<html><font color=#ff6961>Sotetseg",
			description = "<font color=#d3d3d3>Configuration settings for Sotetseg",
			position = 5,
			closedByDefault = true
	)
	String SOTETSEG_SECTION = "sotetsegSection";

	@ConfigSection(
			name = "<html><font color=#ff6961>Xarpus",
			description = "<font color=#d3d3d3>Configuration settings for Xarpus",
			position = 6,
			closedByDefault = true
	)
	String XARPUS_SECTION = "xarpusSection";

	@ConfigSection(
			name = "<html><font color=#ff6961>Verzik Vitur",
			description = "<font color=#d3d3d3>Configuration settings for Verzik Vitur",
			position = 7,
			closedByDefault = true
	)
	String VERZIK_SECTION = "verzikSection";

	/**
	 * Maintainer Section
	 */
	@ConfigItem(
			name =  "<html><br><b>Maintained By:</b><br>" +
					"<font color=#d3d3d3>Damen (Damen#9999)<br>" +
					"<font color=#ff6961>gh: " + GITHUB_LINK +
					"</html>",
			keyName = "createdBy",
			description = "",
			position = 99
	)
	void createdBy();

	/**
	 * General Section
	 */
	@ConfigItem(
			name = "Bank-all MES Loot Chest",
			keyName = "bankAllMES",
			description = "<font color=#ff6961>- Removes the 'Force Right Click' flag from the [Bank-all] option inside the Monumental Chest in the Loot Room",
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
			description = "<font color=#ff6961>- Swaps to the preferred purchasing quantity within the supply chests<br>" +
							"<font color=#00aeef>~ Credit - gh: WLoumakis",
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
			description = "<font color=#ff6961>- Removes the darkness lighting within Ver Sinhaza (ToB Bank Area)<br>" +
							"<font color=#00aeef>~ Credit - gh: Broooklyn",
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
			description = "<font color=#ff6961>- Indicates whether or not the chest has loot in it by highlighting the chest and putting a message on the entrance<br>" +
							"<font color=#00aeef>~ Credit - gh: BickusDiggus",
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
			description = "<font color=#ff6961>- Set a color for the Loot Reminder overlay<br>" +
							"<font color=#00aeef>~ Credit - gh: BickusDiggus",
			position = 5,
			section = GENERAL_SECTION
	)
	@Alpha
	default Color lootReminderColor()
	{
		return new Color(196, 89, 89, 200);
	}

	/**
	 * Maiden Configs
	 */
	@ConfigItem(
			name = "Display Crabs Health",
			keyName = "maidenCrabHPDisplayType",
			description = "<font color=#ff6961>- Displays the hitpoints percentage or the hitpoints of each alive Nylocas Matomenos",
			position = 1,
			section = MAIDEN_SECTION
	)
	default HPDisplayTypes getMaidenCrabHPType()
	{
		return HPDisplayTypes.OFF;
	}

	@ConfigItem(
			name = "Show Leaks",
			keyName = "maidenLeaks",
			description = "<font color=#ff6961>- Sends a client message per leak showing what leaked, their hp and on what Maiden phase",
			position = 2,
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
			name = "Null Bloat Tank Tiles",
			keyName = "nullTankTiles",
			description = "<font color=#ff6961>- Nulls the top of the Bloat tank tiles which causes potential character misplacement if shown<br>" +
							"<font color=#d3d3d3>* Disabling this feature whilst in Bloat will cause a stutter to refresh the scene",
			position = 1,
			section = BLOAT_SECTION
	)
	default boolean shouldNullTopTankTiles()
	{
		return false;
	}

	@ConfigItem(
			name = "Hide Ceiling Chains",
			keyName = "hideCeilingChains",
			description = "<font color=#ff6961>- Hides the chains hanging from the ceiling in the Bloat room<br>" +
							"<font color=#d3d3d3>* Disabling this feature whilst in Bloat will cause a stutter to refresh the scene",
			position = 2,
			section = BLOAT_SECTION
	)
	default boolean shouldNullCeilingChains()
	{
		return false;
	}

	/**
	 * Nylocas Configs
	 */
	@ConfigItem(
			name = "Pillar HP",
			keyName = "nyloPillarHP",
			description = "<font color=#ff6961>- Display the health of each pillar",
			position = 1,
			section = NYLO_SECTION
	)
	default boolean showNylocasPillarHP()
	{
		return false;
	}

	@ConfigItem(
			name = "Hide Pillars",
			keyName = "nyloHidePillars",
			description = "<font color=#ff6961>- Hides the Nylocas Pillars in the Nylocas Room<br>" +
							"<font color=#d3d3d3>* Disabling this feature whilst in the Nylocas room will cause a stutter to refresh the scene",
			position = 2,
			section = NYLO_SECTION
	)
	default boolean nyloHidePillars()
	{
		return false;
	}

	@ConfigItem(
			name = "Recolor Menu",
			keyName = "nyloWavesRecolorMenu",
			description = "<font color=#ff6961>- Recolors the each entry in the menu to their respective color<br>" +
					"<font color=#d3d3d3>* Gray: Melee (Nylocas Ischyros)<br>" +
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
			description = "<font color=#ff6961>- Darkens the color on the menu if the Nylocas is big",
			position = 4,
			section = NYLO_SECTION
	)
	default boolean nyloWavesRecolorBigsMenuDarker()
	{
		return false;
	}

	@ConfigItem(
			name = "Role Selector",
			keyName = "nyloRoleSelector",
			description = "<font color=#ff6961>- Shows the Nylocas Room Role Selection Overlay that you can use to highlight the tiles of your role's nylo's",
			position = 5,
			section = NYLO_SECTION
	)
	default boolean displayNyloRoleSelector()
	{
		return false;
	}

	@ConfigItem(
			name = "",
			keyName = "nyloRoleSelector",
			description = "",
			hidden = true
	)
	void setNyloRoleSelector(NylocasRoleSelectionType type);

	@ConfigItem(
			name = "",
			keyName = "nyloRoleSelected",
			description = "",
			hidden = true
	)
	default NylocasRoleSelectionType nyloRoleSelected()
	{
		return NylocasRoleSelectionType.NONE;
	}

	@ConfigItem(
			name = "Bigs SW Tile",
			keyName = "nyloWavesBigsSWTile",
			description = "<font color=#ff6961>- Display the SW Tile of big nylos",
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
			description = "<font color=#ff6961>- Displays the tick cycle for Nylocas' instance",
			position = 7,
			section = NYLO_SECTION
	)
	default boolean nyloInstanceTimer()
	{
		return false;
	}

	/**
	 * Sotetseg Configs
	 */
	@ConfigItem(
			name = "Instance Timer",
			keyName = "sotetsegInstanceTimer",
			description = "<font color=#ff6961>- Displays the tick cycle for Sotetsegs' instance<br>" +
					"<font color=#d3d3d3>* Pre-Start: Displays before the room starts<br>" +
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
			description = "<font color=#ff6961>- Modifies the Projectile to appear in a specific theme to assist with colorblind users<br>" +
					"<font color=#d3d3d3>* Inferno: Utilizes the blob mage and range projectiles",
			position = 2,
			section = SOTETSEG_SECTION
	)
	default SotetsegProjectileTheme getSotetsegProjectileTheme()
	{
		return SotetsegProjectileTheme.DEFAULT;
	}

	@ConfigItem(
			name = "<html><font color=#ff6961>Zuk Ball for Death Ball",
			keyName = "infernoThemeZukBall",
			description = "<font color=#ff6961>- Use the Zuk ball to replace Sotetseg's death ball when the Inferno theme is selected",
			position = 3,
			section = SOTETSEG_SECTION
	)
	default boolean infernoThemeZukBall()
	{
		return true;
	}

	@ConfigItem(
			name = "Hide White Screen",
			keyName = "sotetsegHideWhiteScreen",
			description = "<font color=#ff6961>- Hides the transitional white screen during Sotetseg maze phase procs.",
			position = 4,
			section = SOTETSEG_SECTION
	)
	default boolean hideSotetsegWhiteScreen()
	{
		return false;
	}

	/**
	 * Xarpus Configs
	 */
	@ConfigItem(
			name = "Instance Timer",
			keyName = "xarpusInstanceTimer",
			description = "<font color=#ff6961>- Displays Xarpus's tick cycle for the initial exhumed spawn. Enter on 0 to start exhumeds as soon as possible.",
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
			description = "<font color=#ff6961>- Mutes the Xarpus hardmode entrance noise when poison splats are thrown",
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
			description = "<font color=#ff6961>- Replaces the Screech sound effect with a Sheesh sound clip<br>" +
							"<font color=#00aeef>~ Credit - Boris<br>" +
							"<font color=#d3d3d3>* Thank you Hoyaa for providing this sound clip for the project",
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
			description = "<font color=#ff6961>- Sets the volume of the sound clip",
			position = 4,
			section = XARPUS_SECTION
	)
	default int xarpusSoundClipVolume()
	{
		return 65;
	}

	/**
	 * Verzik Configs
	 */
	@ConfigItem(
			name = "Verzik Reds Health Overlay",
			keyName = "verzikReds",
			description = "<font color=#ff6961>- Displays the health of red crabs during Verzik",
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
			description = "<font color=#ff6961>- Highlight Verzik tornadoes",
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
			description = "<font color=#ff6961>- Set the color of the marked tornadoes overlay",
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
			description = "<font color=#ff6961>- Mute Verzik's sounds such as P2 area affect and her walking in P3",
			position = 4,
			section = VERZIK_SECTION
	)
	default boolean muteVerzikSounds()
	{
		return false;
	}
}
