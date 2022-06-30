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

import com.google.common.primitives.Ints;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.function.BiConsumer;

@Singleton
public class SceneManager
{
	private final Client client;
	private final ClientThread clientThread;

	@Inject
	SceneManager(Client client, ClientThread clientThread)
	{
		this.client = client;
		this.clientThread = clientThread;
	}

	public void refreshScene()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		clientThread.invoke(() -> client.setGameState(GameState.LOADING));
	}

	public void forEachTile(int plane, BiConsumer<Scene, Tile> user)
	{
		if (plane < 0 || plane >= 4 || user == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		Scene scene = client.getScene();
		Tile[][] tiles = scene.getTiles()[plane];

		for (int x = 0; x < Constants.SCENE_SIZE; x++)
		{
			for (int y = 0; y < Constants.SCENE_SIZE; y++)
			{
				Tile tile = tiles[x][y];

				if (tile == null)
				{
					continue;
				}

				user.accept(scene, tile);
			}
		}
	}

	public void removeThisTile(int plane, int sceneX, int sceneY)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (plane < 0 || plane >= Constants.MAX_Z)
		{
			return;
		}

		if (sceneX < 0 || sceneX >= Constants.SCENE_SIZE)
		{
			return;
		}

		if (sceneY < 0 || sceneY >= Constants.SCENE_SIZE)
		{
			return;
		}

		if (client.isClientThread())
		{
			client.getScene().getTiles()[plane][sceneX][sceneY] = null;
			return;
		}

		clientThread.invoke(() -> client.getScene().getTiles()[plane][sceneX][sceneY] = null);
	}

	public void removeThisTile(WorldPoint worldPoint)
	{
		if (worldPoint == null)
		{
			return;
		}

		removeThisTile(worldPoint.getPlane(), worldPoint.getX() - client.getBaseX(), worldPoint.getY() - client.getBaseY());
	}

	private void removeTheseGameObjects(int plane, int... gameObjectIds)
	{
		if (gameObjectIds.length == 0 || plane < 0 || plane > 3)
		{
			return;
		}

		forEachTile(plane, (scene, tile) ->
		{
			GameObject[] arr = tile.getGameObjects();

			if (arr == null || arr.length == 0)
			{
				return;
			}

			for (GameObject o : arr)
			{
				if (o == null)
				{
					continue;
				}

				for (int id : gameObjectIds)
				{
					if (o.getId() != id)
					{
						continue;
					}

					scene.removeGameObject(o);
					break;
				}
			}
		});
	}

	public void removeTheseGameObjects(int plane, Collection<Integer> gameObjectIds)
	{
		if (gameObjectIds == null || gameObjectIds.isEmpty())
		{
			return;
		}

		if (client.isClientThread())
		{
			removeTheseGameObjects(plane, Ints.toArray(gameObjectIds));
			return;
		}

		clientThread.invoke(() -> removeTheseGameObjects(plane, Ints.toArray(gameObjectIds)));
	}

	private void removeTheseGroundObjects(int plane, int... groundObjectIds)
	{
		if (plane < 0 || plane > 3)
		{
			return;
		}

		forEachTile(plane, (scene, tile) ->
		{
			GroundObject object = tile.getGroundObject();

			if (object != null)
			{
				tile.setGroundObject(null);
			}
		});
	}

	public void removeTheseGroundObjects(int plane, Collection<Integer> groundObjectIds)
	{
		if (groundObjectIds == null || groundObjectIds.isEmpty() || plane < 0 || plane > 3)
		{
			return;
		}

		if (client.isClientThread())
		{
			removeTheseGroundObjects(plane, Ints.toArray(groundObjectIds));
			return;
		}

		clientThread.invoke(() -> removeTheseGroundObjects(plane, Ints.toArray(groundObjectIds)));
	}
}
