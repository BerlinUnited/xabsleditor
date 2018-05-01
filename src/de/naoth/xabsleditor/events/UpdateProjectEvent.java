package de.naoth.xabsleditor.events;

import java.util.ArrayList;
import java.util.EventObject;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class UpdateProjectEvent extends EventObject
{
    public final ArrayList<DefaultMutableTreeNode> projects;
    
    public UpdateProjectEvent(Object source, ArrayList<DefaultMutableTreeNode> projects) {
        super(source);
        this.projects = projects;
    }
}
