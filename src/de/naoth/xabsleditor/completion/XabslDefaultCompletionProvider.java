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
 * A extended version of the DefaultCompletionProvider.
 * It adds the ability to set multiple child providers, which were addtionally
 * used to retrieve completion informations.
 * 
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslDefaultCompletionProvider extends DefaultCompletionProvider
{
    /** The child provider of this (parent) completion provider. */
    protected HashMap<String, CompletionProvider> children = new HashMap<>();

    /**
     * Constructor. Sets the values used to identify and insert "parameterized completions" (e.g. functions or methods).
     */
    public XabslDefaultCompletionProvider() {
        setParameterizedCompletionParams('(', ", ", ')');
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isValidChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '.' || ch == '_';
    }

    /**
     * Does the dirty work of creating a list of completions.
     * Also adds the completions from all child provider.
     *
     * @param comp The text component to look in.
     * @return The list of possible completions, or an empty list if there are none.
     */
    @Override
    protected List<Completion> getCompletionsImpl(JTextComponent comp) {
        List<Completion> l = super.getCompletionsImpl(comp);
        // add completions of the children
        children.values().forEach((p) -> {
            l.addAll(p.getCompletions(comp));
        });
        return l;
    }
    
    /**
     * Adds a child provider to this completion provider.
     * 
     * @param id an identifier in order to be able to replace this provider
     * @param p the provider to be added
     */
    public void addChildCompletionProvider(String id, CompletionProvider p) {
        children.put(id, p);
    }

    /**
     * Returns the completions that have been entered at the specified visual
     * location. This can be used for tool tips when the user hovers the mouse
     * over completed text. The compmletions of the child provider are also added
     * to the returned list.
     *
     * @param tc The text component.
     * @param pt The position, usually from a <tt>MouseEvent</tt>.
     * @return The completions, or an empty list if there are none.
     */
    @Override
    public List<Completion> getCompletionsAt(JTextComponent tc, Point pt) {
        final List<Completion> l = super.getCompletionsAt(tc, pt) == null ? new ArrayList<>() : super.getCompletionsAt(tc, pt);
        // add completions of the children
        children.values().forEach((p) -> {
            List<Completion> u = p.getCompletionsAt(tc, pt);
            if(u != null) {
                l.addAll(u);
            }
        });
        return l;
    }
}
