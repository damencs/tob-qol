package com.tobqol.rooms.bloat;

import com.tobqol.TestUtils;
import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Instance;
import com.tobqol.api.game.SceneManager;
import com.tobqol.rooms.bloat.commons.BloatConstants;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.BeforeRender;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import org.junit.Before;
import org.junit.Test;

import java.awt.Color;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BloatHandlerTest
{
	private TheatreQOLPlugin plugin;
	private TheatreQOLConfig config;
	private ConfigManager configManager;
	private Client client;
	private PluginManager pluginManager;
	private SceneManager sceneManager;
	private TestBloatHandler handler;

	@Before
	public void setUp()
	{
		plugin = new TheatreQOLPlugin();
		config = mock(TheatreQOLConfig.class);
		configManager = mock(ConfigManager.class);
		client = mock(Client.class);
		pluginManager = mock(PluginManager.class);
		sceneManager = mock(SceneManager.class);

		plugin.configManager = configManager;
		handler = new TestBloatHandler(plugin, config);

		Instance instance = mock(Instance.class);
		when(instance.isInRaid()).thenReturn(false);

		TestUtils.setField(handler, "client", client);
		TestUtils.setField(handler, "pluginManager", pluginManager);
		TestUtils.setField(handler, "sceneManager", sceneManager);
		TestUtils.setField(handler, "instance", instance);
	}

	@Test
	public void resolveSkyboxColorUsesExistingHdOverride()
	{
		when(configManager.getConfiguration("hd", "overrideSky", Boolean.class)).thenReturn(Boolean.TRUE);
		when(configManager.getConfiguration("hd", "defaultSkyColor")).thenReturn("HD2008");

		int color = (Integer) TestUtils.invoke(handler, "resolveBloatSkyboxColor", new Class<?>[0]);

		assertEquals(new Color(200, 192, 169).getRGB(), color);
	}

	@Test
	public void resolveSkyboxColorUsesSkyboxPluginColor()
	{
		Plugin skyboxPlugin = mock(Plugin.class);
		Color caveColor = new Color(12, 34, 56);

		when(configManager.getConfiguration("hd", "overrideSky", Boolean.class)).thenReturn(Boolean.FALSE);
		when(configManager.getConfiguration("skybox", "customOtherColor", Color.class)).thenReturn(caveColor);
		when(skyboxPlugin.getName()).thenReturn("Skybox");
		when(pluginManager.getPlugins()).thenReturn(Collections.singletonList(skyboxPlugin));
		when(pluginManager.isPluginActive(skyboxPlugin)).thenReturn(true);

		int color = (Integer) TestUtils.invoke(handler, "resolveBloatSkyboxColor", new Class<?>[0]);

		assertEquals(caveColor.getRGB(), color);
	}

	@Test
	public void resolveSkyboxColorFallsBackToBlackWithoutGraphicsPlugins()
	{
		when(configManager.getConfiguration("hd", "overrideSky", Boolean.class)).thenReturn(Boolean.FALSE);
		when(pluginManager.getPlugins()).thenReturn(Collections.<Plugin>emptyList());

		int color = (Integer) TestUtils.invoke(handler, "resolveBloatSkyboxColor", new Class<?>[0]);

		assertEquals(Color.BLACK.getRGB(), color);
	}

	@Test
	public void enteringAndLeavingBloatTemporarilyOverrides117Hd()
	{
		Plugin hdPlugin = mock(Plugin.class);

		when(config.bloatSkyboxOverride()).thenReturn(true);
		when(config.hideBloatFloor()).thenReturn(true);
		when(config.shouldNullCeilingChains()).thenReturn(false);
		when(client.getPlane()).thenReturn(0);
		when(hdPlugin.getName()).thenReturn("117 HD");
		when(pluginManager.getPlugins()).thenReturn(Collections.singletonList(hdPlugin));
		when(pluginManager.isPluginActive(hdPlugin)).thenReturn(true);
		when(configManager.getConfiguration("hd", "overrideSky", Boolean.class)).thenReturn(Boolean.FALSE);
		when(configManager.getConfiguration("hd", "defaultSkyColor")).thenReturn("OSRS");

		handler.setActive(false);
		TestUtils.invoke(handler, "onGameTick", new Class<?>[]{net.runelite.api.events.GameTick.class}, new Object[]{null});

		handler.setActive(true);
		TestUtils.invoke(handler, "onGameTick", new Class<?>[]{net.runelite.api.events.GameTick.class}, new Object[]{null});

		verify(sceneManager).removeTheseGroundObjects(eq(0), eq(BloatConstants.BLOAT_FLOOR));
		verify(configManager).setConfiguration("hd", "defaultSkyColor", "RUNELITE");
		verify(configManager).setConfiguration("hd", "overrideSky", true);

		handler.setActive(false);
		TestUtils.invoke(handler, "onGameTick", new Class<?>[]{net.runelite.api.events.GameTick.class}, new Object[]{null});

		verify(sceneManager).refreshScene();
		verify(configManager).setConfiguration("hd", "defaultSkyColor", "OSRS");
		verify(configManager).unsetConfiguration("hd", "overrideSky");
		verify(client).setSkyboxColor(0);
	}

	@Test
	public void enteringBloatDoesNotTouchHdConfigWhen117HdIsInactive()
	{
		when(config.bloatSkyboxOverride()).thenReturn(true);
		when(config.hideBloatFloor()).thenReturn(false);
		when(config.shouldNullCeilingChains()).thenReturn(false);
		when(pluginManager.getPlugins()).thenReturn(Collections.<Plugin>emptyList());

		handler.setActive(false);
		TestUtils.invoke(handler, "onGameTick", new Class<?>[]{net.runelite.api.events.GameTick.class}, new Object[]{null});

		handler.setActive(true);
		TestUtils.invoke(handler, "onGameTick", new Class<?>[]{net.runelite.api.events.GameTick.class}, new Object[]{null});

		verify(configManager, never()).setConfiguration("hd", "defaultSkyColor", "RUNELITE");
		verify(configManager, never()).setConfiguration("hd", "overrideSky", true);
	}

	@Test
	public void beforeRenderAppliesResolvedSkyboxColor()
	{
		when(config.bloatSkyboxOverride()).thenReturn(true);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(configManager.getConfiguration("hd", "overrideSky", Boolean.class)).thenReturn(Boolean.TRUE);
		when(configManager.getConfiguration("hd", "defaultSkyColor")).thenReturn("OSRS");
		handler.setActive(true);

		TestUtils.invoke(handler, "onBeforeRender", new Class<?>[]{BeforeRender.class}, new Object[]{null});

		verify(client).setSkyboxColor(Color.BLACK.getRGB());
	}

	private static final class TestBloatHandler extends BloatHandler
	{
		private boolean active;

		private TestBloatHandler(TheatreQOLPlugin plugin, TheatreQOLConfig config)
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
