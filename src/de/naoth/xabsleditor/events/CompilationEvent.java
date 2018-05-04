package de.naoth.xabsleditor.events;

import java.io.File;
import java.util.EventObject;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class CompilationEvent extends EventObject
{
    public final File file;
    public CompilationEvent(Object source, File f) {
        super(source);
        file = f;
    }
    
}
