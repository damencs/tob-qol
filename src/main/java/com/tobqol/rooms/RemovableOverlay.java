package com.tobqol.rooms;

import com.tobqol.TheatreQOLConfig;

import net.runelite.client.ui.overlay.Overlay;

public interface RemovableOverlay
{
	Overlay provideOverlay();
	boolean remove(TheatreQOLConfig config);
}
