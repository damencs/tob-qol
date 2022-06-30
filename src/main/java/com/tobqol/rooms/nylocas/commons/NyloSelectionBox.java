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
package com.tobqol.rooms.nylocas.commons;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.components.InfoBoxComponent;

import java.awt.*;

@Slf4j
public class NyloSelectionBox extends Overlay
{
    private final InfoBoxComponent component;

    @Getter
    @Setter
    private boolean isSelected = false;

    @Getter
    @Setter
    private boolean isHovered = false;

    public NyloSelectionBox(InfoBoxComponent component)
    {
        this.component = component;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (isSelected)
        {
            component.setColor(Color.GREEN);
            component.setText("On");
        }
        else
        {
            component.setColor(Color.RED);
            component.setText("Off");
        }

        Dimension result = component.render(graphics);

        if (isHovered)
        {
            Color color = graphics.getColor();
            graphics.setColor(new Color(200, 200, 200));
            graphics.drawRect(component.getBounds().x, component.getBounds().y, component.getBounds().width, component.getBounds().height);
            graphics.setColor(color);
        }

        return result;
    }
}