package com.tobqol.rooms.verzik;

import com.tobqol.TestUtils;
import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Instance;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VerzikHandlerTest
{
	private TheatreQOLConfig config;
	private net.runelite.api.Client client;
	private TestVerzikHandler handler;

	@Before
	public void setUp()
	{
		config = mock(TheatreQOLConfig.class);
		client = mock(net.runelite.api.Client.class);
		handler = new TestVerzikHandler(mock(TheatreQOLPlugin.class), config);

		Instance instance = mock(Instance.class);
		when(instance.isInRaid()).thenReturn(false);

		TestUtils.setField(handler, "client", client);
		TestUtils.setField(handler, "instance", instance);
	}

	@Test
	public void preservesEntryCameraForThreeTicks()
	{
		when(config.preserveVerzikEntryCamera()).thenReturn(true);
		when(client.getCameraYawTarget()).thenReturn(640);
		when(client.getCameraPitchTarget()).thenReturn(192);

		handler.setActive(false);
		TestUtils.invoke(handler, "onGameTick", new Class<?>[]{net.runelite.api.events.GameTick.class}, new Object[]{null});

		handler.setActive(true);
		TestUtils.invoke(handler, "onGameTick", new Class<?>[]{net.runelite.api.events.GameTick.class}, new Object[]{null});
		TestUtils.invoke(handler, "onGameTick", new Class<?>[]{net.runelite.api.events.GameTick.class}, new Object[]{null});
		TestUtils.invoke(handler, "onGameTick", new Class<?>[]{net.runelite.api.events.GameTick.class}, new Object[]{null});
		TestUtils.invoke(handler, "onGameTick", new Class<?>[]{net.runelite.api.events.GameTick.class}, new Object[]{null});

		verify(client, org.mockito.Mockito.times(3)).setCameraYawTarget(640);
		verify(client, org.mockito.Mockito.times(3)).setCameraPitchTarget(192);
	}

	@Test
	public void disabledEntryCameraPreservationDoesNotMoveCamera()
	{
		when(config.preserveVerzikEntryCamera()).thenReturn(false);

		handler.setActive(false);
		TestUtils.invoke(handler, "onGameTick", new Class<?>[]{net.runelite.api.events.GameTick.class}, new Object[]{null});

		handler.setActive(true);
		TestUtils.invoke(handler, "onGameTick", new Class<?>[]{net.runelite.api.events.GameTick.class}, new Object[]{null});

		verify(client, never()).setCameraYawTarget(org.mockito.Mockito.anyInt());
		verify(client, never()).setCameraPitchTarget(org.mockito.Mockito.anyInt());
	}

	private static final class TestVerzikHandler extends VerzikHandler
	{
		private boolean active;

		private TestVerzikHandler(TheatreQOLPlugin plugin, TheatreQOLConfig config)
		{
			super(plugin, config);
		}

		@Override
		public boolean active()
		{
			return active;
		}

		private void setActive(boolean active)
		{
			this.active = active;
		}
	}
}
