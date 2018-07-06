package de.naoth.xabsleditor.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class LinkListener implements HyperlinkListener
{
    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            // handle anchor links
            if (e.getDescription() != null && e.getDescription().startsWith("#")) {
                if (e.getInputEvent().getSource() instanceof JEditorPane) {
                    JEditorPane p = (JEditorPane) e.getInputEvent().getSource();
                    p.scrollToReference(e.getDescription().substring(1));
                }
            } else if (e.getURL() != null) {
                // handle extern links
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (URISyntaxException | IOException ex) {
                        Logger.getLogger(LinkListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    Logger.getLogger(LinkListener.class.getName()).log(Level.SEVERE, null, "Can not open URL (" + e.getURL() + "); Desktop not supported.");
                }
            }
        }
    }
}
