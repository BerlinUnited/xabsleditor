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

import de.naoth.xabsleditor.parser.XABSLOptionContext;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 *
 * @author Heinrich Mellmann
 */
public class XABSLStateCompletion extends BasicCompletion
{
    XABSLOptionContext.State state;
    
    public XABSLStateCompletion(CompletionProvider provider, XABSLOptionContext.State state) {
        super(provider, state.name);

        this.state = state;
        this.setShortDescription("State " + state.name);
        this.setSummary(createSummary());
        setRelevance(30);
    }

    protected String createSummary() {
        StringBuilder sb = new StringBuilder();
        if(state.initial) {
            sb.append("<font color=\"#00FF00\">initial</font> ");
        }
        if(state.target) {
            sb.append("<font color=\"#FF0000\">target</font> ");
        }
        sb.append("<b>")
          .append(state.name)
          .append("</b> - state<hr>");
        if(!state.comment.isEmpty()) {
            
            sb.append(state.comment).append("<br><br>");
        }
        if(state.outgoingOptions.size() > 0) {
            sb.append("Outgoing options:");
            sb.append("<ul>");
            state.outgoingOptions.stream().forEachOrdered((out) -> {
                sb.append("<li>");
                sb.append(out);
                sb.append("</li>");
            });
            sb.append("</ul>");
        }
        return sb.toString();
    }//end createSummary

    /**
     * Returns the name of this variable.
     *
     * @return The name.
     */
    public String getName() {
        return state.name;
    }//end getName

    /**
     * Returns the text the user must start typing to get this completion.
     *
     * @return The text the user must start to input.
     */
    @Override
    public String getInputText() {
        return state.name;
    }
}//end class XABSLStateCompetion
