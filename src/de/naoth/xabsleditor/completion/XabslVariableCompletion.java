package de.naoth.xabsleditor.completion;

import de.naoth.xabsleditor.parser.XABSLContext;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslVariableCompletion extends BasicCompletion
{
    public XabslVariableCompletion(CompletionProvider provider, XABSLContext.XABSLBasicSymbol v) {
        super(provider, "@"+v.getName());
        setShortDescription("Option parameter");
        setSummary("<b>@" + v.getName() + "</b> - option parameter<hr>" + v.getComment());
        setRelevance(50);
    }

    @Override
    public String getToolTipText() {
        return getShortDescription();
    }
}
