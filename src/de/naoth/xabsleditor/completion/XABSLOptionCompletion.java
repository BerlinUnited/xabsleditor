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
 *
 * @author Heinrich Mellmann
 */
public class XABSLOptionCompletion extends FunctionCompletion
{
    protected XABSLContext.XABSLOption option;

    public XABSLOptionCompletion(CompletionProvider provider, XABSLContext.XABSLOption option) {
        super(provider, option.getName(), "void");
        setShortDescription(option.getComment());
        //super(provider, option.getName(), "XABSLOptionCompletition.DefinitionString", "", "", option.getName() + "</b><hr>" + option.getComment());
        this.option = option;
        
        // TODO: set correct parameter definition
        setParams(option.getParameter().stream().map((t) -> {
            Parameter p = new Parameter(t.getType(), t.getName());
            p.setDescription(t.getComment());
            return p;
        }).collect(Collectors.toList()));
    }
    
    @Override
    protected int[] getParamText(Parameter param, StringBuilder sb) {
        sb.append(param.getName());
        sb.append("=");
        sb.append(param.getType());
        int length = param.getName().length() + 1 + param.getType().length();
        return new int[] {length, param.getName().length() + 1, length};
    }
}//end class XABSLOptionCompletition
