package com.example.afktracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("danksafktracker")
public interface AfkTrackerConfig extends Config {
    @Range(min=1,max=60)
    @ConfigItem(keyName="idleDelayMinutes",name="AFK starts after (minutes)",description="No clicks inside the RuneLite client for this long.",position=1)
    default int idleDelayMinutes() { return 5; }

    @ConfigItem(keyName="showOverlay",name="Show overlay",description="Show the AFK tracker on screen.",position=2)
    default boolean showOverlay() { return true; }

    @ConfigItem(keyName="trackMovement",name="Mouse movement resets timer",description="Normally only clicks reset the idle timer.",position=3)
    default boolean trackMovement() { return false; }
}
