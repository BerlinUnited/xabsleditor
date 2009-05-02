/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.hu_berlin.informatik.ki.jxabsleditor.compilerconnection;

/**
 *
 * @author thomas
 */
public interface CompilationFinishedReceiver
{
  public void compilationFinished(CompileResult result);
}
