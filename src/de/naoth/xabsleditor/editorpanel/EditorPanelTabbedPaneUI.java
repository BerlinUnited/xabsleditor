package de.naoth.xabsleditor.editorpanel;

import java.awt.Component;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.synth.SynthTabbedPaneUI;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class EditorPanelTabbedPaneUI extends SynthTabbedPaneUI
{
    /** Indicator, if listeners already 'installed'. */
    private boolean installed = false;
    /** Order of how the tabs should be traversed. */
    private final Deque<Component> order = new ArrayDeque<>();
    /** Indicates, if the ctrl key is currently pressed */
    private boolean isCtrlPressed = false;
    /** Listener for monitoring the ctrl key */
    private final KeyEventPostProcessor ctrlKeyListener = new KeyEventPostProcessor() {
        @Override
        public boolean postProcessKeyEvent(KeyEvent e) {
            isCtrlPressed = e.isControlDown();
            if(!e.isControlDown() && tabPane.getSelectedComponent()!= order.peekFirst()) {
                // if released, set current tab to be first
                setCurrentFirst();
            }
            return false;
        }
    };
    /** Listener for monitoring other tab switches (eg. by mouse) */
    private final ChangeListener tabSwitchListener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e){
            if(!isCtrlPressed) {
                setCurrentFirst();
            }
        }
    };
    /** Listener for add/remove new tabs to the ordered queue */
    ContainerListener tabModifyListener = new ContainerListener() {
        @Override
        public void componentAdded(ContainerEvent e) {
            if (e.getChild() instanceof EditorPanelTab) {
                order.add(e.getChild());
            }
        }

        @Override
        public void componentRemoved(ContainerEvent e) {
            if (e.getChild() instanceof EditorPanelTab) {
                order.remove(e.getChild());
            }
        }
    };

    /**
     * Installs custom listeners.
     */
    @Override
    protected void installListeners() {
        super.installListeners();
        
        if(!installed) {
            // monitor the ctrl key
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(ctrlKeyListener);
            // monitor other tab switches (eg. by mouse)
            tabPane.addChangeListener(tabSwitchListener);
            // add/remove new tabs to the ordered queue
            tabPane.addContainerListener(tabModifyListener);
            // add already existing tabs to the queue
            Arrays.stream(tabPane.getComponents()).forEach((t) -> {
                if(t instanceof EditorPanelTab) {
                    order.addFirst(t);
                }
            });
            
            installed = true;
        }
    }

    /**
     * Removes (uninstalls) custom listeners.
     */
    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        if(installed) {
            // remove all added listeners
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor(ctrlKeyListener);
            tabPane.removeChangeListener(tabSwitchListener);
            tabPane.removeContainerListener(tabModifyListener);
        }
    }

    /**
     * Sets the current tab as first.
     */
    private void setCurrentFirst() {
        Component c = tabPane.getSelectedComponent();
        if(c != null) {
            order.remove(c);
            order.addFirst(c);
        }
    }

    /**
     * Iterate through tabs in order of their last visit, starting from 'base'.
     * 
     * @param base, where to start
     * @return the next index in order of their last visit
     */
    @Override
    protected int getNextTabIndex(int base) {
        for (Iterator<Component> it = order.iterator(); it.hasNext();) {
            Component c = it.next();
            int idx = tabPane.indexOfComponent(c);
            if(base == idx) {
                if(it.hasNext()) {
                    return tabPane.indexOfComponent(it.next());
                } else if(order.size() > 0) {
                    return tabPane.indexOfComponent(order.getFirst());
                }
            }
        }
        return 0;
    }

    /**
     * Iterate through tabs in reverse order of their last visit, starting from 'base'.
     * 
     * @param base index, where to start
     * @return the previous index in order of their last visit
     */
    @Override
    protected int getPreviousTabIndex(int base) {
        for (Iterator<Component> it = order.descendingIterator(); it.hasNext();) {
            Component c = it.next();
            int idx = tabPane.indexOfComponent(c);
            if(base == idx) {
                if(it.hasNext()) {
                    return tabPane.indexOfComponent(it.next());
                } else if(order.size() > 0) {
                    return tabPane.indexOfComponent(order.getLast());
                }
            }
        }
        return 0;
    }
}
