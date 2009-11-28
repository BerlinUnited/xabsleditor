package de.hu_berlin.informatik.ki.jxabsleditor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;



public class FileDrop
{

  private Listener fileDropListener = null;
  private FileDropBorderChangeListener fileDropBorderChangeListener = null;
  private JComponent targetComponent = null;

  public FileDrop(Listener fileDropListener)
  {
    this.fileDropListener = fileDropListener;
  }//end constructor

  
  public void install(JComponent targetComponent)
  {
    this.targetComponent = targetComponent;
    targetComponent.setTransferHandler(new FileDropHandler());
  }//end install
  
  public void setBorderHighlightingEnabled(boolean value)
  {
    if(value &&
       fileDropBorderChangeListener == null &&
       targetComponent != null)
    {
      try{
        fileDropBorderChangeListener = new FileDropBorderChangeListener(targetComponent);
        targetComponent.getDropTarget().addDropTargetListener(fileDropBorderChangeListener);
      }catch(java.util.TooManyListenersException e)
      {
        e.printStackTrace();
      }
      return;
    }//end if

    
    if(!value &&
       fileDropBorderChangeListener != null &&
       targetComponent != null)
    {
      targetComponent.getDropTarget().removeDropTargetListener(fileDropBorderChangeListener);
    }//end if
  }//end setBorderHighlightingEnabled

          
  private class FileDropBorderChangeListener extends DropTargetAdapter
  {
    private javax.swing.border.Border normalBorder = null;

    private java.awt.Color defaultBorderColor = new java.awt.Color( 0f, 0f, 1f, 0.25f );
    private javax.swing.border.Border dragBorder =
      javax.swing.BorderFactory.createMatteBorder( 2, 2, 2, 2, defaultBorderColor );

    private JComponent target = null;
    
    public FileDropBorderChangeListener(JComponent target)
    {
      this.target = target;
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
      target.setBorder(normalBorder);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
      normalBorder = target.getBorder();
      target.setBorder(dragBorder);
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
      target.setBorder(normalBorder);
    }
  }//end class FileDropBorderChangeListener

  
  private class FileDropHandler extends TransferHandler
  {
    @Override
    public boolean canImport(TransferSupport supp)
    {
      /* we'll only support drops (not clipboard paste) */
      if (!supp.isDrop())
      {
        return false;
      }

      /* return true if and only if the drop contains a list of files */
      return supp.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }//end canImport

    @Override
    public boolean importData(TransferSupport supp)
    {
      if (!canImport(supp) || fileDropListener == null) {
          return false;
      }

      /* fetch the Transferable */
      Transferable t = supp.getTransferable();

      try {
        /* fetch the data from the Transferable */
        Object data = t.getTransferData(DataFlavor.javaFileListFlavor);

        /* data of type javaFileListFlavor is a list of files */
        java.util.List<File> fileList = ( java.util.List<File>)data;

        // copy the files to an array
        java.io.File[] fileArray = new java.io.File[fileList.size()];
        // call the listener
        fileDropListener.filesDropped(fileList.toArray(fileArray));
        
      } catch (UnsupportedFlavorException e) {
          return false;
      } catch (IOException e) {
          return false;
      }

      return true;
    }//end importData
  }//end FileDropHandler


  public static interface Listener {

    /**
    * This method is called when files have been successfully dropped.
    *
    * @param files An array of <tt>File</tt>s that were dropped.
    */
    public abstract void filesDropped( java.io.File[] files );


  }// end inner-interface Listener
}// end class FileDrop
