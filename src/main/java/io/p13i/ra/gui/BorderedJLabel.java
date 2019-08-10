package io.p13i.ra.gui;

import javax.swing.*;

import static io.p13i.ra.gui.User.Preferences.Pref.LocalDiskDocumentsFolderPath;

public class BorderedJLabel extends JLabel {
    public void setBorderTitle(String title, int borderAmount) {
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(borderAmount, borderAmount, borderAmount, borderAmount)));
    }
}
