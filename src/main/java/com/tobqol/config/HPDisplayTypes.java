package com.tobqol.config;

import lombok.RequiredArgsConstructor;
import net.runelite.client.util.ColorUtil;

import java.awt.*;

@RequiredArgsConstructor
public enum HPDisplayTypes
{
	OFF("Off", ColorUtil.fromHex("#FF6961")),
	PERCENT("Percent", ColorUtil.fromHex("#ADCFFF")),
	HITPOINTS("Hitpoints", ColorUtil.fromHex("#FFB347"));

	private final String name;
	private final Color configColorOverride;

	public boolean off()
	{
		return this == OFF;
	}

	public boolean showAsPercent()
	{
		return this == PERCENT;
	}

	public boolean showAsHitpoints()
	{
		return this == HITPOINTS;
	}
}
