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
package com.tobqol.rooms.xarpus.commons;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GroundObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ExhumedTracker
{
	@Getter
    private final Map<Long, GroundObject> exhumeds;

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

        exhumeds.put(hash, obj);
		return true;
	}

    public void untrack(GroundObject obj)
    {
        if (obj == null || obj.getId() != XarpusTable.EXHUMED_GROUND_OBJ)
        {
            return;
        }

        long hash = obj.getHash();
        exhumeds.remove(hash);
    }

    public Collection<GroundObject> getExhumeds() {
        return exhumeds.values();
    }
}
