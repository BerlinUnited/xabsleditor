/*
 * 
 */

package de.hu_berlin.informatik.ki.jxabsleditor.editorpanel;

import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.ParameterizedCompletion;

/**
 *
 * @author Heinrich Mellmann
 */
public class XABSLOptionCompletion
        extends BasicCompletion
        implements ParameterizedCompletion
{
  protected XABSLContext.XABSLOption option;

  public XABSLOptionCompletion(CompletionProvider provider, XABSLContext.XABSLOption option)
  {
    super(provider, option.getName());
    this.option = option;

    //this.setShortDescription(option.toString());
    this.setSummary(createSummary());
  }


  protected String createSummary() {
    StringBuffer sb = new StringBuffer();

    sb.append(option.getName())
      .append("</b>")
      .append("<hr>");

    sb.append(option.getComment());

    return sb.toString();
  }//end createSummary

  @Override
  public String getDefinitionString() {
    return "XABSLOptionCompletition.DefinitionString";
  }//end getDefinitionString

  @Override
  public Parameter getParam(int idx) {
    XABSLContext.XABSLBasicSymbol parameter = this.option.getParameter().get(idx);
    return new Parameter(parameter.getType(), parameter.getName());
  }//end getParam

  @Override
  public int getParamCount() {
    return this.option.getParameter().size();
  }//end getParamCount

  /**
	 * Returns the name of this variable.
	 *
	 * @return The name.
	 */
	public String getName() {
		return getReplacementText();
	}//end getName
  
}//end class XABSLOptionCompletition
