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
