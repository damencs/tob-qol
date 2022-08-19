package com.tobqol.tracking;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoomInfoUtil
{
    static final int TICK_LENGTH = 600;

    public static String formatTime(int ticks)
    {
        return formatTime(ticks, false);
    }

    public static String formatTime(int current, int previous, boolean precise)
    {
        return " (" + formatTime(current - previous, precise) + ")";
    }

    public static String formatTime(int ticks, boolean precise)
    {
        int millis = ticks * TICK_LENGTH;
        String hundredths = String.valueOf(millis % 1000).substring(0, 1);

        if (precise)
        {
            return String.format("%d:%02d.%s",
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1),
                    hundredths);
        }
        else
        {
            if (hundredths.equals("6") || hundredths.equals("8"))
            {
                millis += 1000;
            }

            return String.format("%d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        }
    }

    public static RoomInfoBox createInfoBox(TheatreQOLPlugin plugin, TheatreQOLConfig config, BufferedImage image, String bossName, String time, String tooltip)
    {
        return new RoomInfoBox(image, plugin, config, bossName, time, tooltip);
    }
}
