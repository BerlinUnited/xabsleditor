package de.naoth.xabsleditor.completion;

import de.naoth.xabsleditor.parser.XABSLOptionParser;
import de.naoth.xabsleditor.parser.XParser;
import java.util.Arrays;
import javax.swing.text.JTextComponent;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
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
                option_provider.addCompletion(new XabslVariableCompletion(option_provider, "@"+t.getName(), "Option parameter", t.getComment()));
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
                new XabslCompletion(this, "include", "include", "include"),
                new XabslCompletion(this, "namespace", "namespace", "namespace"),
                new XabslCompletion(this, "option", "option", "option"),
                new XabslCompletion(this, "agent", "agent", "agent"),
                //                          input, definition, template, shortDescription, summary
                new XabslTemplateCompletion(this, "include", "include file", "include \"${cursor}\";", "include", "include"),
                new XabslTemplateCompletion(this, "namespace", "namespace file", "namespace ${id}(\"${name}\")\n{\n${cursor}\n}", "namespace", "An id for the symbol collection. Must be identical to the file name without extension."),
                new XabslTemplateCompletion(this, "option", "option file", "option ${name}\n{\n${cursor}\n}", "option", "The name of the option. Must be identical to the file name without extension."),
                new XabslTemplateCompletion(this, "agent", "agent file", "agent ${id}(\"${agent-title}\", ${root-option});", "agent", "The name of the option. Must be identical to the file name without extension.")
            ));
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
                new XabslCompletion(this, "internal", "internal", "internal"),
                new XabslCompletion(this, "input", "input", "input"),
                new XabslCompletion(this, "output", "output", "output"),
                new XabslCompletion(this, "enum", "enum", "enum"),
                new XabslCompletion(this, "float", "float", "float"),
                new XabslCompletion(this, "bool", "bool", "bool"),
                new XabslCompletion(this, "const", "const", "const"),
                //                          input, definition, template, shortDescription, summary
                new XabslTemplateCompletion(this, "enum", "enum input symbol", "enum input ${name}\n{\n${cursor}\n};", "enum input symbol", "enum input symbol"),
                new XabslTemplateCompletion(this, "float", "float input symbol", "float input ${name};", "float input symbol", "float input symbol"),
                new XabslTemplateCompletion(this, "bool", "bool input symbol", "bool input ${name};", "bool input symbol", "bool input symbol"),
                new XabslTemplateCompletion(this, "enum", "enum output symbol", "enum output ${name}\n{\n${cursor}\n};", "enum output symbol", "enum output symbol"),
                new XabslTemplateCompletion(this, "float", "float output symbol", "float output ${name};", "float output symbol", "float output symbol"),
                new XabslTemplateCompletion(this, "bool", "bool output symbol", "bool output ${name};", "bool output symbol", "bool output symbol"),
                new XabslTemplateCompletion(this, "const", "const symbol", "const ${name} = ${value};", "const symbol", "const symbol")
            ));
        }
    } // END SymbolCompletionProvider
    
    class OptionCompletionProvider extends DefaultCompletionProvider
    {
        public OptionCompletionProvider() {
            addCompletions(Arrays.asList(
                //                        replacement, shortDesc,    summar
                new XabslCompletion(this, "initial", "initial", "initial"),
                new XabslCompletion(this, "target", "target", "target"),
                new XabslCompletion(this, "state", "state", "state"),
                new XabslCompletion(this, "common", "common", "common"),
                //                          input, definition, template, shortDescription, summary
                new XabslTemplateCompletion(this, "float parameter", "float parameter", "float @${name}", "float parameter", "float parameter"),
                new XabslTemplateCompletion(this, "bool parameter", "bool parameter", "bool @${name}", "bool parameter", "bool parameter"),
                new XabslTemplateCompletion(this, "enum parameter", "enum parameter", "enum ${enumeration} @${name}", "enum parameter", "enum parameter"),
                new XabslTemplateCompletion(this, "initial state", "initial state", "initial state ${name}\n{\n\tdecision {\n\t${cursor}}\n\taction {\n\t}\n}", "initial state", "initial state"),
                new XabslTemplateCompletion(this, "target state", "target state", "target state ${name}\n{\n\tdecision {\n\t${cursor}}\n\taction {\n\t}\n}", "target state", "target state"),
                new XabslTemplateCompletion(this, "state", "state", "state ${name}\n{\n\tdecision {\n\t${cursor}}\n\taction {\n\t}\n}", "state", "state"),
                new XabslTemplateCompletion(this, "common decision", "common decision", "common decision\n{\n\tif(${cursor})\n\t\tgoto\n}", "common decision", "common decision")
            ));
        }
    } // END OptionCompletionProvider
    
    class StateCompletionProvider extends DefaultCompletionProvider
    {
        public StateCompletionProvider() {
            addCompletions(Arrays.asList(
                //                        replacement, shortDesc,    summar
                new XabslCompletion(this, "decision", "decision", "decision"),
                new XabslCompletion(this, "action", "action", "action"),
                //                          input, definition, template, shortDescription, summary
                new XabslTemplateCompletion(this, "decision", "decision", "decision {\n\t${cursor}}", "decision", "decision"),
                new XabslTemplateCompletion(this, "action", "action", "action {\n\t}", "action", "action")
            ));
        }
    } // END StateCompletionProvider
    
    class DecisionCompletionProvider extends DefaultCompletionProvider
    {
        public DecisionCompletionProvider() {
            addCompletions(Arrays.asList(
                //                        replacement, shortDesc,    summar
                new XabslCompletion(this, "if", "if", "if"),
                new XabslCompletion(this, "else", "else", "else"),
                new XabslCompletion(this, "stay", "stay", "stay"),
//                new BasicCompletion(this, "goto", "goto", "goto"),
                //                          input, definition, template, shortDescription, summary
                new XabslTemplateCompletion(this, "if statement", "if statement", "if (${condition})\n\t${cursor}", "if statement", "if statement"),
                new XabslTemplateCompletion(this, "if else statement", "if else statement", "if (${condition})\n\t${cursor}\n\telse\t\n${curosr}", "if else statement", "if else statement"),
                new XabslTemplateCompletion(this, "else if statement", "else if statement", "else if (${condition})\n\t${cursor}", "else if statement", "else if statement"),
                new XabslTemplateCompletion(this, "goto", "goto", "goto ${cursor}", "goto", "goto")
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
