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
package com.tobqol.rooms.sotetseg;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import com.tobqol.api.game.Region;
import com.tobqol.rooms.RoomHandler;
import com.tobqol.rooms.sotetseg.commons.MutableMaze;
import com.tobqol.rooms.sotetseg.commons.util.SotetsegTable;
import com.tobqol.rooms.sotetseg.config.SotetsegProjectileTheme;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Projectile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import java.util.Optional;

import static com.tobqol.rooms.sotetseg.commons.util.SotetsegConstants.*;
import static com.tobqol.rooms.sotetseg.commons.util.SotetsegTable.SOTETSEG_CLICKABLE;

@Slf4j
public class SotetsegHandler extends RoomHandler
{
	@Inject private SotetsegSceneOverlay sceneOverlay;

	@Getter
	@CheckForNull
	private NPC sotetsegNpc = null;

	@Getter
	private boolean clickable = false;

	@Getter
	@CheckForNull
	private MutableMaze maze = null;

	@Getter
	@CheckForNull
	private GameObject portal = null;

	@Inject
	protected SotetsegHandler(TheatreQOLPlugin plugin, TheatreQOLConfig config)
	{
		super(plugin, config);
		setRoomRegion(Region.SOTETSEG);
	}

	@Override
	public void load()
	{
		overlayManager.add(sceneOverlay);
	}

	@Override
	public void unload()
	{
		overlayManager.remove(sceneOverlay);
		reset();
	}

	@Override
	public boolean active()
	{
		return instance.getCurrentRegion().isSotetseg() && sotetsegNpc != null && !sotetsegNpc.isDead();
	}

	@Override
	public void reset()
	{
		sotetsegNpc = null;
		clickable = false;
		maze = null;
		portal = null;
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned e)
	{
		isNpcFromName(e.getNpc(), BOSS_NAME, n ->
		{
			instance.lazySetMode(() -> SotetsegTable.findMode(n.getId()));
			sotetsegNpc = n;
			clickable = SotetsegTable.anyMatch(SOTETSEG_CLICKABLE, n.getId());
		});
	}

	@Subscribe
	private void onNpcChanged(NpcChanged e)
	{
		if (!active())
		{
			return;
		}

		isNpcFromName(e.getNpc(), BOSS_NAME, n ->
		{
			if (!(clickable = SotetsegTable.anyMatch(SOTETSEG_CLICKABLE, n.getId())))
			{
				log.debug("[{}] - Sotetseg Changed NPC IDs -> Clickable: {}", client.getTickCount(), clickable);
			}
		});
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned e)
	{
		if (!active() || client.getPlane() != 0)
		{
			return;
		}

		isNpcFromName(e.getNpc(), BOSS_NAME, n -> reset());
	}

	@Subscribe(priority = -7)
	private void onScriptPostFired(ScriptPostFired e)
	{
		if (!config.hideSotetsegWhiteScreen() || !client.isInInstancedRegion() || !instance.getCurrentRegion().isSotetseg())
		{
			return;
		}

		if (e.getScriptId() == 2312 && instance.getTotalAlive() > 0 && instance.getRoomStatus() > 0)
		{
			Optional.ofNullable(client.getWidget(28 << 16 | 1)).ifPresent(parent ->
			{
				try
				{
					Widget child = parent.getDynamicChildren()[1];
					child.setOpacity(0xFF);
				}
				catch (Exception ex)
				{
					log.debug("Something went wrong whilst trying to hide Sotetseg's 'White Screen' - Error: {}", ex.getMessage());
				}
			});
		}
	}

	@Subscribe
	private void onGroundObjectSpawned(GroundObjectSpawned e)
	{
		if (!active())
		{
			return;
		}

		GroundObject obj = e.getGroundObject();

		if (SotetsegTable.isActiveMazeObject(obj))
		{
			if (maze == null)
			{
				maze = new MutableMaze(instance);
			}

			WorldPoint wp = WorldPoint.fromLocal(client, e.getTile().getLocalLocation());
			maze.addPoint(wp.getRegionX(), wp.getRegionY());
		}
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned e)
	{
		if (!active() || portal != null)
		{
			return;
		}

		GameObject obj = e.getGameObject();

		if (obj.getId() == SotetsegTable.MAZE_UNDERWORLD_PORTAL)
		{
			portal = obj;
		}
	}

	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned e)
	{
		if (!active() || portal == null)
		{
			return;
		}

		if (e.getGameObject().getId() == SotetsegTable.MAZE_UNDERWORLD_PORTAL)
		{
			portal = null;
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved projectileMoved)
	{
		if (!active())
		{
			return;
		}

		SotetsegProjectileTheme theme = config.getSotetsegProjectileTheme();

		Projectile projectile = projectileMoved.getProjectile();
		int projectileId = projectile.getId();

		if (!theme.isDefault() && (projectileId != DEATH_BALL || (projectileId == DEATH_BALL && theme.isInfernoTheme())))
		{
			int replacement = -1;

			switch (projectileId)
			{
				case RANGE_ORB:
				{
					switch (theme)
					{
						case INFERNO:
							replacement = INFERNO_RANGE;
							break;
					}
					break;
				}
				case MAGIC_ORB:
				{
					switch (theme)
					{
						case INFERNO:
							replacement = INFERNO_MAGE;
							break;
					}
					break;
				}
				case DEATH_BALL:
				{
					switch (theme)
					{
						case INFERNO:
							if (config.infernoThemeZukBall())
							{
								replacement = INFERNO_DEATH_BALL;
								break;
							}

					}
				}
			}

			if (replacement == -1)
			{
				return;
			}

			Projectile p = client.createProjectile(replacement,
					projectile.getFloor(),
					projectile.getX1(), projectile.getY1(),
					projectile.getHeight(),
					projectile.getStartCycle(), projectile.getEndCycle(),
					projectile.getSlope(),
					projectile.getStartHeight(), projectile.getEndHeight(),
					projectile.getInteracting(),
					projectile.getTarget().getX(), projectile.getTarget().getY());

			client.getProjectiles().addLast(p);
			projectile.setEndCycle(0);
		}
	}
}
