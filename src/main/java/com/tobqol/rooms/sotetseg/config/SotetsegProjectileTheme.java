package com.tobqol.rooms.sotetseg.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SotetsegProjectileTheme
{
	DEFAULT("Default"),
	INFERNO("Inferno");

	private final String option;

	public final boolean isDefault()
	{
		return this == DEFAULT;
	}

	public final boolean isInfernoTheme()
	{
		return this == INFERNO;
	}

	@Override
	public String toString()
	{
		return option;
	}
}
