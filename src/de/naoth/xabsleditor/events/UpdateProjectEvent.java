package de.naoth.xabsleditor.events;

import de.naoth.xabsleditor.utils.Project;
import java.util.EventObject;
import java.util.Map;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class UpdateProjectEvent extends EventObject
{
    public final Map<String, Project> projects;
    
    public UpdateProjectEvent(Object source, Map<String, Project> projects) {
        super(source);
        this.projects = projects;
    }
}
