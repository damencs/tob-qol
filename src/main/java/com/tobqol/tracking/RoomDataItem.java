package com.tobqol.tracking;

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
