/*
 * 
 */
package de.hu_berlin.informatik.ki.jxabsleditor.parser;

import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext.XABSLBasicSymbol;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext.XABSLEnum;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext.XABSLOption;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext.XABSLSymbol;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLOptionContext.State;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLOptionContext.Transition;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLOptionContext.Transition;
import edu.uci.ics.jung.graph.Graph;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.Parser;
import org.fife.ui.rsyntaxtextarea.ParserNotice;
import org.fife.ui.rsyntaxtextarea.Token;

/**
 *
 * @author Heinrich Mellmann
 */
public class XParser implements Parser
{

  private java.util.ArrayList noticeList = new java.util.ArrayList(1);
  private Token currentToken;

  private XABSLContext xabslContext = null;
  private XABSLOptionContext xabslOptionContext = null;

  public XParser(XABSLContext xabslContext)
  {
    this.xabslContext = xabslContext;
    if(xabslContext == null)
    {
      this.xabslContext = new XABSLContext();
    }
  }

  public XParser()
  {
    this.xabslContext = new XABSLContext();
  }
  

  @Override
  public void parse(Reader reader)
  {
    noticeList.clear();

    try
    {
      StringBuilder buffer = new StringBuilder();

      int c = 1;
      while((c = reader.read()) > -1)
      {
        buffer.append((char) c);
      }
      // construct char array
      char[] charArray = new char[buffer.length()];
      buffer.getChars(0, charArray.length, charArray, 0);
      // create segment
      Segment text = new Segment(charArray, 0, charArray.length);

      XTokenMaker tokenizer = new XTokenMaker();
      currentToken = tokenizer.getTokenList(text, Token.NULL, 0);

      try
      {
        if(currentToken != null && currentToken.type != Token.NULL)
        {
          skipSpace();
          if(isToken("option"))
          {
            xabslOptionContext = new XABSLOptionContext();
            parseOption();
          }
          else if(isToken("namespace"))
          {
            parseNamespace();
          }
        }

        if(currentToken != null && currentToken.type != Token.NULL)
        {
          throw new Exception("Unexpected end of file.");
          //System.out.println("Unexpected end of File.");
        }
      }
      catch(Exception e)
      {
        System.err.println(e.getMessage());
      }

    }
    catch(java.io.IOException ioe)
    {
      ioe.printStackTrace();
    }
  }//end parse

  // PARSE SYMBOLS
  XABSLSymbol currentSymbol;
  XABSLEnum currentEnumDeclaration;

  private void parseNamespace() throws Exception
  {
    isTokenAndEat("namespace");
    isTokenAndEat(Token.FUNCTION);
    isTokenAndEat("(");
    isTokenAndEat(Token.LITERAL_STRING_DOUBLE_QUOTE);
    isTokenAndEat(")");
    isTokenAndEat("{");

    while(!isToken("}"))
    {
      parseSymbolsEntry();
    }//end while

    isTokenAndEat("}");
  }//end parseNamespace

  private void parseSymbolsEntry() throws Exception
  {
    String type;
    String comment = this.currentComment;
    this.currentComment = "";

    // enum
    if(isToken("enum"))
    {
      boolean isInternal = false;
      eat();
      type = parseIdentifier();
      if(isToken("internal"))
      {
        isTokenAndEat("internal");
        isInternal = true;
      }
      
      if(isToken("{"))
      {
        currentEnumDeclaration = new XABSLEnum(type);
        parseEnumDeclaration();
        this.xabslContext.add(currentEnumDeclaration);
      }
      else // it's a enum symbol definition :)
      {
        currentSymbol = new XABSLSymbol();

        XABSLEnum enumType = this.xabslContext.getEnumMap().get(type);
        if(enumType != null)
          currentSymbol.setType(enumType);
        else // enum type is not declared
          currentSymbol.setType(type);
        currentSymbol.setComment(comment);

        if(!isInternal)
        {
          if(isToken("output"))
          {
            currentSymbol.setSecondaryType(XABSLSymbol.SecondaryType.output);
            isTokenAndEat("output");
          }
          else
          {
            currentSymbol.setSecondaryType(XABSLSymbol.SecondaryType.input);
            isTokenAndEat("input");
          }
        }else
        {
          currentSymbol.setSecondaryType(XABSLSymbol.SecondaryType.internal);
        }
        
        currentSymbol.setName(parseIdentifier());
        isTokenAndEat(";");
        
        this.xabslContext.add(currentSymbol);
      }
    }
    else if(isToken("float") || isToken("bool"))
    {
      currentSymbol = new XABSLSymbol();
      currentSymbol.setType(currentToken.getLexeme());
      currentSymbol.setComment(comment);
      
      eat();
      
      parseSymbolDeclaration();
      
      this.xabslContext.add(currentSymbol);
    }
    else
    {
      eat();
      noticeList.add(new ParserNotice("A symbol declaration or enum definition expected.", currentToken.offset, currentToken.getLexeme().length()));
    }
    
  }//end parseSymbolsEntry

  private void parseEnumDeclaration() throws Exception
  {
    isTokenAndEat("{");
    currentEnumDeclaration.add(parseIdentifier());

    while(isToken(","))
    {
      eat(); // eat ","
      currentEnumDeclaration.add(parseIdentifier());
    }//end while
    
    isTokenAndEat("}");
    isTokenAndEat(";");
  }//end parseEnumDeclaration

  private void parseSymbolDeclaration() throws Exception
  {
    if(isToken("output"))
    {
      currentSymbol.setSecondaryType(XABSLSymbol.SecondaryType.output);
      isTokenAndEat("output");
    }
    else if(isToken("input"))
    {
      currentSymbol.setSecondaryType(XABSLSymbol.SecondaryType.input);
      isTokenAndEat("input");
    }
    else
    {
      currentSymbol.setSecondaryType(XABSLSymbol.SecondaryType.internal);
      isTokenAndEat("internal");
    }

    currentSymbol.setName(parseIdentifier());

    if(isToken("["))
    {
      do
      {
        eat();
      }
      while(!isToken("]"));

      isTokenAndEat("]");
    }//end if

    if(isToken(Token.LITERAL_STRING_DOUBLE_QUOTE))
    {
      isTokenAndEat(Token.LITERAL_STRING_DOUBLE_QUOTE);
    }

    
    // parse function symbol parameters
    if(isToken("("))
    {
      this.currentComment = "";
      eat();// eat "("

      // parse symbol parameter
      while(!isToken(")"))
      {
        XABSLBasicSymbol parameter = new XABSLBasicSymbol();
        parameter.setComment(this.currentComment);
        
        // parse parameter type if given (float by default)
        parameter.setType("float");
        if(isToken("bool"))
        {
          isTokenAndEat("bool");
          parameter.setType("bool");
        }else if(isToken("float"))
        {
          isTokenAndEat("float");
        }else if(isToken("enum"))
        {
          isTokenAndEat("enum");
          
          String enumName = parseIdentifier();
          XABSLEnum enumType = this.xabslContext.getEnumMap().get(enumName);
          if(enumType == null)
            parameter.setType(enumName);
          else
            parameter.setType(enumType);
        }

        parameter.setName(parseIdentifier()); // parameter name

        if(parameter.getType().equals("float"))
        {
          // parse range if defined
          String range = "";
          if(isToken("["))
          {
            eat();
            while(!isToken("]"))
            {
              range += this.currentToken.getLexeme();
              eat();
            }//end while
            isTokenAndEat("]");
            parameter.setRange(range);
          }//end if

          // parse unit if defined
          if(isToken(Token.LITERAL_STRING_DOUBLE_QUOTE))
          {
            parameter.setUnit(this.currentToken.getLexeme());
            isTokenAndEat(Token.LITERAL_STRING_DOUBLE_QUOTE);
          }//end if
          //System.out.println(parameter);
        }//end if type = float

        this.currentSymbol.addParameter(parameter);

        isTokenAndEat(";");
      }//end while
      
      isTokenAndEat(")");
    }//end if


    isTokenAndEat(";");
  }//end parseSymbolDeclaration
  
  // PARSE OPTION
  private String currentStateName;
  private Set<String> currentOutgoingOptions;
  private String currentComment;
  private boolean currentStateInitial;
  private boolean currentStateTarget;

  private XABSLOption currentOption;

  private void parseOption() throws Exception
  {
    isTokenAndEat("option");
    currentOption = new XABSLOption(parseIdentifier());
    currentOption.setComment(currentComment);
    currentComment = "";
    isTokenAndEat("{");

    while(isToken("float") || isToken("bool") || isToken("enum"))
    {
      parseSymbolDefinition();
    }

    if(isToken("common"))
    {
      parseCommonDecision();
    }

    while(!isToken("}"))
    {
      parseState();
    }//end while
    isTokenAndEat("}");

    this.xabslContext.add(currentOption);
  }//end parseOption

  private void parseSymbolDefinition() throws Exception
  {
    XABSLBasicSymbol parameter = new XABSLBasicSymbol();
    parameter.setComment(this.currentComment);
    
    if(isToken("bool"))
    {
      parameter.setType("bool");
      isTokenAndEat("bool");
    }
    else if(isToken("float"))
    {
      parameter.setType("float");
      isTokenAndEat("float");
    }else
    {
      // try to parse enum
      isTokenAndEat("enum");
      String type = parseIdentifier();
      parameter.setType(this.xabslContext.getEnumMap().get(type));
    }

    isTokenAndEat("@");

    // eat name
    parameter.setName(parseIdentifier());

    if(isToken("["))
    {
      eat();
      eat();
      isTokenAndEat("]");
    }//end if

    isTokenAndEat(";");
    this.currentOption.addParameter(parameter);
  }//end parseSymbolDefinition

  private void parseCommonDecision() throws Exception
  {
    isTokenAndEat("common");
    isTokenAndEat("decision");
    parseBlock();
  }//end parseState

  private void eatInitialOrTarget() throws Exception
  {
    if(!currentStateInitial && isToken("initial"))
    {
      eat();
      currentStateInitial = true;
    }
    else if(!currentStateTarget && isToken("target"))
    {
      eat();
      currentStateTarget = true;
    }
  }

  private void parseState() throws Exception
  {

    currentStateInitial = false;
    currentStateTarget = false;
    currentOutgoingOptions = new HashSet<String>();

    eatInitialOrTarget();
    // we can have initial *and* target state
    eatInitialOrTarget();

    isTokenAndEat("state");

    int offset = currentToken.offset;
    currentStateName = parseIdentifier();


    isTokenAndEat("{");
    if(isToken("decision"))
    {
      parseDecision();
    }
    parseAction();

    addState(currentStateName, 
             currentComment,
             offset,
             currentStateTarget,
             currentStateInitial,
             currentOutgoingOptions);
    
    isTokenAndEat("}");
  }//end parseState

  private void parseDecision() throws Exception
  {
    isTokenAndEat("decision");
    /*
    isTokenAndEat("{");

    boolean resume = true;

    while(resume)
    {
    if(isToken("if"))
    {
    isTokenAndEat("if");
    isTokenAndEat("(");
    parseBooleanExpression();
    isTokenAndEat(")");
    isTokenAndEat("{");
    parseSingleDecision();
    isTokenAndEat("}");
    }
    else if(isToken("else"))
    {
    isTokenAndEat("else");
    // "else if" ?
    if(isToken("if"))
    {
    isTokenAndEat("if");
    isTokenAndEat("(");
    parseBooleanExpression();
    isTokenAndEat(")");
    }
    else
    {
    // only one else allowed, and only at the end
    resume = false;
    }

    isTokenAndEat("{");
    parseSingleDecision();
    isTokenAndEat("}");
    }
    else
    {
    parseSingleDecision();
    // nothing found
    resume = false;
    }
    }
     */

    //isTokenAndEat("}");
    parseBlock();
  }//end parseDecision

  private void parseSingleDecision() throws Exception
  {
    if(isToken("goto"))
    {
      parseGoto();
    }
    else if(isToken("stay"))
    {
      isTokenAndEat("stay");
      addTransition(currentStateName, currentStateName, currentToken.offset);
      isTokenAndEat(";");
    }
    else
    {
      noticeList.add(new ParserNotice(
        "Either \"stay\" or \"goto\" needed in decision",
        currentToken.offset, Math.max(currentToken.textCount, 2)));

    }
  }//end parseSingleDecision

  private void parseAction() throws Exception
  {
    isTokenAndEat("action");

    isTokenAndEat("{");
    while(!isToken("}"))
    {
      parseExpression();
    }//end while
    isTokenAndEat("}");

  }//end parseAction

  private void parseGoto() throws Exception
  {
    isTokenAndEat("goto");

    int offset = currentToken.offset;
    String targetStateName = parseIdentifier();

    addTransition(currentStateName, targetStateName, offset);
    isTokenAndEat(";");

  //System.out.println(currentStateName + " -> " + targetStateName);
  }//end parseGoto

  private void parseExpression() throws Exception
  {
    if(isToken(Token.FUNCTION))
    {
      parseFunction();
    }
    else
    {
      parseAssignment();
    }
  }//end parseExpression

  private void parseFunction() throws Exception
  {
    if(isToken(Token.FUNCTION))
    {
      currentOutgoingOptions.add(currentToken.getLexeme());
    }
    parseFunctionSingle();
    isTokenAndEat(";");
  }//end parseFunction

  private void parseFunctionSingle() throws Exception
  {    
    isTokenAndEat(Token.FUNCTION);
    parseFunctionParameter();
  }

  private void parseFunctionParameter() throws Exception
  {    

    isTokenAndEat("(");
    int bracketCount = 1;

    while(bracketCount > 0)
    {
      if(isToken(Token.LITERAL_NUMBER_DECIMAL_INT) || isToken(Token.LITERAL_NUMBER_FLOAT))
      {
        eat();
      }
      else if(isToken(Token.FUNCTION))
      {
        parseFunctionSingle();
      }
      else if(isToken(Token.IDENTIFIER))
      {
        parseIdentifier();
      }
      else if(isToken("="))
      {
        isTokenAndEat("=");
      }
      else if(isToken("("))
      {
        isTokenAndEat("(");
        bracketCount++;
      }
      else if(isToken(")"))
      {
        isTokenAndEat(")");
        bracketCount--;
      }
      else
      {
        // skip
        eat();
      }

      if(isToken(","))
      {
        isTokenAndEat(",");
      }

    }
  }//end parseFunctionParameter

  private void parseBooleanExpression() throws Exception
  {
    if(isToken("true"))
    {
      isTokenAndEat("true");
    }
    else if(isToken("false"))
    {
      isTokenAndEat("false");
    }
    else if(isToken("&&"))
    {
      isTokenAndEat("&&");
      parseBooleanExpression();
    }
    else if(isToken("||"))
    {
      isTokenAndEat("|");
      parseBooleanExpression();
    }
    else if(isToken("<="))
    {
      isTokenAndEat("<=");
      parseBooleanExpression();
    }
    else if(isToken(">="))
    {
      isTokenAndEat(">=");
      parseBooleanExpression();
    }
    else if(isToken("<"))
    {
      isTokenAndEat("<");
      parseBooleanExpression();
    }
    else if(isToken(">"))
    {
      isTokenAndEat(">");
      parseBooleanExpression();
    }
    else if(isToken("=="))
    {
      isTokenAndEat("==");
      parseBooleanExpression();
    }
    else if(isToken("("))
    {
      isTokenAndEat("(");
      parseBooleanExpression();
      isTokenAndEat(")");
    }
    else
    {
      parseIdentifier();
      if(!isToken(")"))
      {
        parseBooleanExpression();
      }
    }
  }//end parseBooleanExpression


  // parse a block surounded by {}
  private void parseBlock() throws Exception
  {
    isTokenAndEat("{");

    while(!isToken("}"))
    {
      if(isToken("{"))
      {
        parseBlock();
      }
      else if(isToken("goto") || isToken("stay"))
      {
        parseSingleDecision();
      }
      else
      {
        eat();
      }
    }//end while

    isTokenAndEat("}");
  }//end parseBlock

  private void parseAssignment() throws Exception
  {
    parseIdentifier();
    if(!isTokenAndEat("="))
    {
      return;
    }

    int parenthesisCount = 0;

    while(isToken(Token.IDENTIFIER) || isToken(Token.LITERAL_BOOLEAN) 
      || isToken(Token.LITERAL_NUMBER_DECIMAL_INT)
      || isToken(Token.LITERAL_NUMBER_FLOAT) || isToken(Token.OPERATOR)
      || isToken("(") || isToken(")") || isToken("@") || isToken(Token.FUNCTION))
    {

      if(isToken("@"))
      {
        isTokenAndEat("@");
      }
      else if(isToken(Token.IDENTIFIER))
      {
        parseIdentifier();
      }
      else if(isToken("("))
      {
        eat();
        parenthesisCount++;
      }
      else if(isToken(")"))
      {
        eat();
        parenthesisCount--;
      }
      else if(isToken(Token.FUNCTION))
      {
        parseFunctionSingle();
      }
      else
      {
        eat();
      }
    }

    if(parenthesisCount < 0)
    {
      noticeList.add(new ParserNotice(
        "More right paranthesis than left ones (" + Math.abs(parenthesisCount) + ")",
        currentToken.offset, Math.max(currentToken.textCount, 2)));
    }
    else if(parenthesisCount > 0)
    {
      noticeList.add(new ParserNotice(
        "More left paranthesis than right ones (" + Math.abs(parenthesisCount) + ")",
        currentToken.offset, Math.max(currentToken.textCount, 2)));
    }

    isTokenAndEat(";");



  }//end parseAssignment

  private String parseIdentifier() throws Exception
  {
    if(isToken(Token.IDENTIFIER) || isToken(Token.ERROR_IDENTIFIER) || isToken(Token.FUNCTION))
    {
      String id = currentToken.getLexeme();
      eat();
      return id;
    }
    else
    {
      noticeList.add(new ParserNotice("Identifier expected", currentToken.offset, Math.max(currentToken.textCount, 2)));
    }

    return null;
  }//end parseIdentifier

  private void skipSpace()
  {
    while(currentToken != null &&
      (currentToken.type == Token.WHITESPACE ||
      currentToken.type == Token.NULL ||
      currentToken.type == Token.COMMENT_DOCUMENTATION ||
      currentToken.type == Token.COMMENT_EOL ||
      currentToken.type == Token.COMMENT_MULTILINE))
    {
      // accept only dokumentation comments (i.e. /** ... */)
      if( //currentToken.type == Token.COMMENT_EOL ||
          //currentToken.type == Token.COMMENT_MULTILINE ||
          currentToken.type == Token.COMMENT_DOCUMENTATION )
      {
        currentComment = getCommentString(currentToken.getLexeme()); // remember last coment
      }
      currentToken = currentToken.getNextToken();
    }//end while
  }//end skipSpace

  private void eat() throws Exception
  {
    try
    {
      if(currentToken == null)
      {
        throw new Exception("Unexpected end of file.");
      }
      //System.out.println(currentToken.getLexeme() +  " " + currentToken.type);
      currentToken = currentToken.getNextToken();
      skipSpace();
    }
    catch(Exception e)
    {
      System.out.println("ERROR: " + currentToken.toString());
    }
  }//end eat

  private boolean isToken(int type) throws Exception
  {
    if(currentToken == null)
    {
      return false;
    }

    boolean result = currentToken.type == type;
    return result;
  }//end isTokenAndEat

  private boolean isToken(String keyWord) throws Exception
  {
    if(currentToken == null)
    {
      return false;
    }

    boolean result = keyWord.equals(currentToken.getLexeme());
    return result;
  }//end isTokenAndEat

  private boolean isTokenAndEat(int type) throws Exception
  {
    if(currentToken == null)
    {
      return false;
    }

    boolean result = isToken(type);

    if(!result)
    {
      String message = "is " + getNameForTokenType(currentToken.type) + " but " + getNameForTokenType(type) + " expected";
      noticeList.add(new ParserNotice(message, currentToken.offset, currentToken.getLexeme().length()));
      throw new Exception("Unexpected token type: " + message);
    }

    eat();
    return result;
  }//end isTokenAndEat

  private boolean isTokenAndEat(String keyWord) throws Exception
  {
    if(currentToken == null)
    {
      return false;
    }

    boolean result = isToken(keyWord);

    if(!result)
    {
      String message = keyWord + " expected";
      noticeList.add(new ParserNotice(message, currentToken.offset, currentToken.getLexeme().length()));
      throw new Exception("Unexpected token: " + message);
    }//end if

    eat();
    return result;
  }//end isTokenAndEat

  @Override
  public Iterator getNoticeIterator()
  {
    return noticeList.iterator();
  }//end getNoticeIterator

  /** Get a graph suited for visualizing */
  public Graph<XabslNode, XabslEdge> getOptionGraph()
  {
    return this.xabslOptionContext.getOptionGraph();
  }


  private void addState(
          String name,
          String comment,
          int offset,
          boolean target,
          boolean initial,
          Set<String> outgoingOptions
          ) throws Exception
  {
    if(this.xabslOptionContext.getStateMap().containsKey(name))
    {
      noticeList.add(new ParserNotice("State " + name + " already defined.", offset, name.length()));
      throw new Exception("State " + name + " already defined.");
    }//end if

    // create new state :)
    this.xabslOptionContext.new State(
            name,
            comment,
            offset,
            target,
            initial,
            outgoingOptions);
    
  }//end addState


  private void addTransition(String from, String to, int offset)
  {
    // create new transition
    this.xabslOptionContext.new Transition(from, to, offset);
  }//end addTransition

  
  public static String getNameForTokenType(int type)
  {
    switch(type)
    {
      case Token.COMMENT_DOCUMENTATION:
        return "COMMENT_DOCUMENTATION";
      case Token.COMMENT_EOL:
        return "COMMENT_EOL";
      case Token.COMMENT_MULTILINE:
        return "COMMENT_MULTILINE";
      case Token.DATA_TYPE:
        return "DATA_TYPE";
      case Token.ERROR_CHAR:
        return "ERROR_CHAR";
      case Token.ERROR_IDENTIFIER:
        return "ERROR_IDENTIFIER";
      case Token.ERROR_NUMBER_FORMAT:
        return "ERROR_NUMBER_FORMAT";
      case Token.ERROR_STRING_DOUBLE:
        return "ERROR_STRING_DOUBLE";
      case Token.FUNCTION:
        return "FUNCTION";
      case Token.IDENTIFIER:
        return "IDENTIFIER";
      case Token.LITERAL_BACKQUOTE:
        return "LITERAL_BACKQUOTE";
      case Token.LITERAL_BOOLEAN:
        return "LITERAL_BOOLEAN";
      case Token.LITERAL_CHAR:
        return "LITERAL_CHAR";
      case Token.LITERAL_NUMBER_DECIMAL_INT:
        return "LITERAL_NUMBER_DECIMAL_INT";
      case Token.LITERAL_NUMBER_FLOAT:
        return "LITERAL_NUMBER_FLOAT";
      case Token.LITERAL_NUMBER_HEXADECIMAL:
        return "LITERAL_NUMBER_HEXADECIMAL";
      case Token.LITERAL_STRING_DOUBLE_QUOTE:
        return "LITERAL_STRING_DOUBLE_QUOTE";
      case Token.NULL:
        return "NULL";
      case Token.NUM_TOKEN_TYPES:
        return "NUM_TOKEN_TYPES";
      case Token.OPERATOR:
        return "OPERATOR";
      case Token.PREPROCESSOR:
        return "PREPROCESSOR";
      case Token.RESERVED_WORD:
        return "RESERVED_WORD";
      case Token.SEPARATOR:
        return "SEPARATOR";
      case Token.VARIABLE:
        return "VARIABLE";
      case Token.WHITESPACE:
        return "WHITESPACE";

      default:
        return "<unknown: " + type + ">";
    }
  }//end getNameForTokenType

  private String getCommentString(String comment)
  {
    String result = comment;
    result = result.replaceFirst("( |\n|\r|\t)*\\/\\/( |\n|\r|\t)*", "");
    result = result.replaceFirst("( |\n|\r|\t)*(\\/\\*(\\*)?)( |\n|\r|\t)*", "");
    result = result.replaceFirst("( |\n|\r|\t)*\\*\\/( |\n|\r|\t)*", "");
    return result;
  }//end getCommentString

  public HashMap<String, State> getStateMap()
  {
    return this.xabslOptionContext.getStateMap();
  }

  public ArrayList<XABSLSymbol> getSymbolsList() {
    return this.xabslContext.getSymbolsList();
  }

  public ArrayList<Transition> getStateTransitionList()
  {
    return this.xabslOptionContext.getStateTransitionList();
  }

  public XABSLOption getCurrentOption() {
    return currentOption;
  }
  
}//end class XParser
