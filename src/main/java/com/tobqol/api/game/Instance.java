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
