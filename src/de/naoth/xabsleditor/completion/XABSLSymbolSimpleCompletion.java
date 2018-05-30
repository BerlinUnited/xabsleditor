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
package de.naoth.xabsleditor.completion;

import de.naoth.xabsleditor.parser.XABSLContext;
import de.naoth.xabsleditor.parser.XABSLContext.XABSLSymbol.SecondaryType;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 * Represents the completion information for a xabsl symbol with parameters.
 * 
 * @author Heinrich Mellmann
 */
public class XABSLSymbolSimpleCompletion extends BasicCompletion
{
    /** The xabsl symbol represented by this completion. */
    protected XABSLContext.XABSLSymbol symbol;

    /**
     * Constructor. Defines the style and attributes of a xabsl symbol completion.
     * 
     * @param provider the parent completion provider
     * @param symbol the xabsl symbol to show completion for
     */
    public XABSLSymbolSimpleCompletion(CompletionProvider provider, XABSLContext.XABSLSymbol symbol) {
        super(provider, symbol.getName());
        this.symbol = symbol;

        this.setShortDescription(symbol.toString());
        this.setSummary(createSummary());
        setRelevance(40);
    }

    /**
     * Returns the (styled) summary of this state completion.
     * 
     * @return html string summary
     */
    protected String createSummary() {
        StringBuilder sb = new StringBuilder();

//    if(symbol.getDeclarationSource() != null)
//    {
//      sb.append(symbol.getDeclarationSource().fileName);
//    }
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

    /**
     * If this symbol is of an enum type, the possible values are added to the completion info.
     * 
     * @param sb the StringBuilder, where the infos is appended too
     */
    protected void possiblyAddEnumDefinition(StringBuilder sb) {
        // list the enum elements
        if (symbol.getType().equals("enum")) {
            sb.append("<br><br><font color=\"#0000FF\">enum</font> ");
            sb.append("<b>").append(symbol.getEnumDeclaration().name).append("</b>");

            sb.append("<ul>");
            for (String element : symbol.getEnumDeclaration().getElements()) {
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

    /**
     * Returns the secondary type of this symbol.
     * 
     * @return the secondary type
     */
    public SecondaryType getSecondaryType() {
        return symbol.getSecondaryType();
    }

    /**
     * Returns the tool tip text to display for mouse hovers over this completion.
     * 
     * @return The tool tip text for this completion, or <code>null</code> if none.
     */
    @Override
    public String getToolTipText() {
        return getShortDescription();
    }
}//end class XABSLSymbolSimpleCompletion
