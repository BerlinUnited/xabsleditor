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
import java.util.stream.Collectors;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;

/**
 * Represents the completion information for a xabsl option.
 * 
 * @author Heinrich Mellmann
 */
public class XABSLOptionCompletion extends FunctionCompletion
{
    /** The xabsl option represented by this completion. */
    protected XABSLContext.XABSLOption option;

    /**
     * Constructor. Defines the style and attributes of a xabsl option completion.
     * 
     * @param provider the parent completion provider
     * @param option the xabsl option to show completion for
     */
    public XABSLOptionCompletion(CompletionProvider provider, XABSLContext.XABSLOption option) {
        super(provider, option.getName(), "void");
        setShortDescription(option.getComment());
        this.option = option;
        
        //add the options parameter to the completion info
        setParams(option.getParameter().stream().map((t) -> {
            Parameter p = new Parameter(t.getType(), t.getName());
            p.setDescription(t.getComment());
            return p;
        }).collect(Collectors.toList()));
        
        setRelevance(30);
    }
    
    /**
     * Adds the parameter definition of the xabsl option to the info string 
     * (represented by the StringBuilder) and returns the offsets for the
     * replacement.
     * This method was patched for the XabslEditor in the Autocomplete library!
     * 
     * @param param the paramter, which infos should be added to the completion
     * @param sb the StringBuilder, where the infos should be appended to
     * @return the offset start/end where the user can replace the parameter value
     */
    @Override
    protected int[] getParamText(Parameter param, StringBuilder sb) {
        super.getParamText(param, sb);
        sb.append(param.getName());
        sb.append("=");
        sb.append(param.getType());
        int length = param.getName().length() + 1 + param.getType().length();
        return new int[] {length, param.getName().length() + 1, length};
    }
}//end class XABSLOptionCompletition
