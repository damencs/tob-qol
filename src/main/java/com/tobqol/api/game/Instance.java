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

import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Instance
{
	enum Mode
	{
		STORY, REGULAR, HARD;

		public boolean isStoryMode()
		{
			return this == STORY;
		}

		public boolean isRegularMode()
		{
			return this == REGULAR;
		}

		public boolean isHardMode()
		{
			return this == HARD;
		}
	}

	@Nullable
	static Mode findFirstMode(Predicate<Mode> filter)
	{
		if (filter == null)
		{
			return null;
		}

		for (Mode mode : Mode.values())
		{
			if (filter.test(mode))
			{
				return mode;
			}
		}

		return null;
	}

	// Lazy set mode for performance, don't need to recall heavy functions on an already determined instanced-mode.
	boolean lazySetMode(Supplier<Mode> modeSupplier);
	@Nullable
	Mode mode();

	boolean isStoryMode();
	boolean isRegularMode();
	boolean isHardMode();

	Region getCurrentRegion();
	int getRaidStatus();
	int getRoomStatus();

	int getPartySize();
	int getDeathSize();
	int getTotalAlive();

	int getTickCycle();
	void resetTickCycle();
}
