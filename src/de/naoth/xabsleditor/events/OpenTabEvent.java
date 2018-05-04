package de.naoth.xabsleditor.events;

import de.naoth.xabsleditor.utils.Project;
import java.io.File;
import java.util.EventObject;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class OpenTabEvent extends EventObject
{
    public final File file;
    public final Project project;
    public final int carretPosition;
    
    public OpenTabEvent(Object source, File f, Project p) {
        this(source, f, p, 0);
    }
    
    public OpenTabEvent(Object source, File f, Project p, int c) {
        super(source);
        this.file = f;
        this.project = p;
        carretPosition = c;
    }
}
