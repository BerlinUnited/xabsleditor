package de.naoth.xabsleditor.events;

import java.util.EventObject;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class ReloadProjectEvent extends EventObject
{
    public ReloadProjectEvent(Object source) {
        super(source);
    }
}
