package com.tobqol;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ThreatreQOLTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TheatreQOLPlugin.class);
		RuneLite.main(args);
	}
}