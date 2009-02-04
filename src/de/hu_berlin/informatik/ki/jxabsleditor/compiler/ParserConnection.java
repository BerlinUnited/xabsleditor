/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hu_berlin.informatik.ki.jxabsleditor.compiler;

import de.hu_berlin.informatik.ki.jxabsleditor.Helper;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.fife.ui.rsyntaxtextarea.Parser;
import org.fife.ui.rsyntaxtextarea.ParserNotice;
import xabslc.XabslLexer;
import xabslc.XabslParser;

/**
 *
 * DOESN'T WORK YET!!!
 *
 * @author thomas
 */
public class ParserConnection implements Parser
{

  private XabslLexer lexer;
  private XabslParser parser;
  private ArrayList<ParserNotice> noticeList;
  private CommonTree tree;

  public ParserConnection()
  {
    noticeList = new ArrayList<ParserNotice>(20);
    tree = new CommonTree();
  }

  public void parse()
  {
    parse(new StringReader(""));
  }

  public void parse(Reader r)
  {
    tree = new CommonTree();
    noticeList.clear();

    try
    {
      lexer = new XabslLexer(new ANTLRReaderStream(r));
      CommonTokenStream tokenStream = new CommonTokenStream(lexer);
      parser = new XabslParser(tokenStream)
      {

        @Override
        public void reportError(RecognitionException e)
        {
          if(!(e instanceof SemanticException))
          {
            noticeList.add(exception2Notice(parser, e));
          }
        }
      };

      tree = (CommonTree) parser.xabsl().getTree();



    }
    catch(Error ex)
    {
      ex.printStackTrace();
    }
    catch(RecognitionException ex)
    {
      noticeList.add(exception2Notice(parser, ex));
    }
    catch(Exception ex)
    {
      Helper.handleException(ex);
    }

  }

  public CommonTree getTree()
  {
    return tree;
  }

  private static ParserNotice exception2Notice(XabslParser p, RecognitionException ex)
  {
    return new ParserNotice(p.getErrorMessage(ex, p.getTokenNames()), ex.index,
      ex.token.getText().length());
//    return new ParserNotice(ex.getLocalizedMessage(), ex.index,
//        ex.token.getText().length(), ex.line, ex.charPositionInLine);
  }

  public Iterator getNoticeIterator()
  {
    return noticeList.iterator();
  }
}
