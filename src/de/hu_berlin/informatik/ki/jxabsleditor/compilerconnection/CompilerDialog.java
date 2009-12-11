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

import de.hu_berlin.informatik.ki.jxabsleditor.Helper;
import de.hu_berlin.informatik.ki.jxabsleditor.OptionsDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author thomas
 */
public class CompilerDialog extends javax.swing.JDialog
{

  private SwingWorker<CompileResult, Object> worker;
  private File optionFile;
  private File outputFile;
  private CompilationFinishedReceiver receiver;
  private final CompilerDialog finalThis;
  private final Properties configuration;

  /** Creates new form CompilerDialog */
  public CompilerDialog(java.awt.Frame parent, boolean modal,
    File optionFile, File outputFile,
    CompilationFinishedReceiver receiver, Properties configuration)
  {
    super(parent, modal);
    finalThis = this;
    this.configuration = configuration;

    this.optionFile = optionFile;
    this.outputFile = outputFile;
    this.receiver = receiver;

    worker = new CompiliationWorker();

    initComponents();

    pbCompiling.setIndeterminate(true);
    this.validate();

    worker.execute();
  }

  public class CompiliationWorker extends SwingWorker<CompileResult, Object>
  {

    @Override
    protected CompileResult doInBackground() throws Exception
    {
      CompileResult result = new CompileResult();

      File agentsFile = Helper.getAgentFileForOption(optionFile);
      if(agentsFile == null)
      {
        JOptionPane.showMessageDialog(null, "Could not find agents.xabsl",
          "ERROR", JOptionPane.ERROR_MESSAGE);
        result = null;
      }
      else
      {

        try
        {

          Process compilerProcess;

          String compilerCommand = configuration.getProperty(OptionsDialog.XABSL_COMPILER_COMMAND);
          if(compilerCommand != null && !"".equals(compilerCommand))
          {
            String cmd = compilerCommand;
            cmd += " " + agentsFile.getAbsolutePath() + " -i "
              + outputFile.getAbsolutePath();
            compilerProcess = Runtime.getRuntime().exec(cmd);
          }
          else
          {
            String[] cmdarray = autoSearchCommand(agentsFile);
            compilerProcess = Runtime.getRuntime().exec(cmdarray);
          }
          compilerProcess.waitFor();

          if(compilerProcess.exitValue() != 0)
          {
            result.errors = true;
          }

          BufferedReader rIn = new BufferedReader(
            new InputStreamReader(compilerProcess.getInputStream()));

          StringBuilder stdout = new StringBuilder();
          String line = null;
          while((line = rIn.readLine()) != null)
          {
            stdout.append(line);
            stdout.append("\n");
          }

          BufferedReader rErr = new BufferedReader(new InputStreamReader(compilerProcess.getErrorStream()));
          StringBuilder stderr = new StringBuilder();
          line = null;
          while((line = rErr.readLine()) != null)
          {
            if(line.startsWith("ERROR "))
            {
              result.errors = true;
            }
            else if(line.startsWith("WARNING "))
            {
              result.warnings = true;
            }

            // check for syntax errors
            String[] splitted = line.split(":");
            if(splitted.length >= 2 && splitted[1].trim().startsWith("error"))
            {
              result.errors = true;
            }

            stderr.append(line);
            stderr.append("\n");
          }

          System.out.println(stderr);

          result.messages = stderr.toString();

          pbCompiling.setIndeterminate(false);
          pbCompiling.setValue(pbCompiling.getMaximum());

          if(result.errors)
          {
            JOptionPane.showMessageDialog(null,
              "Compilation failed!", "ERROR", JOptionPane.ERROR_MESSAGE);
          }
          else if(result.warnings)
          {
            JOptionPane.showMessageDialog(null,
              "There were warnings during compiliation.", "WARNING", JOptionPane.WARNING_MESSAGE);
          }
          else
          {
            JOptionPane.showMessageDialog(null, "Intermediate code successfully " +
              "compiled and saved.");
          }

        }
        catch(Exception ex)
        {
          Helper.handleException(ex);
        }
      }

      finalThis.setVisible(false);

      if(receiver != null)
      {
        receiver.compilationFinished(result);
      }
      return result;
    }
  }

  private String[] autoSearchCommand(File agentsFile) throws Exception
  {
    // find out path to Extern-directory
    File extern = new File(agentsFile.getAbsolutePath());
    extern = extern.getParentFile();
    while(extern != null && extern.isDirectory() &&
      (!extern.getName().equals("Projects") || !(new File(extern.getParentFile(), "Extern").exists()) || !(new File(extern.getParentFile(), "Documents").exists())))
    {
      extern = extern.getParentFile();
    }

    if(extern != null)
    {
      extern = new File(extern.getParentFile(), "Extern");
    }

    if(extern == null || !extern.isDirectory())
    {
      throw new Exception("Could not find \"Extern\"-directory! Aborting. " +
        "Please specify your custom path in the options.");
    }

    String[] cmdarray = new String[]
    {
      "java",
      "-jar",
      extern.getAbsolutePath() + "/java/jruby-complete-1.2.0.jar",
      extern.getAbsolutePath() + "/ruby/xabsl-compiler/xabsl.rb",
      agentsFile.getAbsolutePath(),
      "-i",
      outputFile.getAbsolutePath()
    };

    return cmdarray;
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    pbCompiling = new javax.swing.JProgressBar();
    lblCompiling = new javax.swing.JLabel();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setLocationByPlatform(true);

    lblCompiling.setText("Compiling...");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(pbCompiling, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
          .addComponent(lblCompiling))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(lblCompiling)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(pbCompiling, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel lblCompiling;
  private javax.swing.JProgressBar pbCompiling;
  // End of variables declaration//GEN-END:variables
}
