/*
 * 
 */

package de.hu_berlin.informatik.ki.jxabsleditor.editorpanel;

import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;


/**
 *
 * @author Heinrich Mellmann
 */
public class XABSLSymbolSimpleCompletion extends BasicCompletion
{
  protected XABSLContext.XABSLSymbol symbol;

  public XABSLSymbolSimpleCompletion(CompletionProvider provider, XABSLContext.XABSLSymbol symbol)
  {
    super(provider, symbol.getName());
    this.symbol = symbol;

    this.setShortDescription(symbol.toString());
    this.setSummary(createSummary());
  }

  
  protected String createSummary() {
    StringBuffer sb = new StringBuffer();
    
    sb.append("<font color=\"#0000FF\">")
      .append(symbol.getType())
      .append(" ")
      .append(symbol.getSecondaryType().name())
      .append("</font><b> ")
      .append(symbol.getName())
      .append("</b>")
      .append("<hr>");

    sb.append(symbol.getComment());
    possiblyAddEnumDefinition(sb);

    return sb.toString();
  }//end createSummary
  

  protected void possiblyAddEnumDefinition(StringBuffer sb) {
		// list the enum elements
    if(symbol.getType().equals("enum"))
    {
      sb.append("<br><br><font color=\"#0000FF\">enum</font> ");
      sb.append("<b>").append(symbol.getEnumDeclaration().name).append("</b>");

      sb.append("<ul>");
      for(String element: symbol.getEnumDeclaration().getElements())
      {
        sb.append("<li><i>").append(element).append("</i></li>");
      }
      sb.append("</ul>");
    }//end if
	}//end possiblyAddEnumDefinition


  /**
	 * Returns the name of this variable.
	 *
	 * @return The name.
	 */
	public String getName() {
		return getReplacementText();
	}//end getName


  /**
	 * Returns the type of this variable.
	 *
	 * @return The type.
	 */
	public String getType() {
		return symbol.getType();
	}
}//end class XABSLSymbolSimpleCompletion
