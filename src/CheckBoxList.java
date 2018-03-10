import java.awt.Component;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.event.*;

@SuppressWarnings("serial")
public abstract class CheckBoxList extends JList<JCheckBox> {
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    public CheckBoxList() {
        setCellRenderer(new CellRenderer());
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if (index != -1) {
                    JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
                    checkbox.setSelected(!checkbox.isSelected());
                    onSelected(index, checkbox.isSelected());
                    repaint();
                }
            }
        });
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    abstract void onSelected(int index, boolean selected);

    public CheckBoxList(ListModel<JCheckBox> model){
        this();
        setModel(model);
    }

    protected class CellRenderer implements ListCellRenderer<JCheckBox> {
        public Component getListCellRendererComponent(
                JList<? extends JCheckBox> list, JCheckBox value, int index,
                boolean isSelected, boolean cellHasFocus) {

            //Drawing checkbox, change the appearance here
            value.setBackground(isSelected ? getSelectionBackground()
                    : getBackground());
            value.setForeground(isSelected ? getSelectionForeground()
                    : getForeground());
            value.setEnabled(isEnabled());
            value.setFont(getFont());
            value.setFocusPainted(false);
            value.setBorderPainted(true);
            value.setBorder(isSelected ? UIManager
                    .getBorder("List.focusCellHighlightBorder") : noFocusBorder);
            return value;
        }
    }
}