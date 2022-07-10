package com.tobqol.config.font;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FontStyles
{
    PLAIN("Plain", 0),
    BOLD("Bold", 1),
    ITALIC("Italic", 2),
    BOLD_ITALICIZED("Bold & Italic", 3);

    private final String style;

    @Getter
    private final int value;

    @Override
    public String toString()
    {
        return style;
    }
}
