package de.naoth.xabsleditor.completion;

import de.naoth.xabsleditor.editorpanel.XSyntaxTextArea;
import de.naoth.xabsleditor.parser.XABSLOptionParser;
import de.naoth.xabsleditor.parser.XParser;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextArea;

/**
 * The completion provider for the XABSL language.
 * 
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslCompletionProvider extends LanguageAwareCompletionProvider
{
    private final CompletionProvider emptyDocumentProvider = new EmptyCompletionProvider();
    private final CompletionProvider symbolProvider = new SymbolCompletionProvider();
    private final CompletionProvider optionProvider = new OptionCompletionProvider();
    private final CompletionProvider stateProvider = new StateCompletionProvider();
    private final CompletionProvider decisionProvider = new DecisionCompletionProvider();
    private final CompletionProvider actionProvider = new XabslDefaultCompletionProvider();
    private final CompletionProvider statesProvider = new XabslDefaultCompletionProvider();

    /**
     * Constructor. Sets the default completion provider.
     */
    public XabslCompletionProvider() {
        super(new XabslDefaultCompletionProvider());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CompletionProvider getProviderFor(JTextComponent comp) {
        XSyntaxTextArea rsta = (XSyntaxTextArea)comp;
        int line = rsta.getCaretLineNumber();
        CompletionProvider provider = null;
        for (int i = line; i >= 0; i--) {
            Token l = rsta.getTokenListForLine(i);
            while(l != null) {
                if(l.getType() != Token.NULL) {
                    // based on the previous (known) token select a completion provider
                    switch(l.getLexeme()) {
                        case "goto":      provider = updateLocalStates(rsta.getXParser(), (XabslDefaultCompletionProvider) statesProvider);      break;
                        case "if":
                        case "else":
                        case "decision":  provider = updateLocalParameter(rsta.getXParser(), (XabslDefaultCompletionProvider) decisionProvider); break;
                        case "action":    provider = updateLocalParameter(rsta.getXParser(), (XabslDefaultCompletionProvider) actionProvider);   break;
                        case "state":     provider = stateProvider;    break;
                        case "option":    provider = optionProvider;   break;
                        case "namespace": provider = symbolProvider;   break;
                    }
                }
                l = l.getNextToken();
            }
            // stop 'search'
            if(provider != null) { break; }
        }

        if(provider == null) {
            return emptyDocumentProvider;
        }
        
        return provider;
    } // END getProviderFor()
    
    /**
     * Updates the given completion provider with the parameter/variables of the current option.
     * To determine the parameter/variable, the XParser is used.
     * 
     * @param parser the parser of the current document/option
     * @param provider the provider which should be extended
     * @return the extended provider
     */
    public CompletionProvider updateLocalParameter(XParser parser, XabslDefaultCompletionProvider provider) {
        if(parser.getFileParser() instanceof XABSLOptionParser) {
            // add the variables of the current option
            DefaultCompletionProvider variables = new DefaultCompletionProvider();
            ((XABSLOptionParser)parser.getFileParser()).getOption().getParameter().forEach((t) -> {
                variables.addCompletion(new XabslVariableCompletion(variables, t));
            });
            provider.addChildCompletionProvider("variables", variables);
            return provider;
        }
        return provider;
    } // END updateLocalParameter()
    
    /**
     * Updates the given completion provider with the states of the current option.
     * To determine the states, the XParser is used.
     * 
     * @param parser the parser of the current document/option
     * @param provider the provider which should be extended
     * @return the extended provider
     */
    public CompletionProvider updateLocalStates(XParser parser, XabslDefaultCompletionProvider provider) {
        // add all states of the current option
        DefaultCompletionProvider states = new DefaultCompletionProvider();
        ((XABSLOptionParser)parser.getFileParser()).getStates().values().forEach((s)->{
            states.addCompletion(new XABSLStateCompletion(states, s));
        });
        provider.addChildCompletionProvider("states", states);
        return provider;
    }
    
    /**
     * Updates the action and decision provider with the (global/project) symbols (contained in the given provider).
     * 
     * @param provider completion provider with the global/project symbols
     */
    public void updateSymbols(CompletionProvider provider) {
        ((XabslDefaultCompletionProvider)actionProvider).addChildCompletionProvider("symbols", provider);
        ((XabslDefaultCompletionProvider)decisionProvider).addChildCompletionProvider("symbols", provider);
    }
    
    /**
     * Updates the action provider with the (global/project) options (contained in the given provider).
     * 
     * @param provider completion provider with the global/project options
     */
    public void updateOptions(CompletionProvider provider) {
        ((XabslDefaultCompletionProvider)actionProvider).addChildCompletionProvider("options", provider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getToolTipText(RTextArea textArea, MouseEvent e) {
        String tip = null;
        // just use the completion of the action provider; others have no "usefull" information for a tooltip
        List<Completion> completions = actionProvider.getCompletionsAt(textArea, e.getPoint());
        if (completions!=null && completions.size()>0) {
            for (Completion completion : completions) {
                // don't show tooltip for shorthand completions
                if(!(completion instanceof XabslTemplateCompletion || completion.getToolTipText() == null)) {
                    tip = completion.getToolTipText();
                    break;
                }
            }
        }
        
        return tip;
    } // END getToolTipText()
    
    /**
     * Completion provider containing the completions for an empty xabsl file.
     */
    class EmptyCompletionProvider extends DefaultCompletionProvider
    {
        public EmptyCompletionProvider() {
            addCompletions(Arrays.asList(
                //                        replacement, shortDesc,    summary               
                new XabslCompletion(this, "include", "include", "include statement"),
                new XabslCompletion(this, "namespace", "namespace", "namespace statement"),
                new XabslCompletion(this, "option", "option", "option statement"),
                new XabslCompletion(this, "agent", "agent", "agent statement"),
                //                          input, definition, template, shortDescription, summary
                new XabslTemplateCompletion(this, "include", 
                                                  "include \"\";", 
                                                  "include \"${cursor}\";", 
                                                  "include", 
                                                  "<pre>include \"|\";</pre>"),
                new XabslTemplateCompletion(this, "namespace", 
                                                  "namespace(\"\"){}", 
                                                  "namespace ${id}(\"${name}\")\n{\n\t${cursor}\n}",
                                                  "namespace", 
                                                  "<pre>namespace <b>id</b>(\"<b>name</b>\")\n{\n    |\n}</pre>"),
                new XabslTemplateCompletion(this, "option", 
                                                  "option {}", 
                                                  "option ${name}\n{\n${cursor}\n}", 
                                                  "option", 
                                                  "<pre>option <b>name</b>\n{\n    |\n}</pre>"),
                new XabslTemplateCompletion(this, "agent", 
                                                  "agent (\"\");", 
                                                  "agent ${id}(\"${agent-title}\", ${root-option});", 
                                                  "agent", 
                                                  "<pre>agent <b>id</b>(\"<b>agent-title</b>\", <b>root-option</b>);</pre>")
            ));
        }
    } // END EmptyCompletionProvider
    
    /**
     * Completion provider containing the completions for a xabsl symbol file.
     */
    class SymbolCompletionProvider extends XabslDefaultCompletionProvider
    {
        public SymbolCompletionProvider() {
            addCompletions(Arrays.asList(
                //                        replacement, shortDesc,    summar
                new XabslCompletion(this, "internal", "internal", "internal statement"),
                new XabslCompletion(this, "input", "input", "input statement"),
                new XabslCompletion(this, "output", "output", "output statement"),
                new XabslCompletion(this, "enum", "enum", "enum statement"),
                new XabslCompletion(this, "float", "float", "float statement"),
                new XabslCompletion(this, "bool", "bool", "bool statement"),
                new XabslCompletion(this, "const", "const", "const statement"),
                //                          input, definition, template, shortDescription, summary
                new XabslTemplateCompletion(this, "enum", 
                                                  "enum input {}", 
                                                  "enum input ${name}\n{\n\t${cursor}\n};", 
                                                  "enum input symbol", 
                                                  "<pre>enum input <b>name</b>\n{\n    |\n};</pre>"),
                new XabslTemplateCompletion(this, "float", 
                                                  "float input", 
                                                  "float input ${name};", 
                                                  "float input symbol", 
                                                  "<pre>float input <b>name</b>;</pre>"),
                new XabslTemplateCompletion(this, "bool", 
                                                  "bool input", 
                                                  "bool input ${name};", 
                                                  "bool input symbol", 
                                                  "<pre>bool input <b>name</b>;</pre>"),
                new XabslTemplateCompletion(this, "enum", 
                                                  "enum output {}", 
                                                  "enum output ${name}\n{\n\t${cursor}\n};", 
                                                  "enum output symbol", 
                                                  "<pre>enum output <b>name</b>\n{\n    |\n};</pre>"),
                new XabslTemplateCompletion(this, "float", 
                                                  "float output", 
                                                  "float output ${name};", 
                                                  "float output symbol", 
                                                  "<pre>float output <b>name</b>;</pre>"),
                new XabslTemplateCompletion(this, "bool", 
                                                  "bool output", 
                                                  "bool output ${name};", 
                                                  "bool output symbol", 
                                                  "<pre>bool output <b>name</b>;</pre>"),
                new XabslTemplateCompletion(this, "const", 
                                                  "const name = value", 
                                                  "const ${name} = ${value};", 
                                                  "const symbol", 
                                                  "<pre>const <b>name</b> = <b>value</b>;</pre>")
            ));
        }
    } // END SymbolCompletionProvider
    
    /**
     * Completion provider containing the completions for a xabsl option file.
     */
    class OptionCompletionProvider extends XabslDefaultCompletionProvider
    {
        public OptionCompletionProvider() {
            addCompletions(Arrays.asList(
                //                        replacement, shortDesc,    summar
                new XabslCompletion(this, "initial", "initial", "initial statement"),
                new XabslCompletion(this, "target", "target", "target statement"),
                new XabslCompletion(this, "state", "state", "state statement"),
                new XabslCompletion(this, "common", "common", "common statement"),
                //                          input, definition, template, shortDescription, summary
                new XabslTemplateCompletion(this, "float parameter", 
                                                  "float @param", 
                                                  "float @${name};", 
                                                  "float parameter", 
                                                  "<pre>float @<b>name</b>;</pre>"),
                new XabslTemplateCompletion(this, "bool parameter", 
                                                  "bool @param", 
                                                  "bool @${name};", 
                                                  "bool parameter", 
                                                  "<pre>bool @<b>name</b>;</pre>"),
                new XabslTemplateCompletion(this, "enum parameter", 
                                                  "enum @param", 
                                                  "enum ${enumeration} @${name};", 
                                                  "enum parameter", 
                                                  "<pre>enum <b>enumeration</b> @<b>name</b>;</pre>"),
                new XabslTemplateCompletion(this, "initial state", 
                                                  "initial state { decision {} action {} }", 
                                                  "initial state ${name}\n{\n\tdecision {\n\t\t${if-else}\n\t}\n\taction {\n\t\t${cursor}\n\t}\n}", 
                                                  "initial state", 
                                                  "<pre>initial state <b>name</b>\n{\n    decision {\n        |\n}\n    action {\n        |\n    }\n}</pre>"),
                new XabslTemplateCompletion(this, "target state", 
                                                  "target state { decision {} action {} }", 
                                                  "target state ${name}\n{\n\tdecision {\n\t${if-else}\n\t}\n\taction {\n\t${cursor}\n\t}\n}", 
                                                  "target state", 
                                                  "<pre>target state <b>name</b>\n{\n    decision {\n        |\n}\n    action {\n        |\n    }\n}</pre>"),
                new XabslTemplateCompletion(this, "state", 
                                                  "state { decision {} action {} }", 
                                                  "state ${name}\n{\n\tdecision {\n\t${if-else}\n\t}\n\taction {\n\t${cursor}\n\t}\n}",
                                                  "state", 
                                                  "<pre>state <b>name</b>\n{\n    decision {\n        |\n}\n    action {\n        |\n    }\n}</pre>"),
                new XabslTemplateCompletion(this, "common decision", 
                                                  "common decision { if() goto }", 
                                                  "common decision\n{\n\tif(${condition})\n\t\tgoto ${cursor}\n}", 
                                                  "common decision", 
                                                  "<pre>common decision\n{\n    if(|)\n        goto |\n}</pre>")
            ));
        }
    } // END OptionCompletionProvider
    
    /**
     * Completion provider containing the completions applicable in a xabsl state part.
     */
    class StateCompletionProvider extends XabslDefaultCompletionProvider
    {
        public StateCompletionProvider() {
            addCompletions(Arrays.asList(
                //                        replacement, shortDesc,    summar
                new XabslCompletion(this, "decision", "decision", "decision statement"),
                new XabslCompletion(this, "action", "action", "action statement"),
                //                          input, definition, template, shortDescription, summary
                new XabslTemplateCompletion(this, "decision", 
                                                  "decision", 
                                                  "decision {\n\t${cursor}\n}", 
                                                  "decision", 
                                                  "<pre>decision {\n    |\n}</pre>"),
                new XabslTemplateCompletion(this, "action", 
                                                  "action", 
                                                  "action {\n\t${cursor}\n}", 
                                                  "action", 
                                                  "<pre>action {\n    |\n}</pre>")
            ));
        }
    } // END StateCompletionProvider
    
    /**
     * Completion provider containing the completions applicable in a xabsl decision part.
     */
    class DecisionCompletionProvider extends XabslDefaultCompletionProvider
    {
        public DecisionCompletionProvider() {
            addCompletions(Arrays.asList(
                //                        replacement, shortDesc,    summar
                new XabslCompletion(this, "if", "if", "if statement"),
                new XabslCompletion(this, "else", "else", "else statement"),
                new XabslCompletion(this, "stay", "stay", "stay statement"),
                new XabslCompletion(this, "action_done", "action_done", "action_done statement"),
                new XabslCompletion(this, "state_time", "state_time", "state_time statement"),
                new XabslCompletion(this, "option_time", "option_time", "option_time statement"),
                //                          input, definition, template, shortDescription, summary
                new XabslTemplateCompletion(this, "if statement", 
                                                  "if statement", 
                                                  "if (${condition})\n\t${cursor}", 
                                                  "if statement", 
                                                  "<pre>if (<b>condition</b>)\n    |</pre>"),
                new XabslTemplateCompletion(this, "if else statement", 
                                                  "if else statement", 
                                                  "if (${condition})\n\t${goto}\nelse\n\t${cursor}", 
                                                  "if else statement", 
                                                  "<pre>if (<b>condition</b>)\n    |\nelse\n    |</pre>"),
                new XabslTemplateCompletion(this, "else if statement", 
                                                  "else if statement", 
                                                  "else if (${condition})\n\t${cursor}", 
                                                  "else if statement", 
                                                  "<pre>else if (<b>condition</b>)\n    |</pre>"),
                new XabslTemplateCompletion(this, "goto", 
                                                  "goto", 
                                                  "goto ${cursor};", 
                                                  "goto", 
                                                  "<pre>goto |</pre>")
            ));
        }
    } // END DecisionCompletionProvider
} // END XabslCompletionProvider
