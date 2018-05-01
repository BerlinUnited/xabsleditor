package de.naoth.xabsleditor.events;

import java.util.EventObject;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class UpdateProjectEvent extends EventObject
{    
    public UpdateProjectEvent(Object source) {
        super(source);
    }
}
