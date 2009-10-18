/*
 * 
 */

package de.hu_berlin.informatik.ki.jxabsleditor.editorpanel;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 *
 * @author Heinrich Mellmann
 */
public class XABSLStateCompetion extends BasicCompletion
{
  String stateName;
  
  public XABSLStateCompetion(CompletionProvider provider, String stateName)
  {
    super(provider, stateName);
    
    this.stateName = stateName;
    this.setShortDescription("State " + stateName);
    this.setSummary(createSummary());
  }

  protected String createSummary() {
    StringBuffer sb = new StringBuffer();

    sb.append("<b> ")
      .append(this.stateName)
      .append("</b>")
      .append("<hr>");

    sb.append("An option state.");

    return sb.toString();
  }//end createSummary
  
  /**
	 * Returns the name of this variable.
	 *
	 * @return The name.
	 */
	public String getName() {
		return this.stateName;
	}//end getName

  /**
	 * Returns the text the user must start typing to get this completion.
	 *
	 * @return The text the user must start to input.
	 */
  @Override
	public String getInputText() {
		return this.stateName;
	}
}//end class XABSLStateCompetion
