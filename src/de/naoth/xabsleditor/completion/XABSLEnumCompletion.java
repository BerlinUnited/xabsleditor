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

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 * Represents the completion information for a xabsl enum.
 * 
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XABSLEnumCompletion extends BasicCompletion
{
    private String enumName = "";
    private String enumElement = "";

    /**
     * Constructor. Defines the style and attributes of a xabsl enum completion.
     * 
     * @param provider the parent completion provider
     * @param enumName the name of the enum
     * @param enumElement the value/element of the enum
     */
    public XABSLEnumCompletion(CompletionProvider provider, String enumName, String enumElement) {
        super(provider, enumElement);

        this.enumElement = enumElement;
        this.enumName = enumName;

        this.setShortDescription("element of enum " + enumName);
        this.setSummary(createSummary());
        setRelevance(40);
    }

    /**
     * Returns the (styled) summary of this enum completion.
     * 
     * @return html string summary
     */
    protected String createSummary() {
        StringBuilder sb = new StringBuilder();

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
        return this.enumName + "." + this.enumElement;
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
