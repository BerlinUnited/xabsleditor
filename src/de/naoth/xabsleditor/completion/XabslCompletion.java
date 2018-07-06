package de.naoth.xabsleditor.completion;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 * This class represents a completion for the xabsl language itself.
 * 
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslCompletion extends BasicCompletion
{
    /**
     * Constructor. Defines the style and attributes of a xabsl keyword completion.
     * 
     * @param provider the parent completion provider
     * @param replacementText The text to replace.
     * @param shortDesc A short description of the completion.  This will be 
     *                  displayed in the completion list.  This may be <code>null</code>.
     * @param summary The summary of this completion. This should be HTML or <code>null</code>.
     */
    public XabslCompletion(CompletionProvider provider, String replacementText, String shortDesc, String summary) {
        super(provider, replacementText, shortDesc, summary);
        setRelevance(10);
    }

    /**
     * Returns the tool tip text to display for mouse hovers over this completion.
     * 
     * @return The tool tip text for this completion, or <code>null</code> if none.
     */
    @Override
    public String getToolTipText() {
        return getShortDescription();
    }
}
