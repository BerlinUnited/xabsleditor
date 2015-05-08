/*
 * Copyright 2009 NaoTeam Humboldt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.naoth.xabsleditor.editorpanel;

import de.naoth.xabsleditor.parser.XABSLContext;
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
