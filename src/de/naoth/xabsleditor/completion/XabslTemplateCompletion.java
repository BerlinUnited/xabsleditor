package de.naoth.xabsleditor.completion;

import javax.swing.ImageIcon;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.TemplateCompletion;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslTemplateCompletion extends TemplateCompletion
{
//    private static final ImageIcon ICON = new ImageIcon(XabslCompletionProvider.class.getResource("/de/naoth/xabsleditor/res/test.png"));
    
    public XabslTemplateCompletion(CompletionProvider provider, String inputText, String definitionString, String template, String shortDescription, String summary) {
        super(provider, inputText, definitionString, template, shortDescription, summary);
        setRelevance(20);
//        setIcon(ICON);
    }
}
