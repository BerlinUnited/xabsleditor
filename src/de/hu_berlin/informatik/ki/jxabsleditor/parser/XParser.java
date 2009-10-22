/*
 * 
 */
package de.hu_berlin.informatik.ki.jxabsleditor.parser;

import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext.XABSLSymbol;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLOptionContext.State;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLOptionContext.Transition;
import edu.uci.ics.jung.graph.Graph;
import java.io.Reader;
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
  
  private XABSLContext xabslContext = null;
  protected XABSLOptionContext xabslOptionContext = null;
  
  private Token currentToken;
  private String currentComment;

  private XABSLAbstractParser parser;

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
            this.parser = new XABSLOptionParser(this);
          }
          else if(isToken("namespace"))
          {
            this.parser = new XABSLNamespaceParser(this);
          }
        }

        this.parser.parse();

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
  

  public HashMap<String, State> getStateMap()
  {
    if(xabslOptionContext != null)
    {
      return this.xabslOptionContext.getStateMap();
    }
    else
    {
      return new HashMap<String, State>();
    }
  }//end getStateMap

  public ArrayList<Transition> getStateTransitionList()
  {
    if(xabslOptionContext != null)
    {
      return this.xabslOptionContext.getStateTransitionList();
    }
    else
    {
      return new ArrayList<Transition>();
    }
  }
  
  public ArrayList<XABSLSymbol> getSymbolsList() {
    return this.xabslContext.getSymbolsList();
  }

  /** Get a graph suited for visualizing */
  public Graph<XabslNode, XabslEdge> getOptionGraph()
  {
    if(xabslOptionContext != null)
    {
      return this.xabslOptionContext.getOptionGraph();
    }
    else
    {
      return null;
    }
  }

  @Override
  public Iterator getNoticeIterator()
  {
    return noticeList.iterator();
  }//end getNoticeIterator

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

  public static String getCommentString(String comment)
  {
    String result = comment;
    result = result.replaceFirst("( |\n|\r|\t)*\\/\\/( |\n|\r|\t)*", "");
    result = result.replaceFirst("( |\n|\r|\t)*(\\/\\*(\\*)?)( |\n|\r|\t)*", "");
    result = result.replaceFirst("( |\n|\r|\t)*\\*\\/( |\n|\r|\t)*", "");
    return result;
  }//end getCommentString


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

  protected String parseIdentifier() throws Exception
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


  protected void eat() throws Exception
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

  protected boolean isToken(int type) throws Exception
  {
    if(currentToken == null)
    {
      return false;
    }

    boolean result = currentToken.type == type;
    return result;
  }//end isToken

  protected boolean isToken(String keyWord) throws Exception
  {
    if(currentToken == null)
    {
      return false;
    }

    boolean result = keyWord.equals(currentToken.getLexeme());
    return result;
  }//end isTokenAndEat

  protected boolean isTokenAndEat(int type) throws Exception
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

  protected boolean isTokenAndEat(String keyWord) throws Exception
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


  public abstract class XABSLAbstractParser
  {
    protected final XParser parent;

    public XABSLAbstractParser(XParser parent)
    {
      this.parent = parent;
    }

    abstract void parse() throws Exception;

    // read comment string only once
    protected String getCurrentComment()
    {
      String comment = this.parent.currentComment;
      this.parent.currentComment = "";
      return comment;
    }//end getCurrentComment

    protected XABSLContext getXABSLContext()
    {
      return this.parent.xabslContext;
    }//end getXABSLContext

    protected Token getCurrentToken()
    {
      return this.parent.currentToken;
    }

    protected void addNotice(ParserNotice notice)
    {
      this.parent.noticeList.add(notice);
    }

    protected boolean isTokenAndEat(String keyWord) throws Exception {
      return this.parent.isTokenAndEat(keyWord);
    }

    protected boolean isTokenAndEat(int type) throws Exception {
      return this.parent.isTokenAndEat(type);
    }

    protected boolean isToken(String keyWord) throws Exception {
      return this.parent.isToken(keyWord);
    }

    protected boolean isToken(int type) throws Exception {
      return this.parent.isToken(type);
    }

    protected void eat() throws Exception {
      this.parent.eat();
    }

    protected String parseIdentifier() throws Exception {
      return this.parent.parseIdentifier();
    }

  }//end class AbstractParser

  
}//end class XParser
