package de.naoth.xabsleditor.completion;

import de.naoth.xabsleditor.parser.XABSLContext;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 * Represents the completion information for a xabsl option variable.
 * 
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslVariableCompletion extends BasicCompletion
{
    /**
     * Constructor. Defines the style and attributes of a xabsl option variable completion.
     * 
     * @param provider the parent completion provider
     * @param v the xabsl variable/symbol to show completion for
     */
    public XabslVariableCompletion(CompletionProvider provider, XABSLContext.XABSLBasicSymbol v) {
        super(provider, "@"+v.getName());
        setShortDescription("Option parameter");
        setSummary("<b>@" + v.getName() + "</b> - option parameter<hr>" + v.getComment());
        setRelevance(50);
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
