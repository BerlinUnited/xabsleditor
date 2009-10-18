/*
 * 
 */

package de.hu_berlin.informatik.ki.jxabsleditor.editorpanel;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 *
 * @author admin
 */
public class XABSLEnumCompletion extends BasicCompletion
{
  private String enumName = "";
  private String enumElement = "";

  public XABSLEnumCompletion(CompletionProvider provider, String enumName, String enumElement)
  {
    super(provider, enumElement);

    this.enumElement = enumElement;
    this.enumName = enumName;
    
    this.setShortDescription("element of enum " + enumName);
    this.setSummary(createSummary());
  }


  protected String createSummary() {
    StringBuffer sb = new StringBuffer();

    sb.append("<font color=\"#0000FF\">")
      .append("enum ")
      .append("</font><b> ")
      .append(this.enumName)
      .append(".")
      .append(this.enumElement)
      .append("</b>")
      .append("<hr>");

    sb.append("Element of enum type <i>" + enumName + "</i>");
    
    return sb.toString();
  }//end createSummary

  /**
	 * Returns the name of this variable.
	 *
	 * @return The name.
	 */
	public String getName() {
		return this.enumElement;
	}//end getName

  /**
	 * Returns the text the user must start typing to get this completion.
	 *
	 * @return The text the user must start to input.
	 */
  @Override
	public String getInputText() {
		return this.enumName+"."+this.enumElement;
	}


  /**
	 * Returns the type of this variable.
	 *
	 * @return The type.
	 */
	public String getType() {
		return this.enumName;
	}
}//end class XABSLEnumCompletion
