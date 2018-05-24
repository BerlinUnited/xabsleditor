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
import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;

/**
 *
 * @author Heinrich Mellmann
 */
public class XABSLSymbolCompletion extends FunctionCompletion
{
    protected XABSLContext.XABSLSymbol symbol;
    
    public XABSLSymbolCompletion(CompletionProvider provider, XABSLContext.XABSLSymbol symbol) {
        super(provider, symbol.getName(), symbol.getType());
        this.symbol = symbol;
        // TODO: set correct parameter definition
        setParams(symbol.getParameter().stream().map((t) -> {
            Parameter p = new Parameter(t.getType(), t.getName());
            p.setDescription(t.getComment());
            return p;
        }).collect(Collectors.toList()));
        setRelevance(40);
    }
    
    @Override
    protected int[] getParamText(Parameter param, StringBuilder sb) {
        sb.append(param.getName());
        sb.append("=");
        sb.append(param.getType());
        int length = param.getName().length() + 1 + param.getType().length();
        return new int[] {length, param.getName().length() + 1, length};
    }
}//end class XABSLSymbolCompletion
