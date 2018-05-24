package de.naoth.xabsleditor.completion;

import org.fife.ui.autocomplete.DefaultCompletionProvider;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslDefaultCompletionProvider extends DefaultCompletionProvider
{

    public XabslDefaultCompletionProvider() {
        setParameterizedCompletionParams('(', ", ", ')');
    }
    
    @Override
    protected boolean isValidChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '.' || ch == '_';
    }
}
