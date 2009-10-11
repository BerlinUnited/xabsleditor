/*
 * 
 */
package de.hu_berlin.informatik.ki.jxabsleditor.editorpanel;

import java.awt.Component;
import java.awt.Graphics;
import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.VariableCompletion;


/**
 * 
 * @author Heinrich Mellmann
 */
class CCellRenderer extends CompletionCellRenderer {

	private Icon variableIcon;
	private Icon functionIcon;
	private Icon emptyIcon;

	public CCellRenderer() {
		variableIcon = getIcon("/de/hu_berlin/informatik/ki/jxabsleditor/res/var.png");
		functionIcon = getIcon("/de/hu_berlin/informatik/ki/jxabsleditor/res/function.png");
		emptyIcon = new EmptyIcon(16);
	}


	/**
	 * Returns an icon.
	 *
	 * @param resource The icon to retrieve.  This should either be a file,
	 *        or a resource loadable by the current ClassLoader.
	 * @return The icon.
	 */
	private Icon getIcon(String resource) {
		ClassLoader cl = getClass().getClassLoader();
		URL url = cl.getResource(resource);
		if (url==null) {
			File file = new File(resource);
			try {
				url = file.toURI().toURL();
			} catch (MalformedURLException mue) {
				mue.printStackTrace(); // Never happens
			}
		}
		return url!=null ? new ImageIcon(url) : null;
	}


	/**
	 * {@inheritDoc}
	 */
  @Override
	protected void prepareForOtherCompletion(JList list,
			Completion c, int index, boolean selected, boolean hasFocus) {
		super.prepareForOtherCompletion(list, c, index, selected, hasFocus);
		setIcon(emptyIcon);
	}


	/**
	 * {@inheritDoc}
	 */
  @Override
	protected void prepareForVariableCompletion(JList list,
			VariableCompletion vc, int index, boolean selected,
			boolean hasFocus) {
		super.prepareForVariableCompletion(list, vc, index, selected,
										hasFocus);
		setIcon(variableIcon);
	}


	/**
	 * {@inheritDoc}
	 */
  @Override
	protected void prepareForFunctionCompletion(JList list,
			FunctionCompletion fc, int index, boolean selected,
			boolean hasFocus) {
		super.prepareForFunctionCompletion(list, fc, index, selected,
										hasFocus);
		setIcon(functionIcon);
	}


	private static class EmptyIcon implements Icon, Serializable {

		private int size;

		public EmptyIcon(int size) {
			this.size = size;
		}

    @Override
		public int getIconHeight() {
			return size;
		}

    @Override
		public int getIconWidth() {
			return size;
		}

    @Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
		}
		
	}//end class EmptyIcon
}//end class CCellRenderer