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
