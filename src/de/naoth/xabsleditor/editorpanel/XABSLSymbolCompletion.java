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
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.ParameterizedCompletion;

/**
 *
 * @author Heinrich Mellmann
 */
public class XABSLSymbolCompletion
        extends XABSLSymbolSimpleCompletion
        implements ParameterizedCompletion
{

  public XABSLSymbolCompletion(CompletionProvider provider, XABSLContext.XABSLSymbol symbol)
  {
    super(provider, symbol);
  }

  @Override
  public String getDefinitionString() {
    /*
     helpBody += "<br><u>Parameter</u>:<table>";
        helpHeader += "(";
        replacementText += "(";
        String separator = "";
        for(XParser.XABSLBasicSymbol parameter: symbol.getParameter())
        {
          String type = "<font color=\"#0000FF\">" + parameter.getType() + "</font>";
          if(parameter.getType().equals("enum") && parameter.getEnumDeclaration() != null)
          {
            type += " " + parameter.getEnumDeclaration().name;
          }

          replacementText += separator + parameter.getName() + "=";
          helpHeader += separator + type + " " + parameter.getName();
          separator = ", ";

          helpBody += "<tr><td><font color=\"#0000FF\">" + parameter.getType()
                    + "</font> " + parameter.getName();

          helpBody += " - " + ((parameter.getUnit()==null)?"":parameter.getUnit())
                    + ((parameter.getRange()==null)?"":(" [" + parameter.getRange()+ "]"))
                    + " " + parameter.getComment()
                    + "</td></tr>";
        }//end for
        replacementText += ")";
        helpHeader += ")";

        inputText += "(..)";
        helpBody += "</table>";
      
        helpHeader + "<hr>" + helpBody
     */
    return "test";
  }//end getDefinitionString

  @Override
  public Parameter getParam(int idx) {
    XABSLContext.XABSLBasicSymbol parameter = this.symbol.getParameter().get(idx);
    return new Parameter(parameter.getType(), parameter.getName());
  }//end getParam

  @Override
  public int getParamCount() {
    return this.symbol.getParameter().size();
  }//end getParamCount
  
}//end class XABSLSymbolCompletion
