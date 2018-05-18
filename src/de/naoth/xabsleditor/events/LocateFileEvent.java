package de.naoth.xabsleditor.events;

import java.io.File;
import java.util.EventObject;

/**
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class LocateFileEvent extends EventObject
{
    public final File file;
    
    public LocateFileEvent(Object source, File file) {
        super(source);
        this.file = file;
    }
    
    public LocateFileEvent(Object source, String file) {
        this(source, new File(file));
    }
}
