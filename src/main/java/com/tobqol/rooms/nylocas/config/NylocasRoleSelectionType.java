package com.tobqol.rooms.nylocas.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Getter
@Slf4j
public enum NylocasRoleSelectionType
{
	OFF("OFF"),
	ON("ON"),
	MAGE("MAGE"),
	MELEE("MELEE"),
	RANGE("RANGE");

	private final String option;

	public final boolean isOff()
	{
		return this == OFF;
	}

	public final boolean isOn()
	{
		return this == ON;
	}

	public final boolean isMage()
	{
		return this == MAGE;
	}

	public final boolean isMelee()
	{
		return this == MELEE;
	}

	public final boolean isRange()
	{
		return this == RANGE;
	}

	public final boolean isAnyRole()
	{
		return (this == MAGE || this == MELEE || this == RANGE);
	}

	public final boolean isAnyOrOn()
	{
		return (this == ON || this == MAGE || this == MELEE || this == RANGE);
	}

	@Override
	public String toString()
	{
		return option;
	}
}
