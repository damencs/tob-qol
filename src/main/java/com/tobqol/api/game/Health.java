package com.tobqol.api.game;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;

import java.awt.*;
import java.text.DecimalFormat;

@Getter
public class Health implements Comparable<Health>
{
	private final int base;

	@Setter
	private int current;

	public Health(int base)
	{
		this.base = base;
		this.current = base;
	}

	public boolean zero()
	{
		return current == 0;
	}

	public Health addHealth(int amount)
	{
		current = Math.min(current + amount, base);
		return this;
	}

	public Health removeHealth(int amount)
	{
		current = Math.max(current - amount, 0);
		return this;
	}

	public double percent()
	{
		return (double) current / (double) base;
	}

	public double truncatedPercent()
	{
		return NumberUtils.toDouble(new DecimalFormat("#.0").format(percent() * 100));
	}

	public Color color()
	{
		double percent = percent();

		if (percent > 1)
		{
			percent = 1;
		}
		else if (percent < 0)
		{
			percent = 0;
		}

		int r = (int) (255.0 * (1 - percent));
		int g = (int) (255.0 * percent);
		return new Color(r, g, 0, 0xFF);
	}

	@Override
	public int compareTo(Health o)
	{
		return Integer.compare(current, o.current);
	}
}
