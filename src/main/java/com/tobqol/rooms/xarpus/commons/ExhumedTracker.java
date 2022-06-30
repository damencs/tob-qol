package com.tobqol.rooms.xarpus.commons;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.GroundObject;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
public class ExhumedTracker
{
	@Getter
	private final Map<Long, Pair<GroundObject, Integer>> exhumeds;

	public ExhumedTracker()
	{
		this.exhumeds = new HashMap<>();
	}

	public boolean track(GroundObject obj)
	{
		if (obj == null || obj.getId() != XarpusTable.EXHUMED_GROUND_OBJ)
		{
			return false;
		}

		long hash = obj.getHash();

		if (exhumeds.containsKey(hash))
		{
			return false;
		}

		exhumeds.put(hash, Pair.of(obj, 12));
		return true;
	}

	public void tick()
	{
		exhumeds.values().removeIf(p -> p.getRight() <= 0);
		exhumeds.replaceAll((k, v) -> Pair.of(v.getLeft(), v.getRight() - 1));
	}

	public void forEachExhumed(BiConsumer<GroundObject, Integer> action)
	{
		if (action == null || exhumeds.isEmpty())
		{
			return;
		}

		Collection<Pair<GroundObject, Integer>> exhumeds = this.exhumeds.values();

		if (exhumeds.isEmpty())
		{
			return;
		}

		exhumeds.forEach(p -> action.accept(p.getLeft(), p.getRight()));
	}
}
