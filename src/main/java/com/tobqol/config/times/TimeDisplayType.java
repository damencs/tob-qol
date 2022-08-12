package com.tobqol.config.times;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TimeDisplayType
{
    OFF("Off"),
    CHAT("Chat"),
    INFOBOX("Infobox"),
    LIVE_OVERLAY("Live Overlay"),
    CHAT_AND_INFOBOX("Chat & Infobox"),
    LIVE_OVERLAY_AND_CHAT("Live & Chat"),
    LIVE_OVERLAY_AND_INFOBOX("Live & Infobox"),
    ALL("ALL");

    private final String type;

    @Override
    public String toString()
    {
        return type;
    }

    public boolean isOff()
    {
        return this == OFF;
    }

    public boolean isChat()
    {
        return this == CHAT || this == CHAT_AND_INFOBOX || this == LIVE_OVERLAY_AND_CHAT || this == ALL;
    }

    public boolean isInfobox()
    {
        return this == INFOBOX || this == CHAT_AND_INFOBOX || this == LIVE_OVERLAY_AND_INFOBOX || this == ALL;
    }

    public boolean isLiveOverlay()
    {
        return this == LIVE_OVERLAY || this == LIVE_OVERLAY_AND_CHAT || this == LIVE_OVERLAY_AND_INFOBOX || this == ALL;
    }

    public boolean isAll()
    {
        return this == ALL;
    }

    public boolean isAny()
    {
        return this == CHAT || this == INFOBOX || this == CHAT_AND_INFOBOX || this == LIVE_OVERLAY || this == LIVE_OVERLAY_AND_CHAT || this == LIVE_OVERLAY_AND_INFOBOX || this == ALL;
    }
}
