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
package de.hu_berlin.informatik.ki.jxabsleditor.parser;

import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

/**
 *
 * @author Heinrich Mellmann
 */
public class XTokenMaker extends AbstractTokenMaker
{

  private static String KEYWORDS = "" +
    "option|action|decision|common|state|initial|target|agent|float|bool" +
    "|if|else|enum|include|namespace|input|output|internal|goto|stay"; // to be extended
  protected final String operators = "+-*/%!=<>^&|?:";
  protected final String separators = "()[]{}";
  protected final String dotSeparators = ".,;";

  /**
   * Constructor.
   */
  public XTokenMaker()
  {
    super();	// Initializes tokensToHighlight.
  }

  /**
   * Returns the text to place at the beginning and end of a
   * line to "comment" it in a this programming language.
   *
   * @return The start and end strings to add to a line to "comment"
   *         it out.
   */
  @Override
  public String[] getLineCommentStartAndEnd()
  {
    return new String[]
      {
        "/*", "*/"
      };
  }

  /**
   * Checks the token to give it the exact ID it deserves before
   * being passed up to the super method.
   *
   * @param segment <code>Segment</code> to get text from.
   * @param start Start offset in <code>segment</code> of token.
   * @param end End offset in <code>segment</code> of token.
   * @param tokenType The token's type.
   * @param startOffset The offset in the document at which the token occurs.
   */
  @Override
  public void addToken(Segment segment, int start, int end, int tokenType, int startOffset)
  {

    if(tokenType == Token.IDENTIFIER)
    {
      int value = wordsToHighlight.get(segment, start, end);
      if(value != -1)
      {
        tokenType = value;
      }
    }//end if

    super.addToken(segment, start, end, tokenType, startOffset);
  }//end addToken

  /**
   * Returns the words to highlightfor the JavaScript programming language.
   *
   * @return A <code>TokenMap</code> containing the words to highlight for
   *         the JavaScript programming language.
   * @see org.fife.ui.rsyntaxtextarea.AbstractTokenMaker#getWordsToHighlight
   */
  @Override
  public TokenMap getWordsToHighlight()
  {
    String[] keyWords = KEYWORDS.split("\\|");

    TokenMap tokenMap = new TokenMap(keyWords.length + 2);

    // register reserved words
    int reservedWord = Token.RESERVED_WORD;
    for(String keyWord : keyWords)
    {
      tokenMap.put(keyWord, reservedWord);
    }//end for

    //tokenMap.put("goto", Token.FUNCTION);

    // register boolean
    int literalBoolean = Token.LITERAL_BOOLEAN;
    tokenMap.put("false", literalBoolean);
    tokenMap.put("true", literalBoolean);

    return tokenMap;
  }//end getWordsToHighlight

  /**
   * Returns the first token in the linked list of tokens generated
   * from <code>text</code>.  This method must be implemented by
   * subclasses so they can correctly implement syntax highlighting.
   *
   * @param text The text from which to get tokens.
   * @param initialTokenType The token type we should start with.
   * @param startOffset The offset into the document at which
   *                    <code>text</code> starts.
   * @return The first <code>Token</code> in a linked list representing
   *         the syntax highlighted text.
   */
  @Override
  public Token getTokenList(Segment text, int initialTokenType,
    int startOffset)
  {
    resetTokenList();

    char[] array = text.array;
    int offset = text.offset;
    int count = text.count;
    int end = offset + count;

    int currentTokenStart = offset;
    int currentTokenType = initialTokenType;

    boolean stringStarted = false;

    // See, when we find a token, its starting position is always of the
    // form: 'startOffset + (currentTokenStart-offset)'; but since
    // startOffset and offset are constant, tokens' starting positions
    // become: 'newStartOffset+currentTokenStart' for one less subraction
    // operation.
    int newStartOffset = startOffset - offset;

    int i = offset;
    while(i < end)
    {
      char c = array[i];

      switch(currentTokenType)
      {
        case Token.NULL:
          currentTokenStart = i;	// Starting a new token here.
          currentTokenType = decideState(c);
          //i++;
          break;

        case Token.WHITESPACE:
          if(Character.isWhitespace(c))
          {
            currentTokenType = Token.WHITESPACE;
            i++;
          }
          else
          {
            currentTokenType = Token.NULL;
            addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
          }
          break;

        case Token.IDENTIFIER:
          // it is allowed to have identifiers to contain dots,
          // i.e. something like ball.pos.x
          if(Character.isJavaIdentifierPart(c) || c == '.')
          {
            currentTokenType = Token.IDENTIFIER;
            i++;
          }
          else
          {
            //System.out.println(new String(array,currentTokenStart,i-currentTokenStart));
            currentTokenType = Token.NULL;
            int tokenType = wordsToHighlight.get(text, currentTokenStart, i - 1);

            if(tokenType == -1)
            {
              // skip space
              int j = i;
              while(j < (array.length - 1) && Character.isWhitespace(array[j]))
              {
                j++;
              }

              c = array[j];
              if(c == '(')
              {
                addToken(text.array, currentTokenStart, i - 1, Token.FUNCTION, newStartOffset + currentTokenStart, true);
              }
              else
              {
                addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
              }
            }
            else
            {
              addToken(text, currentTokenStart, i - 1, tokenType, newStartOffset + currentTokenStart);
            }
          }
          break;

        case Token.LITERAL_NUMBER_DECIMAL_INT:
          if(Character.isDigit(c))
          {
            currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
            i++;
          }
          else if(c == '.')
          {
            currentTokenType = Token.LITERAL_NUMBER_FLOAT;
            i++;
          }
          else
          {
            currentTokenType = Token.NULL;
            addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
          }
          break;
        case Token.LITERAL_NUMBER_FLOAT:
          if(Character.isDigit(c))
          {
            currentTokenType = Token.LITERAL_NUMBER_FLOAT;
            i++;
          }
          else
          {
            currentTokenType = Token.NULL;
            addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_FLOAT, newStartOffset + currentTokenStart);
          }
          break;
        case Token.OPERATOR:
        {
          boolean operatorChar = is(operators, c);

          // handle the current char
          if(operatorChar)
          {
            currentTokenType = Token.OPERATOR;
            i++;
          }//end if

          // check for comments
          if(!operatorChar || i == end)
          {
            currentTokenType = getOperatorState(array, currentTokenStart, i);
          }//end if

          // handle an operator
          if(!operatorChar && currentTokenType == Token.OPERATOR)
          {
            currentTokenType = Token.NULL;
            addToken(text, currentTokenStart, i - 1, Token.OPERATOR, newStartOffset + currentTokenStart);
          }//end if
        }
          break;

        case Token.COMMENT_MULTILINE:
        case Token.COMMENT_DOCUMENTATION:
          if(i < end - 1 && array[i] == '*' && array[i + 1] == '/')
          {
            addToken(text, currentTokenStart, ++i, currentTokenType, newStartOffset + currentTokenStart);
            currentTokenType = Token.NULL;
          }
          i++;
          break;

        case Token.COMMENT_EOL:
          if(c != '\n' && c != '\r')
          {
            currentTokenType = Token.COMMENT_EOL;
            i++;
          }
          else
          {
            currentTokenType = Token.NULL;
            addToken(text, currentTokenStart, i - 1, Token.COMMENT_EOL, newStartOffset + currentTokenStart);
          }
          break;
        case Token.LITERAL_STRING_DOUBLE_QUOTE:
          if(c == '"')
          {
            if(stringStarted)
            {
              stringStarted = false;
              currentTokenType = Token.NULL;
              addToken(text, currentTokenStart, i, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset + currentTokenStart);
            }
            else
              stringStarted = true;
          }
          i++;
          break;

        case Token.SEPARATOR:
          if(is(separators, c))
          {
            addToken(text, currentTokenStart, i, Token.SEPARATOR, newStartOffset + currentTokenStart);
          }
          else
          {
            addToken(text, currentTokenStart, i, Token.IDENTIFIER, newStartOffset + currentTokenStart);
          }
          currentTokenType = Token.NULL;
          i++;
          break;

        default:
          addToken(text, currentTokenStart, i, Token.ERROR_IDENTIFIER, newStartOffset + currentTokenStart);
          currentTokenType = Token.NULL;
          i++;

        //System.err.println("Invalid currentTokenType: " + currentTokenType + "; c=='" + c + "'");
      }//end switch
    }//end for

    // the whole line is a token

    // Deal with the (possibly there) last token.
    if(currentTokenType != Token.NULL)
    {
      addToken(text, currentTokenStart, end - 1, currentTokenType, newStartOffset + currentTokenStart);
    }
    if(currentTokenType != Token.COMMENT_MULTILINE && currentTokenType != Token.COMMENT_DOCUMENTATION)
    {
      addNullToken();
    }
    // Return the first token in our linked list.
    return firstToken;

  }//end getTokenList

  private int decideState(char c)
  {
    if(Character.isWhitespace(c))
    {
      return Token.WHITESPACE;
    }
    else if(Character.isJavaIdentifierStart(c))
    {
      return Token.IDENTIFIER;
    }
    else if(Character.isDigit(c))
    {
      return Token.LITERAL_NUMBER_DECIMAL_INT;
    }
    else if(is(operators, c))
    {
      return Token.OPERATOR;
    }
    else if(is(separators, c))
    {
      return Token.SEPARATOR;
    }
    else if(is(dotSeparators, c))
    {
      return Token.ERROR_IDENTIFIER;
    }else if(c == '"')
    {
      return Token.LITERAL_STRING_DOUBLE_QUOTE;
    }

    // unknown identifier...
    return Token.ERROR_IDENTIFIER;
  }//end decideState

  private int getOperatorState(char[] array, int start, int end)
  {
    if(end - start == 3) // Operator has the length >= 3
    {
      if(array[start] == '/' &&
        array[start + 1] == '*' &&
        array[start + 2] == '*')
      {
        return Token.COMMENT_DOCUMENTATION;
      }
    }//end if

    if(end - start > 1) // Operator has the length >= 2
    {
      if(array[start] == '/' && array[start + 1] == '*')
      {
        return Token.COMMENT_MULTILINE;
      }
      if(array[start] == '/' && array[start + 1] == '/')
      {
        return Token.COMMENT_EOL;
      }
    }//end if

    return Token.OPERATOR;
  }//end getOperatorState

  private boolean isCommentStart(char[] array, int start, int end)
  {
    String str = new String(array, start, end);
    return str.startsWith("/*");
  }//end isComment

  private boolean isCommentEnd(char[] array, int start, int end)
  {
    String str = new String(array, start, end);
    return str.startsWith("*/");
  }//end isComment

  private boolean is(String matchString, char c)
  {
    return matchString.indexOf(c) > -1;
  }//end is
}//end class XScanner
