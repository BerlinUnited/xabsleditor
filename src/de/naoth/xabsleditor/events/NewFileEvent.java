package de.naoth.xabsleditor.events;

import java.io.File;
import java.util.EventObject;

/**
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class NewFileEvent extends EventObject
{
    public final File startDirectory;
    
    public NewFileEvent(Object source) {
        this(source, new File(""));
    }
    
    public NewFileEvent(Object source, String startDirectory) {
        this(source, new File(startDirectory));
    }
    
    public NewFileEvent(Object source, File startDirectory) {
        super(source);
        this.startDirectory = startDirectory;
    }
}
