package com.tobqol.api.util;

import com.tobqol.rooms.nylocas.NylocasHandler;
import com.tobqol.rooms.nylocas.config.NylocasRoleSelectionType;
import net.runelite.client.input.MouseAdapter;

import javax.inject.Inject;
import java.awt.event.MouseEvent;

public class TheatreInputListener extends MouseAdapter
{
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
            if (nylocas.getNyloSelectionManager().getMeleeBounds().contains(event.getPoint()))
            {
                nylocas.determineSelection(NylocasRoleSelectionType.MELEE);
            }
            else if (nylocas.getNyloSelectionManager().getRangeBounds().contains(event.getPoint()))
            {
                nylocas.determineSelection(NylocasRoleSelectionType.RANGE);
            }
            else if (nylocas.getNyloSelectionManager().getMageBounds().contains(event.getPoint()))
            {
                nylocas.determineSelection(NylocasRoleSelectionType.MAGE);
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
