package de.naoth.xabsleditor.completion;

import java.awt.Point;
import java.util.ArrayList;
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

    @Override
    public List<Completion> getCompletionsAt(JTextComponent tc, Point pt) {
        final List<Completion> l = super.getCompletionsAt(tc, pt) == null ? new ArrayList<>() : super.getCompletionsAt(tc, pt);
        children.values().forEach((p) -> {
            List<Completion> u = p.getCompletionsAt(tc, pt);
            if(u != null) {
                l.addAll(u);
            }
        });
        return l;
    }
}
