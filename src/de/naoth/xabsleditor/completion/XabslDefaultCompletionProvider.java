package de.naoth.xabsleditor.completion;

import java.util.HashMap;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslDefaultCompletionProvider extends DefaultCompletionProvider
{
    protected HashMap<String, CompletionProvider> children = new HashMap<>();

    public XabslDefaultCompletionProvider() {
        setParameterizedCompletionParams('(', ", ", ')');
    }
    
    @Override
    protected boolean isValidChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '.' || ch == '_';
    }

    @Override
    protected List<Completion> getCompletionsImpl(JTextComponent comp) {
        List<Completion> l = super.getCompletionsImpl(comp);
        children.values().forEach((p) -> {
            l.addAll(p.getCompletions(comp));
        });
        return l;
    }
    
    public void addChildCompletionProvider(String id, CompletionProvider p) {
        children.put(id, p);
    }
}
