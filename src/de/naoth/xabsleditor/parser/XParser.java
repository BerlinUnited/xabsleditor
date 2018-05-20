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
package de.naoth.xabsleditor.parser;

import de.naoth.xabsleditor.parser.XABSLOptionContext.State;
import de.naoth.xabsleditor.parser.XABSLOptionContext.Transition;
import edu.uci.ics.jung.graph.Graph;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;

/**
 *
 * @author Heinrich Mellmann
 */
public class XParser extends AbstractParser
{

  public static final String SYNTAX_STYLE_XABSL				= "text/xabsl";
  
  private DefaultParseResult result;
  
  private XABSLContext xabslContext = null;
  protected XABSLOptionContext xabslOptionContext = null;
  
  private Token currentToken;
  private String currentComment;
  private RSyntaxDocument currentDocument;
  private String currentFileName = null;

  private XABSLAbstractParser parser;

  // HACK: make it beter
  private String fileType = "";

  public XParser(XABSLContext xabslContext)
  {
    this.xabslContext = xabslContext;
    if(xabslContext == null)
    {
      this.xabslContext = new XABSLContext();
    }

    result = new DefaultParseResult(this);
  }

  public XParser()
  {
    this.xabslContext = new XABSLContext();
    result = new DefaultParseResult(this);
  }

  @Override
  public ParseResult parse(RSyntaxDocument doc, String style)
  {
    Element root = doc.getDefaultRootElement();
		int lineCount = root.getElementCount();
    currentDocument = doc;

    if (style==null || SyntaxConstants.SYNTAX_STYLE_NONE.equals(style)){
			result.clearNotices();
			result.setParsedLines(0, lineCount-1);
			return result;
		}

    try
    {
      Segment text = new Segment();
      doc.getText(0, root.getEndOffset(), text);
      parse(text);
      result.setParsedLines(0, lineCount-1);
    }
    catch(Exception e)
    {
      System.err.println(e.getMessage());
    }
    return result;
  }//end parse

  public void parse(Reader reader, String fileName)
  {
    this.currentFileName = fileName;
    parse(reader);
  }//end parse

  public void parse(Reader reader)
  {
    //noticeList.clear();

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
      parse(text);
    }
    catch(java.io.IOException ioe)
    {
      ioe.printStackTrace();
    }
  }
      
  public void parse(Segment text)
  {
    result.clearNotices();
    
    XTokenMaker tokenizer = new XTokenMaker();
    currentToken = tokenizer.getTokenList(text, Token.NULL, 0);

    try
    {
      if(currentToken != null && currentToken.getType() != Token.NULL)
      {
        skipSpace();
        if(isToken("option"))
        {
          this.parser = new XABSLOptionParser(this);
          this.fileType = "option";
        }
        else if(isToken("namespace"))
        {
          this.parser = new XABSLNamespaceParser(this);
          this.fileType = "symbol";
        }
        else if(isToken("include"))
        {
          this.parser = new XABSLAgentParser(this);
          this.fileType = "agent";
        }
      }

      // new files doesn't have any character/token -> can't be parsed!
      if(this.parser != null) {
        this.parser.parse();
      }

      if(currentToken != null && currentToken.getType() != Token.NULL)
      {
        throw new Exception("Unexpected end of file.");
        //System.out.println("Unexpected end of File.");
      }
    }
    catch(Exception e)
    {
      System.err.println(e.getMessage());
    }
  }//end parse

  public String getFileType() {
    return fileType;
  }

  public Map<String, State> getStateMap()
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
  }//end getOptionGraph

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
      case Token.DEFAULT_NUM_TOKEN_TYPES:
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
      (currentToken.getType() == Token.WHITESPACE ||
      currentToken.getType() == Token.NULL ||
      currentToken.getType() == Token.COMMENT_DOCUMENTATION ||
      currentToken.getType() == Token.COMMENT_EOL ||
      currentToken.getType() == Token.COMMENT_MULTILINE))
    {
      // accept only dokumentation comments (i.e. /** ... */)
      if( //currentToken.type == Token.COMMENT_EOL ||
          //currentToken.type == Token.COMMENT_MULTILINE ||
          currentToken.getType() == Token.COMMENT_DOCUMENTATION )
      {
        currentComment = getCommentString(currentToken.getLexeme()); // remember last coment
      }else if(currentToken.getType() == Token.NULL)
      {
        //currentLine++;
      }
      getNextToken();
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
      result.addNotice(new DefaultParserNotice(this, "Identifier expected", getCurrentLine(), currentToken.getOffset(), Math.max(currentToken.getEndOffset(), 2)));
    }

    return null;
  }//end parseIdentifier

  private void getNextToken()
  {
    currentToken = currentToken.getNextToken();
  }//end getNextToken

  protected void eat() throws Exception
  {
    try
    {
      if(currentToken == null)
      {
        throw new Exception("Unexpected end of file.");
      }
      //System.out.println(currentToken.getLexeme() +  " " + currentToken.type);
      getNextToken();
      skipSpace();
    }
    catch(Exception e)
    {
      System.out.println("ERROR: " + currentToken.toString());
    }
  }//end eat

  protected void isEOF() throws Exception
  {
    if(currentToken != null)
    {
      this.result.addNotice(new DefaultParserNotice(this, "End of file expected.", getCurrentLine(), currentToken.getOffset(), currentToken.getLexeme().length()));
      throw new Exception("End of file expected.");
    }//end if
  }//end isEOF

  protected boolean isToken(int type) throws Exception
  {
    if(currentToken == null)
    {
      return false;
    }

    return currentToken.getType() == type;
  }//end isToken

  protected boolean isToken(String keyWord) throws Exception
  {
    if(currentToken == null)
    {
      return false;
    }

    return keyWord.equals(currentToken.getLexeme());
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
      String message = "is " + getNameForTokenType(currentToken.getType()) + " but " + getNameForTokenType(type) + " expected";
      this.result.addNotice(new DefaultParserNotice(this, message, getCurrentLine(), currentToken.getOffset(), currentToken.getLexeme().length()));
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
      this.result.addNotice(new DefaultParserNotice(this, message, getCurrentLine(), currentToken.getOffset(), currentToken.getLexeme().length()));
      throw new Exception("Unexpected token: " + message);
    }//end if

    eat();
    return result;
  }//end isTokenAndEat

  protected int getCurrentLine()
  {
    if(currentDocument == null)
      return 0;

    Element root = currentDocument.getDefaultRootElement();
    return root.getElementIndex(currentToken.getOffset());
  }//end getCurrentLine

  protected String getCurrentFileName()
  {
    return currentFileName;
  }//end getCurrentFileName

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

    protected void addNotice(DefaultParserNotice notice)
    {
      this.parent.result.addNotice(notice);
      //this.parent.noticeList.add(notice);
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

    protected void isEOF() throws Exception {
      parent.isEOF();
    }

    protected String parseIdentifier() throws Exception {
      return this.parent.parseIdentifier();
    }

    protected int getCurrentLine(){
      return this.parent.getCurrentLine();
    }

    protected String getCurrentFileName(){
      return this.parent.getCurrentFileName();
    }
  }//end class AbstractParser

  
}//end class XParser
