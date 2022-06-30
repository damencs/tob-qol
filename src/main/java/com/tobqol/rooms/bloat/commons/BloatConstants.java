package com.tobqol.rooms.bloat.commons;

import com.google.common.collect.ImmutableList;

public interface BloatConstants
{
	String BOSS_NAME = "Pestilent Bloat";
	String BOSS_NAME_SIMPLE = "Bloat";

	ImmutableList<Integer> TANK = ImmutableList.of(32957, 32955, 32959, 32960, 32964, 33084);                   // GameObjects
	ImmutableList<Integer> TOP_OF_TANK = ImmutableList.of(32958, 32962, 32964, 32965, 33062);                   // GameObjects
	ImmutableList<Integer> CEILING_CHAINS = ImmutableList.of(32949, 32950, 32951, 32952, 32953, 32954, 32970);  // GameObjects
	ImmutableList<Integer> BLOAT_FLOOR = ImmutableList.of(32941, 32942, 32944, 32946, 32948);                   // GroundObjects
}
