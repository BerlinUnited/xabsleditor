package de.naoth.xabsleditor.events;

import java.io.File;
import java.util.EventObject;

/**
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class OpenFileEvent extends EventObject
{
    public final File file;
    public final int carretPosition;
    
    public OpenFileEvent(Object source, File f) {
        super(source);
        file = f;
        carretPosition = 0;
    }
    
    public OpenFileEvent(Object source, File f, int c) {
        super(source);
        file = f;
        carretPosition = c;
    }
}
