package io.p13i.ra.gui;

import javax.swing.*;

public class BorderedJPanel extends JLabel {
    public void setBorderTitle(String title, int borderAmount) {
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(borderAmount, borderAmount, borderAmount, borderAmount)));
    }
}
