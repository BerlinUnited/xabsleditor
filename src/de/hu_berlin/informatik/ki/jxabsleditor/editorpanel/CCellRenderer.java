/*
 * 
 */
package de.hu_berlin.informatik.ki.jxabsleditor.editorpanel;

import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext.XABSLSymbol.SecondaryType;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.Serializable;
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
  private Icon macroIcon;
	private Icon emptyIcon;
  private Icon testIcon;

  private Icon downarrowIcon;
  private Icon leftarrowIcon;
  private Icon rightarrowIcon;

	public CCellRenderer() {
		variableIcon = getIcon("/de/hu_berlin/informatik/ki/jxabsleditor/res/var.png");
		functionIcon = getIcon("/de/hu_berlin/informatik/ki/jxabsleditor/res/function.png");
    macroIcon = getIcon("/de/hu_berlin/informatik/ki/jxabsleditor/res/macro.png");
    testIcon = getIcon("/de/hu_berlin/informatik/ki/jxabsleditor/res/test.png");

    downarrowIcon = getIcon("/de/hu_berlin/informatik/ki/jxabsleditor/res/downarrow.png");
    leftarrowIcon = getIcon("/de/hu_berlin/informatik/ki/jxabsleditor/res/leftarrow.png");
    rightarrowIcon = getIcon("/de/hu_berlin/informatik/ki/jxabsleditor/res/rightarrow.png");

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
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(
      this.getClass().getResource(resource)));
	}//end getIcon


  /**
	 * {@inheritDoc}
	 */
  @Override
  public Component getListCellRendererComponent(JList list, Object value,
						int index, boolean selected, boolean hasFocus) {

		super.getListCellRendererComponent(list,value,index,selected,hasFocus);

    if (value instanceof XABSLSymbolCompletion) {
			XABSLSymbolCompletion xc = (XABSLSymbolCompletion)value;
			prepareForXABSLSymbolCompletion(list, xc, index, selected, hasFocus);
		}
		else if (value instanceof XABSLSymbolSimpleCompletion) {
			XABSLSymbolSimpleCompletion xc = (XABSLSymbolSimpleCompletion)value;
			prepareForXABSLSymbolSimpleCompletion(list, xc, index, selected, hasFocus);
		}
    else if (value instanceof XABSLOptionCompletion) {
			XABSLOptionCompletion oc = (XABSLOptionCompletion)value;
			prepareForXABSLOptionCompletion(list, oc, index, selected, hasFocus);
		}
    else if (value instanceof XABSLEnumCompletion) {
			XABSLEnumCompletion ec = (XABSLEnumCompletion)value;
			prepareForXABSLEnumCompletion(list, ec, index, selected, hasFocus);
		}

    return this;
  }//end getListCellRendererComponent

  
  protected void prepareForXABSLSymbolSimpleCompletion(JList list,
		XABSLSymbolSimpleCompletion xc, int index, boolean selected, boolean hasFocus) {

		StringBuffer sb = new StringBuffer("<html><b><em>");
		sb.append(xc.getName());
		sb.append("</em></b>");

		if (xc.getType()!=null) {
			sb.append(" : ");
			if (!selected) {
				sb.append("<font color='#a0a0ff'>");
			}
			sb.append(xc.getType());
			if (!selected) {
				sb.append("</font>");
			}
		}//end if

		setText(sb.toString());

    
    switch(xc.getSecondaryType())
    {
      case input: setIcon(rightarrowIcon); break;
      case output: setIcon(leftarrowIcon); break;
      case internal: setIcon(downarrowIcon); break;
    }//end switch

	}//end prepareForXABSLSymbolSimpleCompletion



  protected void prepareForXABSLEnumCompletion(JList list,
		XABSLEnumCompletion xe, int index, boolean selected, boolean hasFocus) {

		StringBuffer sb = new StringBuffer("<html><em>");
		if (!selected) {
			sb.append("<font color='#a0a0a0'>");
		}
    sb.append(xe.getType())
      .append(".")
      .append(xe.getName());
    if (!selected) {
			sb.append("</font>");
		}
		sb.append("</em>");

		setText(sb.toString());
    setIcon(functionIcon);
	}//end prepareForXABSLEnumCompletion
  

  protected void prepareForXABSLSymbolCompletion(JList list,
		XABSLSymbolCompletion xc, int index, boolean selected, boolean hasFocus) {

		StringBuffer sb = new StringBuffer("<html><b><em>");
		sb.append(xc.getName());
		sb.append("</em></b>");

		sb.append(xc.getProvider().getParameterListStart());
		int paramCount = xc.getParamCount();
		for (int i=0; i<paramCount; i++) {
			FunctionCompletion.Parameter param = xc.getParam(i);
			String type = param.getType();
			String name = param.getName();
			if (type!=null) {
				if (!selected) {
					sb.append("<font color='#aa0077'>");
				}
				sb.append(type);
				if (!selected) {
					sb.append("</font>");
				}
				if (name!=null) {
					sb.append(' ');
				}
			}
			if (name!=null) {
				sb.append(name);
			}
			if (i<paramCount-1) {
				sb.append(xc.getProvider().getParameterListSeparator());
			}
		}
		sb.append(xc.getProvider().getParameterListEnd());
		sb.append(" : ");
		if (!selected) {
			sb.append("<font color='#a0a0ff'>");
		}
		sb.append(xc.getType());
		if (!selected) {
			sb.append("</font>");
		}

		setText(sb.toString());
    setIcon(variableIcon);
	}//end prepareForXABSLSymbolCompletion


  protected void prepareForXABSLOptionCompletion(JList list,
		XABSLOptionCompletion oc, int index, boolean selected, boolean hasFocus) {

		StringBuffer sb = new StringBuffer("<html><b><em>");
		sb.append(oc.getName());
		sb.append("</em></b>");

		sb.append(oc.getProvider().getParameterListStart());
		int paramCount = oc.getParamCount();
		for (int i=0; i<paramCount; i++) {
			FunctionCompletion.Parameter param = oc.getParam(i);
			String type = param.getType();
			String name = param.getName();
			if (type!=null) {
				if (!selected) {
					sb.append("<font color='#aa0077'>");
				}
				sb.append(type);
				if (!selected) {
					sb.append("</font>");
				}
				if (name!=null) {
					sb.append(' ');
				}
			}
			if (name!=null) {
				sb.append(name);
			}
			if (i<paramCount-1) {
				sb.append(oc.getProvider().getParameterListSeparator());
			}
		}
		sb.append(oc.getProvider().getParameterListEnd());

		setText(sb.toString());
    setIcon(macroIcon);
	}//end prepareForXABSLOptionCompletion

	/**
	 * {@inheritDoc}
	 */
  @Override
	protected void prepareForOtherCompletion(JList list,
			Completion c, int index, boolean selected, boolean hasFocus) {
		super.prepareForOtherCompletion(list, c, index, selected, hasFocus);
		setIcon(emptyIcon);
	}//end prepareForOtherCompletion


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
	}//end prepareForVariableCompletion


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
	}//end prepareForFunctionCompletion


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