package de.naoth.xabsleditor.events;

import de.naoth.xabsleditor.editorpanel.XEditorPanel;
import java.util.EventObject;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class RefreshGraphEvent extends EventObject
{
    public RefreshGraphEvent(XEditorPanel source) {
        super(source);
    }
}
