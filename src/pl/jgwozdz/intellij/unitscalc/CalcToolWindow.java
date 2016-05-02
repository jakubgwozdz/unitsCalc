package pl.jgwozdz.intellij.unitscalc;

import javax.swing.*;

/**
 *
 */
public class CalcToolWindow {
    private JTextField inputField;

    private JPanel calcWindowPanel;
    private JComboBox unitsCombo;
    private JTextField outputField;
    private JTable historyTable;
    private JLabel label;

    public JPanel getCalcWindowPanel() {
        return calcWindowPanel;
    }

    public JTextField getInputField() {
        return inputField;
    }

    public JComboBox getUnitsCombo() {
        return unitsCombo;
    }

    public JTextField getOutputField() {
        return outputField;
    }

    public JTable getHistoryTable() {
        return historyTable;
    }

    public JLabel getLabel() {
        return label;
    }
}
