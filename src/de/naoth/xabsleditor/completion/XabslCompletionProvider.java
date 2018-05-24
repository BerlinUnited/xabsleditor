package de.naoth.xabsleditor.completion;

import de.naoth.xabsleditor.parser.XABSLOptionParser;
import de.naoth.xabsleditor.parser.XParser;
import java.util.Arrays;
import javax.swing.text.JTextComponent;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
import org.fife.ui.autocomplete.TemplateCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;

/**
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
    private final CompletionProvider actionProvider = new ActionCompletionProvider();
    private final CompletionProvider optionsProvider = new OptionsCompletionProvider();

    public XabslCompletionProvider() {
        super(new XabslDefaultCompletionProvider());
        /*
        // add some default macros
        ((DefaultCompletionProvider)getDefaultCompletionProvider()).addCompletion(new ShorthandCompletion(getDefaultCompletionProvider(),
                "state",                                                 // input text
                "state <name> {\n\tdecision {\n\t}\n\taction {\n\t}\n}", // replacement
                "behavior state",                                        // short description
                "behavior state"                                         // summary
        ));
        */
        //((DefaultCompletionProvider)getDefaultCompletionProvider()).setParameterizedCompletionParams('(', ", ", ')');
    }

    @Override
    protected CompletionProvider getProviderFor(JTextComponent comp) {
        RSyntaxTextArea rsta = (RSyntaxTextArea)comp;
        int line = rsta.getCaretLineNumber();
        CompletionProvider provider = null;
        for (int i = line; i >= 0; i--) {
            Token l = rsta.getTokenListForLine(i);
            while(l != null) {
                if(l.getType() != Token.NULL) {
                    switch(l.getLexeme()) {
                        case "goto":      provider = optionsProvider;  break;
                        case "if":
                        case "else":
                        case "decision":  provider = updateLocalParameter((XParser)rsta.getParser(0), (DefaultCompletionProvider) decisionProvider); break;
                        case "action":    provider = updateLocalParameter((XParser)rsta.getParser(0), (DefaultCompletionProvider) actionProvider);   break;
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
        
        return provider;//super.getProviderFor(comp);
    }
    
    public CompletionProvider updateLocalParameter(XParser parser, DefaultCompletionProvider provider) {
        if(parser.getFileParser() instanceof XABSLOptionParser) {
            DefaultCompletionProvider option_provider = new DefaultCompletionProvider();
            ((XABSLOptionParser)parser.getFileParser()).getOption().getParameter().forEach((t) -> {
                option_provider.addCompletion(new BasicCompletion(option_provider, "@"+t.getName(), "Option parameter", t.getComment()));
            });
            option_provider.setParent(provider);
            return option_provider;
        }
        return provider;
    }
    
    public void updateSymbols(CompletionProvider provider) {
        actionProvider.setParent(provider);
        decisionProvider.setParent(provider);
    }
    
    public void updateOptions(CompletionProvider provider) {
        optionsProvider.setParent(provider);
        actionProvider.setParent(provider);
    }
    
    class EmptyCompletionProvider extends DefaultCompletionProvider
    {

        public EmptyCompletionProvider() {
            addCompletions(Arrays.asList(
                //                        replacement, shortDesc,    summary               
                new BasicCompletion(this, "include", "include", "include"),
                new BasicCompletion(this, "namespace", "namespace", "namespace"),
                new BasicCompletion(this, "option", "option", "option"),
                new BasicCompletion(this, "agent", "agent", "agent"),
                //                          input, definition, template, shortDescription, summary
                new TemplateCompletion(this, "include", "include file", "include \"${cursor}\";", "include", "include"),
                new TemplateCompletion(this, "namespace", "namespace file", "namespace ${id}(\"${name}\")\n{\n${cursor}\n}", "namespace", "An id for the symbol collection. Must be identical to the file name without extension."),
                new TemplateCompletion(this, "option", "option file", "option ${name}\n{\n${cursor}\n}", "option", "The name of the option. Must be identical to the file name without extension."),
                new TemplateCompletion(this, "agent", "agent file", "agent ${id}(\"${agent-title}\", ${root-option});", "agent", "The name of the option. Must be identical to the file name without extension.")
            ));
            /*
            addCompletion(new ShorthandCompletion(getDefaultCompletionProvider(),
                "state",                                                 // input text
                "state <name> {\n\tdecision {\n\t}\n\taction {\n\t}\n}", // replacement
                "behavior state",                                        // short description
                "behavior state"                                         // summary
            ));
            */
        }
    } // END EmptyCompletionProvider
    
    class NamespaceCompletionProvider extends DefaultCompletionProvider {

        public NamespaceCompletionProvider() {
        }
        
    }
    
    class SymbolCompletionProvider extends DefaultCompletionProvider
    {

        public SymbolCompletionProvider() {
            addCompletions(Arrays.asList(
                //                        replacement, shortDesc,    summar
                new BasicCompletion(this, "internal", "internal", "internal"),
                new BasicCompletion(this, "input", "input", "input"),
                new BasicCompletion(this, "output", "output", "output"),
                new BasicCompletion(this, "enum", "enum", "enum"),
                new BasicCompletion(this, "float", "float", "float"),
                new BasicCompletion(this, "bool", "bool", "bool"),
                new BasicCompletion(this, "const", "const", "const"),
                //                          input, definition, template, shortDescription, summary
                new TemplateCompletion(this, "enum", "enum input symbol", "enum input ${name}\n{\n${cursor}\n};", "enum input symbol", "enum input symbol"),
                new TemplateCompletion(this, "float", "float input symbol", "float input ${name};", "float input symbol", "float input symbol"),
                new TemplateCompletion(this, "bool", "bool input symbol", "bool input ${name};", "bool input symbol", "bool input symbol"),
                new TemplateCompletion(this, "enum", "enum output symbol", "enum output ${name}\n{\n${cursor}\n};", "enum output symbol", "enum output symbol"),
                new TemplateCompletion(this, "float", "float output symbol", "float output ${name};", "float output symbol", "float output symbol"),
                new TemplateCompletion(this, "bool", "bool output symbol", "bool output ${name};", "bool output symbol", "bool output symbol"),
                new TemplateCompletion(this, "const", "const symbol", "const ${name} = ${value};", "const symbol", "const symbol")
            ));
        }
    } // END SymbolCompletionProvider
    
    class OptionCompletionProvider extends DefaultCompletionProvider
    {
        public OptionCompletionProvider() {
            addCompletions(Arrays.asList(
                //                        replacement, shortDesc,    summar
                new BasicCompletion(this, "initial", "initial", "initial"),
                new BasicCompletion(this, "target", "target", "target"),
                new BasicCompletion(this, "state", "state", "state"),
                new BasicCompletion(this, "common", "common", "common"),
                //                          input, definition, template, shortDescription, summary
                new TemplateCompletion(this, "float parameter", "float parameter", "float @${name}", "float parameter", "float parameter"),
                new TemplateCompletion(this, "bool parameter", "bool parameter", "bool @${name}", "bool parameter", "bool parameter"),
                new TemplateCompletion(this, "enum parameter", "enum parameter", "enum ${enumeration} @${name}", "enum parameter", "enum parameter"),
                new TemplateCompletion(this, "initial state", "initial state", "initial state ${name}\n{\n\tdecision {\n\t${cursor}}\n\taction {\n\t}\n}", "initial state", "initial state"),
                new TemplateCompletion(this, "target state", "target state", "target state ${name}\n{\n\tdecision {\n\t${cursor}}\n\taction {\n\t}\n}", "target state", "target state"),
                new TemplateCompletion(this, "state", "state", "state ${name}\n{\n\tdecision {\n\t${cursor}}\n\taction {\n\t}\n}", "state", "state"),
                new TemplateCompletion(this, "common decision", "common decision", "common decision\n{\n\tif(${cursor})\n\t\tgoto\n}", "common decision", "common decision")
            ));
        }
    } // END OptionCompletionProvider
    
    class StateCompletionProvider extends DefaultCompletionProvider
    {
        public StateCompletionProvider() {
            addCompletions(Arrays.asList(
                //                        replacement, shortDesc,    summar
                new BasicCompletion(this, "decision", "decision", "decision"),
                new BasicCompletion(this, "action", "action", "action"),
                //                          input, definition, template, shortDescription, summary
                new TemplateCompletion(this, "decision", "decision", "decision {\n\t${cursor}}", "decision", "decision"),
                new TemplateCompletion(this, "action", "action", "action {\n\t}", "action", "action")
            ));
        }
    } // END StateCompletionProvider
    
    class DecisionCompletionProvider extends DefaultCompletionProvider
    {
        public DecisionCompletionProvider() {
            addCompletions(Arrays.asList(
                //                        replacement, shortDesc,    summar
                new BasicCompletion(this, "if", "if", "if"),
                new BasicCompletion(this, "else", "else", "else"),
                new BasicCompletion(this, "stay", "stay", "stay"),
//                new BasicCompletion(this, "goto", "goto", "goto"),
                //                          input, definition, template, shortDescription, summary
                new TemplateCompletion(this, "if statement", "if statement", "if (${condition})\n\t${cursor}", "if statement", "if statement"),
                new TemplateCompletion(this, "if else statement", "if else statement", "if (${condition})\n\t${cursor}\n\telse\t\n${curosr}", "if else statement", "if else statement"),
                new TemplateCompletion(this, "else if statement", "else if statement", "else if (${condition})\n\t${cursor}", "else if statement", "else if statement"),
                new TemplateCompletion(this, "goto", "goto", "goto ${cursor}", "goto", "goto")
            ));
        }
    } // END DecisionCompletionProvider
    
    class ActionCompletionProvider extends DefaultCompletionProvider
    {
        public ActionCompletionProvider() {
            // TODO: add symbols, options, behavior(?)
        }
    } // END ActionCompletionProvider
    
    class OptionsCompletionProvider extends DefaultCompletionProvider
    {
        public OptionsCompletionProvider() {
            // TODO: add symbols, options, behavior(?)
        }
    } // END OptionCompletionProvider
    
}
