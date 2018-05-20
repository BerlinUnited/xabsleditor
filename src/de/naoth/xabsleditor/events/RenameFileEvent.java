package de.naoth.xabsleditor.events;

import java.io.File;
import java.util.EventObject;

/**
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class RenameFileEvent extends EventObject
{
    public final File file;
    public RenameFileEvent(Object source, File f) {
        super(source);
        file = f;
    }
}
