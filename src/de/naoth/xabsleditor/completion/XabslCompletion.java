package de.naoth.xabsleditor.completion;

import javax.swing.ImageIcon;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 * This class represents a completion for the xabsl language itself.
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslCompletion extends BasicCompletion
{
    private static final ImageIcon ICON = new ImageIcon(XabslCompletionProvider.class.getResource("/de/naoth/xabsleditor/res/test.png"));
    
    public XabslCompletion(CompletionProvider provider, String replacementText, String shortDesc, String summary) {
        super(provider, replacementText, shortDesc, summary);
        setRelevance(10);
        setIcon(ICON);
    }
}
