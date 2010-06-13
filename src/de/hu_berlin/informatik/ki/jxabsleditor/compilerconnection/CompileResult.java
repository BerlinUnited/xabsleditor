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
package de.hu_berlin.informatik.ki.jxabsleditor.compilerconnection;

import java.util.ArrayList;

/**
 *
 * @author thomas
 */
public class CompileResult
{
  public String messages;
  public boolean warnings;
  public boolean errors;



  
  private ArrayList<CompilerNotice> noticeList;

  public CompileResult()
  {
    noticeList = new ArrayList<CompilerNotice>();
  }


  public void addNotice(CompilerNotice notice)
  {
    this.noticeList.add(notice);
  }

  public CompilerNotice getNotice(int line)
  {
    for(CompilerNotice notice: this.noticeList)
    {
      if(notice.lineOffset == line)
        return notice;
    }//end for
    return null;
  }//end getNotice

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    for(CompilerNotice notice: this.noticeList)
    {
      sb.append(notice).append('\n');
    }
    return sb.toString();
  }//end toString


  public static class CompilerNotice
  {
    public enum Level
    {
      INFO,
      WARNING,
      ERROR
    }

    public final Level level;
    public final String message;
    public final String fileName;
    public final int lineNumber;
    public final int lineOffset;

    public CompilerNotice(int lineOffset, Level level, String message, String fileName, int lineNumber) {
      this.lineOffset = lineOffset;
      this.level = level;
      this.message = message;
      this.fileName = fileName;
      this.lineNumber = lineNumber;
    }

    @Override
    public String toString()
    {
      StringBuilder sb = new StringBuilder();
      sb.append(this.level.name())
        .append(' ')
        .append(this.fileName)
        .append(':')
        .append(this.lineNumber)
        .append('\n')
        .append(this.message);

      return sb.toString();
    }//end toString
  }//end class CompilerNotice
}//end class CompileResult
