package com.tobqol.config.times;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TimeDisplayDetail
{
    SIMPLE("Simple"),
    DETAILED("Detailed");

    private final String style;

    @Override
    public String toString()
    {
        return style;
    }

    public boolean isSimple()
    {
        return this == SIMPLE;
    }

    public boolean isDetailed()
    {
        return this == DETAILED;
    }
}
