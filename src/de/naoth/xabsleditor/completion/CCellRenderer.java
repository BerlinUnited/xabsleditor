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
package de.naoth.xabsleditor.completion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.Serializable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.VariableCompletion;

/**
 * This class handles the ui/style of the completion window and each completion entry.
 * @author Heinrich Mellmann
 */
public class CCellRenderer extends CompletionCellRenderer {

    private static final Icon VARIABLE_ICON = new ImageIcon(CCellRenderer.class.getResource("/de/naoth/xabsleditor/res/var.png"));
    private static final Icon FUNCTION_ICON = new ImageIcon(CCellRenderer.class.getResource("/de/naoth/xabsleditor/res/function.png"));
    private static final Icon MACRO_ICON = new ImageIcon(CCellRenderer.class.getResource("/de/naoth/xabsleditor/res/macro.png"));
    private static final Icon TEST_ICON = new ImageIcon(CCellRenderer.class.getResource("/de/naoth/xabsleditor/res/test.png"));
    private static final Icon EMPTY_ICON = new EmptyIcon(16);

    private static final Icon DOWNARROW_ICON = new ImageIcon(CCellRenderer.class.getResource("/de/naoth/xabsleditor/res/downarrow.png"));
    private static final Icon LEFTARROW_ICON = new ImageIcon(CCellRenderer.class.getResource("/de/naoth/xabsleditor/res/leftarrow.png"));
    private static final Icon RIGHTARROW_ICON = new ImageIcon(CCellRenderer.class.getResource("/de/naoth/xabsleditor/res/rightarrow.png"));
    
    private static final Icon AGENT_ICON = new ImageIcon(CCellRenderer.class.getResource("/de/naoth/xabsleditor/res/xabsl_agents_file.png"));
    private static final Icon OPTION_ICON = new ImageIcon(CCellRenderer.class.getResource("/de/naoth/xabsleditor/res/xabsl_option_file.png"));
    private static final Icon SYMBOL_ICON = new ImageIcon(CCellRenderer.class.getResource("/de/naoth/xabsleditor/res/xabsl_symbols_file.png"));

    public CCellRenderer() {
        setAlternateBackground(new Color(245, 245, 245));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean hasFocus) {

        super.getListCellRendererComponent(list, value, index, selected, hasFocus);
        
        if (value instanceof XABSLSymbolCompletion) {
            prepareForXABSLSymbolCompletion(list, (XABSLSymbolCompletion) value, index, selected, hasFocus);
        } else if (value instanceof XABSLSymbolSimpleCompletion) {
            prepareForXABSLSymbolSimpleCompletion(list, (XABSLSymbolSimpleCompletion) value, index, selected, hasFocus);
        } else if (value instanceof XABSLOptionCompletion) {
            prepareForXABSLOptionCompletion(list, (XABSLOptionCompletion) value, index, selected, hasFocus);
        } else if (value instanceof XABSLEnumCompletion) {
            prepareForXABSLEnumCompletion(list, (XABSLEnumCompletion) value, index, selected, hasFocus);
        } else if(value instanceof XabslVariableCompletion) {
            prepareForXABSLVariableCompletion(list, (XabslVariableCompletion) value, index, selected, hasFocus);
        } else if(value instanceof XabslCompletion) {
            prepareForXABSLCompletion(list, (XabslCompletion)value, index, selected, hasFocus);
        } else if(value instanceof XabslTemplateCompletion) {
            prepareForXABSLTemplateCompletion(list, (XabslTemplateCompletion)value, index, selected, hasFocus);
        } else if(value instanceof XABSLStateCompletion) {
            setIcon(SYMBOL_ICON);
        }

        return this;
    }//end getListCellRendererComponent

    protected void prepareForXABSLSymbolSimpleCompletion(JList list, XABSLSymbolSimpleCompletion xc, int index, boolean selected, boolean hasFocus) {

        StringBuilder sb = new StringBuilder("<html><b><em>");
        sb.append(xc.getName());
        sb.append("</em></b>");

        if (xc.getType() != null) {
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

        switch (xc.getSecondaryType()) {
            case input:
                setIcon(LEFTARROW_ICON);
                break;
            case output:
                setIcon(RIGHTARROW_ICON);
                break;
            case internal:
                setIcon(DOWNARROW_ICON);
                break;
        }//end switch

    }//end prepareForXABSLSymbolSimpleCompletion

    protected void prepareForXABSLEnumCompletion(JList list,
            XABSLEnumCompletion xe, int index, boolean selected, boolean hasFocus) {

        StringBuilder sb = new StringBuilder("<html><em>");
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
        setIcon(FUNCTION_ICON);
    }//end prepareForXABSLEnumCompletion

    protected void prepareForXABSLSymbolCompletion(JList list,
            XABSLSymbolCompletion xc, int index, boolean selected, boolean hasFocus) {

        StringBuilder sb = new StringBuilder("<html><b><em>");
        sb.append(xc.getName());
        sb.append("</em></b>");

        sb.append(xc.getProvider().getParameterListStart());
        int paramCount = xc.getParamCount();
        for (int i = 0; i < paramCount; i++) {
            FunctionCompletion.Parameter param = xc.getParam(i);
            String type = param.getType();
            String name = param.getName();
            if (type != null) {
                if (!selected) {
                    sb.append("<font color='#aa0077'>");
                }
                sb.append(type);
                if (!selected) {
                    sb.append("</font>");
                }
                if (name != null) {
                    sb.append(' ');
                }
            }
            if (name != null) {
                sb.append(name);
            }
            if (i < paramCount - 1) {
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
        setIcon(VARIABLE_ICON);
    }//end prepareForXABSLSymbolCompletion

    protected void prepareForXABSLOptionCompletion(JList list, XABSLOptionCompletion oc, int index, boolean selected, boolean hasFocus) {

        StringBuilder sb = new StringBuilder("<html><b><em>");
        sb.append(oc.getName());
        sb.append("</em></b>");

        sb.append(oc.getProvider().getParameterListStart());
        int paramCount = oc.getParamCount();
        for (int i = 0; i < paramCount; i++) {
            FunctionCompletion.Parameter param = oc.getParam(i);
            String type = param.getType();
            String name = param.getName();
            if (type != null) {
                if (!selected) {
                    sb.append("<font color='#aa0077'>");
                }
                sb.append(type);
                if (!selected) {
                    sb.append("</font>");
                }
                if (name != null) {
                    sb.append(' ');
                }
            }
            if (name != null) {
                sb.append(name);
            }
            if (i < paramCount - 1) {
                sb.append(oc.getProvider().getParameterListSeparator());
            }
        }
        sb.append(oc.getProvider().getParameterListEnd());

        setText(sb.toString());
        setIcon(MACRO_ICON);
    }//end prepareForXABSLOptionCompletion
    
    protected void prepareForXABSLVariableCompletion(JList list, XabslVariableCompletion vc, int index, boolean selected, boolean hasFocus) {
        setText("<html><b><em>"+vc.getReplacementText()+"</em></b></html>");
        setIcon(TEST_ICON);
    }
    
    protected void prepareForXABSLCompletion(JList list, XabslCompletion vc, int index, boolean selected, boolean hasFocus) {
        setText("<html><b><font color='#0000FF'>"+vc.getReplacementText()+"</font></b></html>");
        setIcon(TEST_ICON);
    }

    protected void prepareForXABSLTemplateCompletion(JList list, XabslTemplateCompletion vc, int index, boolean selected, boolean hasFocus) {
        setText("<html><b><font color='#5096ff'>"+vc.getDefinitionString()+"</font></b></html>");
        setIcon(TEST_ICON);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareForOtherCompletion(JList list,
            Completion c, int index, boolean selected, boolean hasFocus) {
        super.prepareForOtherCompletion(list, c, index, selected, hasFocus);
        setIcon(EMPTY_ICON);
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
        setIcon(VARIABLE_ICON);
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
        setIcon(FUNCTION_ICON);
    }//end prepareForFunctionCompletion

    private static class EmptyIcon implements Icon, Serializable {

        private final int size;

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
