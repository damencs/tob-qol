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

import java.awt.*;

@Slf4j
public class NyloSelectionManager extends Overlay
{
    @Getter
    private final NyloSelectionBox melee;

    @Getter
    private final NyloSelectionBox mage;

    @Getter
    private final NyloSelectionBox range;

    @Getter
    @Setter
    private boolean isHidden = true;

    @Getter
    private Rectangle meleeBounds = new Rectangle();

    @Getter
    private Rectangle rangeBounds = new Rectangle();

    @Getter
    private Rectangle mageBounds = new Rectangle();

    public NyloSelectionManager(NyloSelectionBox melee, NyloSelectionBox mage, NyloSelectionBox range)
    {
        this.mage = mage;
        this.melee = melee;
        this.range = range;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (isHidden)
        {
            return null;
        }

        Dimension meleeD = melee.render(graphics);
        graphics.translate(meleeD.width + 1, 0);

        Dimension rangeD = range.render(graphics);
        graphics.translate(rangeD.width + 1, 0);

        Dimension mageD = mage.render(graphics);
        graphics.translate(-meleeD.width - rangeD.width - 2, 0);

        meleeBounds = new Rectangle(getBounds().getLocation(), meleeD);
        rangeBounds = new Rectangle(new Point(getBounds().getLocation().x + meleeD.width + 1, getBounds().y), rangeD);
        mageBounds = new Rectangle(new Point(getBounds().getLocation().x + meleeD.width + 1 + rangeD.width + 1, getBounds().y), mageD);

        return new Dimension(meleeD.width + rangeD.width + mageD.width, Math.max(Math.max(meleeD.height, rangeD.height), mageD.height));
    }
}
