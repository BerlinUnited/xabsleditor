package de.naoth.xabsleditor.completion;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.TemplateCompletion;

/**
 * This class is intended for shorthand completions of the xabsl language for
 * commonly used statements.
 * 
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslTemplateCompletion extends TemplateCompletion
{
    /**
     * Constructor. Defines the style and attributes of a xabsl shorthand completion.
     * 
     * @param provider the parent completion provider
     * @param inputText the shorthand string, which gets replaced by the template
     * @param definitionString the string, which should be shown as entry in the completion window
     * @param template the template string to which is the replacement
     * @param shortDescription A short description of the completion.  This will 
     *                         be displayed in the completion list.  This may be <code>null</code>.
     * @param summary The summary of this completion. This should be HTML or 
     *                <code>null</code>.
     */
    public XabslTemplateCompletion(CompletionProvider provider, String inputText, String definitionString, String template, String shortDescription, String summary) {
        super(provider, inputText, definitionString, template, shortDescription, summary);
        setRelevance(20);
    }
}
