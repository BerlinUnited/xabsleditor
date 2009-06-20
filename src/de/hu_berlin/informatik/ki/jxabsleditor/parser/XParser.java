/*
 * 
 */
package de.hu_berlin.informatik.ki.jxabsleditor.parser;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
  private HashMap<String, State> stateMap = new HashMap<String, State>();
  
  private ArrayList<Transition> stateTransitionList = new ArrayList<Transition>();
  private Graph<XabslNode, XabslEdge> optionGraph;
  private HashSet<Transition> commonDecisions = new HashSet<Transition>();

  private void convertInternalRepresentationsToGraph()
  {
    commonDecisions.clear();

    optionGraph = new DirectedSparseGraph<XabslNode, XabslEdge>();

    // states
    for(State s : stateMap.values())
    {
      XabslNode n = new XabslNode();
      n.setName(s.name);
      n.setType(XabslNode.Type.State);
      n.setPosInText(s.offset);
      n.setInitialState(s.initial);
      n.setTargetState(s.target);

      optionGraph.addVertex(n);

      for(String o : s.outgoingOptions)
      {
        XabslNode outNode = new XabslNode();
        outNode.setName(o);
        outNode.setType(XabslNode.Type.Option);
        optionGraph.addEdge(new XabslEdge(XabslEdge.Type.Outgoing), n, outNode);
      }
    }

    // transitions
    for(Transition t : stateTransitionList)
    {
      if(t.from == null)
      {
        // common decision, add later when all states are known
        commonDecisions.add(t);
      }
      else
      {
        // not a common decision
        XabslNode nFrom = new XabslNode(t.from, XabslNode.Type.State);
        XabslNode nTo = new XabslNode(t.to, XabslNode.Type.State);
        optionGraph.addEdge(new XabslEdge(XabslEdge.Type.Normal), nFrom, nTo);
      }
    }

    // common decisions
    LinkedList<XabslNode> vertices = new LinkedList<XabslNode>(optionGraph.getVertices());
    for(Transition t : commonDecisions)
    {
      for(XabslNode nFrom : vertices)
      {
        XabslNode nTo = new XabslNode(t.to, XabslNode.Type.State);
        
        optionGraph.addEdge(new XabslEdge(XabslEdge.Type.CommonDecision), nFrom, nTo);
      }
    }

  }

  @Override
  public void parse(Reader reader)
  {
    noticeList.clear();
    stateMap.clear();
    stateTransitionList.clear();
    commonDecisions.clear();

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
            parseOption();
          }
          else if(isToken("namespace"))
          {
            parseNamespace();
          }
        }

        if(currentToken != null && currentToken.type != Token.NULL)
        {
          System.out.println("Unexpected end of File.");
        }
      }
      catch(Exception e)
      {
        System.err.println(e.getMessage());
      }

      convertInternalRepresentationsToGraph();

    }
    catch(java.io.IOException ioe)
    {
      ioe.printStackTrace();
    }
  }//end parse

  // PARSE SYMBOLS
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
    // enum
    if(isToken("enum"))
    {
      boolean isInternal = false;
      eat();
      parseIdentifier();
      if(isToken("internal"))
      {
        isTokenAndEat("internal");
        isInternal = true;
      }
      if(isToken("{"))
      {
        parseEnumDeclaration();
      }
      else
      {
        if(!isInternal)
        {
          if(isToken("output"))
          {
            isTokenAndEat("output");
          }
          else
          {
            isTokenAndEat("input");
          }
        }
        parseIdentifier();
        isTokenAndEat(";");
      }
    }
    else if(isToken("float") || isToken("bool"))
    {
      eat();
      parseSymbolDeclaration();
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
    do
    {
      parseIdentifier();
    }
    while(isToken(","));
    isTokenAndEat("}");
    isTokenAndEat(";");
  }//end parseEnumDeclaration

  private void parseSymbolDeclaration() throws Exception
  {
    if(isToken("output"))
    {
      isTokenAndEat("output");
    }
    else if(isToken("input"))
    {
      isTokenAndEat("input");
    }
    else
    {
      isTokenAndEat("internal");
    }

    parseIdentifier();

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

    isTokenAndEat(";");
  }//end parseSymbolDeclaration
  // PARSE OPTION
  private String currentStateName;
  private Set<String> currentOutgoingOptions;
  private String currentComment;
  private boolean currentStateInitial;
  private boolean currentStateTarget;

  private void parseOption() throws Exception
  {
    isTokenAndEat("option");
    parseIdentifier();
    isTokenAndEat("{");

    while(isToken("float") || isToken("bool"))
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
  }//end parseOption

  private void parseSymbolDefinition() throws Exception
  {
    if(isToken("bool"))
    {
      isTokenAndEat("bool");
    }
    else
    {
      isTokenAndEat("float");
    }

    isTokenAndEat("@");

    // eat name
    eat();

    if(isToken("["))
    {
      eat();
      eat();
      isTokenAndEat("]");
    }

    isTokenAndEat(";");
  }

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

    addState(new State(currentStateName, currentComment, offset, this.stateMap.size(),
      currentStateTarget, currentStateInitial, currentOutgoingOptions));
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
      addTransition(new Transition(currentStateName, currentStateName, currentToken.offset));
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

    addTransition(new Transition(currentStateName, targetStateName, offset));
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
    isTokenAndEat(Token.FUNCTION);
    isTokenAndEat("(");
    parseFunctionParameter();
    isTokenAndEat(")");
    isTokenAndEat(";");
  }//end parseFunction

  private void parseFunctionParameter() throws Exception
  {
    boolean isFirst = true;
    while(!isToken(")"))
    {
      if(!isFirst)
      {
        isTokenAndEat(",");
      }
      isFirst = false;
      parseIdentifier();
      isTokenAndEat("=");
      if(isToken(Token.LITERAL_NUMBER_DECIMAL_INT) || isToken(Token.LITERAL_NUMBER_FLOAT))
      {
        eat();
      }
      else
      {
        parseIdentifier();
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

    while(isToken(Token.IDENTIFIER) || isToken(Token.LITERAL_BOOLEAN) || isToken(Token.LITERAL_NUMBER_DECIMAL_INT) || isToken(Token.LITERAL_NUMBER_FLOAT) || isToken(Token.OPERATOR) || isToken("(") || isToken(")") || isToken("@"))
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
    if(isToken(Token.IDENTIFIER) || isTokenAndEat(Token.ERROR_IDENTIFIER))
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
      if(currentToken.type == Token.COMMENT_DOCUMENTATION ||
        currentToken.type == Token.COMMENT_EOL ||
        currentToken.type == Token.COMMENT_MULTILINE)
      {
        currentComment = currentToken.getLexeme(); // remember last coment
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
      noticeList.add(new ParserNotice("is " + getNameForTokenType(currentToken.type) + " but " + getNameForTokenType(type) + " expected", currentToken.offset, currentToken.getLexeme().length()));
      throw new Exception("Unexpected token type.");
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
      noticeList.add(new ParserNotice(keyWord + " expected", currentToken.offset, currentToken.getLexeme().length()));
      throw new Exception("Unexpected token.");
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
    return optionGraph;
  }

  private void addState(State state) throws Exception
  {
    if(this.stateMap.containsKey(state.name))
    {
      noticeList.add(new ParserNotice("State " + state.name + " already defined.",
        state.offset, state.name.length()));
      throw new Exception("State " + state.name + " already defined.");
    }//end if

    this.stateMap.put(state.name, state);

  }//end addState

  private void addTransition(Transition transition)
  {
    this.stateTransitionList.add(transition);

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
  }

  public HashMap<String, State> getStateMap()
  {
    return stateMap;
  }

  public ArrayList<Transition> getStateTransitionList()
  {
    return stateTransitionList;
  }

  public class State
  {

    public State(String name, String comment, int offset, int number,
      boolean target, boolean initial, Set<String> outgoingOptions)
    {
      this.name = name;
      this.comment = comment;
      this.offset = offset;
      this.number = number;
      this.target = target;
      this.initial = initial;
      this.outgoingOptions = outgoingOptions;
    }
    public final String name;
    public final String comment;
    public final int offset;
    public final boolean target;
    public final boolean initial;
    public final Set<String> outgoingOptions;
    private final int number;

    @Override
    public String toString()
    {
      return "\"" + name + "\" [shape=\"circle\" pos=\"10," + (number * 70) + "\" URL=\"" + offset + "\"];";
    }//end toString

    @Override
    public boolean equals(Object obj)
    {
      if(obj == null)
      {
        return false;
      }
      if(getClass() != obj.getClass())
      {
        return false;
      }
      final State other = (State) obj;
      if((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
      return hash;
    }
  }//end class State

  public class Transition
  {

    public Transition(String from, String to, int offset)
    {
      this.from = from;
      this.to = to;
      this.offset = offset;
    }
    public final String from;
    public final String to;
    public final int offset;

    @Override
    public String toString()
    {
      return "\"" + from + "\" -> \"" + to + "\" [URL=\"" + offset + "\"]";
    }//end toString

    @Override
    public boolean equals(Object obj)
    {
      if(obj == null)
      {
        return false;
      }
      if(getClass() != obj.getClass())
      {
        return false;
      }
      final Transition other = (Transition) obj;
      if((this.from == null) ? (other.from != null) : !this.from.equals(other.from))
      {
        return false;
      }
      if((this.to == null) ? (other.to != null) : !this.to.equals(other.to))
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 3;
      hash = 67 * hash + (this.from != null ? this.from.hashCode() : 0);
      hash = 67 * hash + (this.to != null ? this.to.hashCode() : 0);
      return hash;
    }
  }//end class Transition
}//end class XParser
