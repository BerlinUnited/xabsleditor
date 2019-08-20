package de.naoth.xabsleditor.editorpanel;

import de.naoth.xabsleditor.parser.XParser;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

/**
 * The XABSL editor with some customizations of the underlying TextEditorPane.
 * 
 * TextEditorPane: 
 *   "An extension of RSyntaxTextArea that adds information 
 *    about the file being edited, such as: Its name and location."
 *    http://javadoc.fifesoft.com/rsyntaxtextarea/
 * 
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XSyntaxTextArea extends TextEditorPane
{
    /**
     * underline only when the hyperlink is activated
     */
    @Override
    public boolean getUnderlineForToken(Token t) {
        // HACK: using the color of token to identify if
        //       it is activated, since hoveredOverLinkOffset
        //       is private in RSyntaxTextArea
        if (t.isHyperlink()) {
            return (getHyperlinksEnabled()
                    && getForegroundForToken(t) == getHyperlinkForeground());
        }

        return super.getUnderlineForToken(t);
    } //END getUnderlineForToken()
    
    /**
     * Clears all highlighted text parts from a (previous) highlighted search.
     */
    public void clearAllHighlights() {
        // HACK: "clearMarkAllHighlights()" is private, so we have to trigger a
        //       null-search inorder to call it.
        SearchContext sc = new SearchContext(null);
        sc.setMarkAll(false);
        SearchEngine.find(this, sc).wasFound();
    }
    
    /**
     * Returns the Xabsl Parser of this text area.
     * 
     * @return the XParser if set, otherwise null
     */
    public XParser getXParser() {
        for (int i = 0; i < getParserCount(); i++) {
            if(getParser(i) instanceof XParser) {
                return (XParser) getParser(i);
            }
        }
        return null;
    }
}
