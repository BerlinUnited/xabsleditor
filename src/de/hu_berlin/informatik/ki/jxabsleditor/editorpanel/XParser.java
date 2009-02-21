/*
 * 
 */
package de.hu_berlin.informatik.ki.jxabsleditor.editorpanel;

import java.io.Reader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

  public void parse(Reader reader)
  {
    noticeList.clear();
    stateMap.clear();
    stateTransitionList.clear();

    try
    {
      char[] buffer = new char[1024];
      CharBuffer c;

      int length = reader.read(buffer);
      Segment text = new Segment(buffer, 0, length);

      XTokenMaker tokenizer = new XTokenMaker();
      currentToken = tokenizer.getTokenList(text, Token.NULL, 0);

      try
      {
        if(currentToken != null && currentToken.type != Token.NULL)
        {
          parseOption();
        }
      }
      catch(Exception e)
      {
        System.err.println(e.getMessage());
      }

      /*
      while(currentToken != null && currentToken.type != Token.NULL)
      {
      if(currentToken.type != Token.WHITESPACE && currentToken.type != Token.NULL)
      //System.out.println(currentToken.type + " " + currentToken.getLexeme());
      currentToken = currentToken.getNextToken();
      }//end while
       */

      // construct the graph string
      String graphString = "strict digraph option {\n";
      graphString += "node [fontsize=\"10\"];\n";


      int pos = 1;
      for(State state : stateMap.values())
      {
        graphString += state+"\n";
      //System.out.println(state.name);
      }//end for

      for(Transition transition : this.stateTransitionList)
      {
        if(!this.stateMap.containsKey(transition.to))
        {
          noticeList.add(new ParserNotice("State " + transition.to + " is not defined.",
            transition.offset, transition.to.length()));

        }
        else
        {
          graphString += "\"" + transition.from + "\" -> \"" + transition.to + "\"\n";
        //System.out.println(transition.from +" -> " + transition.to);
        }
      }//end for

      graphString += "}";
      System.out.println(graphString);

    }
    catch(java.io.IOException ioe)
    {
      ioe.printStackTrace();
    }
  }//end parse
  private String currentStateName;
  private String currentComment;

  private void parseOption() throws Exception
  {
    skipSpace();
    isTokenAndEat("option");
    parseIdentifier();
    isTokenAndEat("{");
    while(!isToken("}"))
    {
      parseState();
    }//end while
    isTokenAndEat("}");
  }//end parseOption

  private void parseState() throws Exception
  {
    if(isToken("initial"))
    {
      eat();
    }
    else if(isToken("final"))
    {
      eat();
    }


    isTokenAndEat("state");

    int offset = currentToken.offset;
    currentStateName = parseIdentifier();

    addState(new State(currentStateName, currentComment, offset, this.stateMap.size()));

    isTokenAndEat("{");
    parseDecision();
    parseAction();
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

    System.out.println(currentStateName + " -> " + targetStateName);
  }//end parseGoto

  private void parseExpression() throws Exception
  {
    parseIdentifier();
    if(isToken("="))
    {
      parseAssignment();
    }
    else
    {
      parseFunction();
    }
  }//end parseExpression

  private void parseFunction() throws Exception
  {
    //parseIdentifier();
    isTokenAndEat("(");
    isTokenAndEat(")");
    isTokenAndEat(";");
  }//end parseFunction

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
            parseBlock();
        else if(isToken("goto") || isToken("stay"))
            parseSingleDecision();
        else
            eat();
    }//end while

    isTokenAndEat("}");
  }//end parseBlock


  private void parseAssignment() throws Exception
  {
    //parseIdentifier();
    isTokenAndEat("=");

    if(isToken(Token.IDENTIFIER))
    {
      parseIdentifier();
    }
    else if(isToken(Token.LITERAL_BOOLEAN))
    {
      eat();
    }
    else if(isToken(Token.LITERAL_NUMBER_DECIMAL_INT))
    {
      eat();
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
  }//end parseState

  private void skipSpace()
  {
    while(currentToken != null &&
      (currentToken.type == Token.WHITESPACE ||
      currentToken.type == Token.NULL ||
      currentToken.type == Token.COMMENT ||
      currentToken.type == Token.COMMENT_DOCUMENTATION ||
      currentToken.type == Token.COMMENT_EOL ||
      currentToken.type == Token.COMMENT_MULTILINE))
    {
      if(currentToken.type == Token.COMMENT ||
        currentToken.type == Token.COMMENT_DOCUMENTATION ||
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
      noticeList.add(new ParserNotice("is " + currentToken.type + " but " + type + " expected", currentToken.offset, currentToken.getLexeme().length()));
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

  public Iterator getNoticeIterator()
  {
    return noticeList.iterator();
  }//end getNoticeIterator

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

    public State(String name, String comment, int offset, int number)
    {
      this.name = name;
      this.comment = comment;
      this.offset = offset;
      this.number = number;
    }
    public final String name;
    public final String comment;
    public final int offset;
    
    private final int number;

    @Override
    public String toString()
    {
        return "\"" + name + "\" [shape=\"circle\" pos=\"10," + (number * 70) + "\" URL=\"" + offset + "\"];";
    }//end toString
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
  }//end class Transition
}//end class XParser
