package com.tobqol.rooms.sotetseg.commons.util;

import com.google.common.collect.ImmutableList;

public interface SotetsegConstants
{
	String BOSS_NAME = "Sotetseg";

	int SOTETSEG_MELEE_ANIM = 8138;
	int SOTETSEG_MAGIC_ANIM = 8139;

	int DEATH_BALL = 1604;
	int MAGIC_ORB = 1606;
	int RANGE_ORB = 1607;

	int MAZE_UNDERWORLD_PORTAL = 33037; // GameObject

	int INACTIVE_MAZE_GROUND_OBJ_0 = 33033; // Overworld
	int INACTIVE_MAZE_GROUND_OBJ_1 = 33034; // Underworld
	ImmutableList<Integer> ACTIVE_MAZE_GROUND_OBJS = ImmutableList.of(33035, 41750, 41751, 41752, 41753);

	int INFERNO_RANGE = 1378;
	int INFERNO_MAGE = 1380;
	int INFERNO_DEATH_BALL = 1375;
}
