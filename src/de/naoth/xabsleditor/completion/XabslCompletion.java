package de.naoth.xabsleditor.completion;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 * This class represents a completion for the xabsl language itself.
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslCompletion extends BasicCompletion
{
    public XabslCompletion(CompletionProvider provider, String replacementText, String shortDesc, String summary) {
        super(provider, replacementText, shortDesc, summary);
        setRelevance(10);
    }

    @Override
    public String getToolTipText() {
        return getShortDescription();
    }
}
