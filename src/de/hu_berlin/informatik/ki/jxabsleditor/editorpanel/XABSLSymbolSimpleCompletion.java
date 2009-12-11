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
package de.hu_berlin.informatik.ki.jxabsleditor.editorpanel;

import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext.XABSLSymbol.SecondaryType;
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

  public SecondaryType getSecondaryType() {
    return symbol.getSecondaryType();
  }
  
}//end class XABSLSymbolSimpleCompletion
