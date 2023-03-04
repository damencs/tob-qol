package com.tobqol.rooms.sotetseg.commons;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@EqualsAndHashCode(callSuper = true)
public class SotetsegNotification extends PartyMemberMessage
{
    String name;
    boolean ballSpawned;
}
