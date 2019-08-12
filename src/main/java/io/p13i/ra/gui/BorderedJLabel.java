package io.p13i.ra.gui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 * Wraps the bordering of a JLabel
 */
public class BorderedJLabel extends JLabel {
    public void setBorderTitle(String title, int borderAmount) {
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(borderAmount, borderAmount, borderAmount, borderAmount)));
    }
}
