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
package com.tobqol.api.util;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.rooms.nylocas.NylocasHandler;
import net.runelite.client.input.MouseAdapter;

import javax.inject.Inject;
import java.awt.event.MouseEvent;

public class TheatreInputListener extends MouseAdapter
{
    @Inject
    private TheatreQOLConfig config;

    @Inject
    private NylocasHandler nylocas;

    @Override
    public MouseEvent mouseReleased(MouseEvent event)
    {
        if (nylocas.getNyloSelectionManager().isHidden())
        {
            return event;
        }

        if (nylocas.getNyloSelectionManager().getBounds().contains(event.getPoint()))
        {
            event.consume();
            return event;
        }

        return event;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event)
    {
        if (nylocas.getNyloSelectionManager().isHidden())
        {
            return event;
        }

        if (nylocas.getNyloSelectionManager().getBounds().contains(event.getPoint()))
        {
            event.consume();
            return event;
        }

        return event;
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event)
    {
        if (nylocas.getNyloSelectionManager().isHidden())
        {
            return event;
        }

        if (event.getButton() == MouseEvent.BUTTON1 && nylocas.getNyloSelectionManager().getBounds().contains(event.getPoint()))
        {
            boolean updated;

            if (nylocas.getNyloSelectionManager().getMeleeBounds().contains(event.getPoint()))
            {
                updated = !config.nyloRoleSelectedMelee();
                config.nyloSetRoleSelectedMelee(updated);
                nylocas.getNyloSelectionManager().getMelee().setSelected(updated);
                nylocas.setDisplayRoleMelee(updated);
            }
            else if (nylocas.getNyloSelectionManager().getRangeBounds().contains(event.getPoint()))
            {
                updated = !config.nyloRoleSelectedRange();
                config.nyloSetRoleSelectedRange(updated);
                nylocas.getNyloSelectionManager().getRange().setSelected(updated);
                nylocas.setDisplayRoleRange(updated);
            }
            else if (nylocas.getNyloSelectionManager().getMageBounds().contains(event.getPoint()))
            {
                updated = !config.nyloRoleSelectedMage();
                config.nyloSetRoleSelectedMage(updated);
                nylocas.getNyloSelectionManager().getMage().setSelected(updated);
                nylocas.setDisplayRoleMage(updated);
            }

            event.consume();
        }
        return event;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event)
    {
        if (nylocas.getNyloSelectionManager().isHidden())
        {
            return event;
        }

        nylocas.getNyloSelectionManager().getMelee().setHovered(false);
        nylocas.getNyloSelectionManager().getRange().setHovered(false);
        nylocas.getNyloSelectionManager().getMage().setHovered(false);

        if (nylocas.getNyloSelectionManager().getBounds().contains(event.getPoint()))
        {
            if (nylocas.getNyloSelectionManager().getMeleeBounds().contains(event.getPoint()))
            {
                nylocas.getNyloSelectionManager().getMelee().setHovered(true);
            }
            else if (nylocas.getNyloSelectionManager().getRangeBounds().contains(event.getPoint()))
            {
                nylocas.getNyloSelectionManager().getRange().setHovered(true);
            }
            else if (nylocas.getNyloSelectionManager().getMageBounds().contains(event.getPoint()))
            {
                nylocas.getNyloSelectionManager().getMage().setHovered(true);
            }
        }
        return event;
    }
}
