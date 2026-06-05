package com.tobqol.api.game;

import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.client.callback.ClientThread;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SceneManagerTest
{
	private Client client;
	private SceneManager sceneManager;

	@Before
	public void setUp()
	{
		client = mock(Client.class);
		sceneManager = new SceneManager(client, mock(ClientThread.class));
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.isClientThread()).thenReturn(true);
	}

	@Test
	public void removeTheseGroundObjectsOnlyRemovesMatchingIds()
	{
		Tile[][][] tiles = new Tile[Constants.MAX_Z][Constants.SCENE_SIZE][Constants.SCENE_SIZE];
		Tile matchingTile = mock(Tile.class);
		Tile otherTile = mock(Tile.class);
		GroundObject matchingObject = mock(GroundObject.class);
		GroundObject otherObject = mock(GroundObject.class);
		Scene scene = mock(Scene.class);

		when(matchingObject.getId()).thenReturn(1001);
		when(otherObject.getId()).thenReturn(2002);
		when(matchingTile.getGroundObject()).thenReturn(matchingObject);
		when(otherTile.getGroundObject()).thenReturn(otherObject);

		tiles[0][0][0] = matchingTile;
		tiles[0][1][1] = otherTile;

		when(scene.getTiles()).thenReturn(tiles);
		when(client.getScene()).thenReturn(scene);

		sceneManager.removeTheseGroundObjects(0, Collections.singletonList(1001));

		verify(matchingTile).setGroundObject(null);
		verify(otherTile, never()).setGroundObject(null);
	}
}
