package de.naoth.xabsleditor.completion;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslVariableCompletion extends BasicCompletion
{
    public XabslVariableCompletion(CompletionProvider provider, String replacementText, String shortDesc, String summary) {
        super(provider, replacementText, shortDesc, summary);
        setRelevance(50);
    }
    
    @Override
    public String getToolTipText() {
        return getShortDescription();
    }
}
