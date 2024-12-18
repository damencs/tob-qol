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
package com.tobqol.timetracking;

import lombok.Getter;
import lombok.Setter;

public class RoomDataItem implements Comparable
{
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private int value;

    @Getter
    @Setter
    private int sort;

    @Getter
    @Setter
    private boolean hidden;

    @Getter
    @Setter
    private String compareName;

    @Getter
    @Setter
    private boolean exception;

    public RoomDataItem(String name, int value)
    {
        this.name = name;
        this.value = value;
        this.sort = -1;
        this.hidden = false;
        this.compareName = null;
        this.exception = false;
    }

    public RoomDataItem(String name, int value, boolean hidden)
    {
        this.name = name;
        this.value = value;
        this.sort = -1;
        this.hidden = hidden;
        this.compareName = "";
        this.exception = false;
    }

    public RoomDataItem(String name, int value, int sort, boolean hidden)
    {
        this.name = name;
        this.value = value;
        this.sort = sort;
        this.hidden = hidden;
        this.compareName = "";
        this.exception = false;
    }

    public RoomDataItem(String name, int value, int sort, boolean hidden, String compareName)
    {
        this.name = name;
        this.value = value;
        this.sort = sort;
        this.hidden = hidden;
        this.compareName = compareName;
        this.exception = false;
    }

    public RoomDataItem(String name, int value, boolean hidden, boolean exception)
    {
        this.name = name;
        this.value = value;
        this.sort = -1;
        this.hidden = hidden;
        this.compareName = "";
        this.exception = exception;
    }

    public RoomDataItem(String name, int value, int sort, boolean hidden, String compareName, boolean exception)
    {
        this.name = name;
        this.value = value;
        this.sort = sort;
        this.hidden = hidden;
        this.compareName = compareName;
        this.exception = exception;
    }

    @Override
    public int compareTo(Object comparesTo)
    {
        return this.sort - ((RoomDataItem)comparesTo).getSort();
    }

    @Override
    public String toString()
    {
        return "Name: " + name + ", Value: " + value + ", Sort: " + sort + ", Hidden: " + hidden + ", Compared Key Name: " + compareName + ", Exception: " + exception;
    }
}
