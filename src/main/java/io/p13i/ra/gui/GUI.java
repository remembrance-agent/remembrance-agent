package io.p13i.ra.gui;

import io.p13i.ra.RemembranceAgentClient;
import io.p13i.ra.input.InputMechanismManager;
import io.p13i.ra.input.keyboard.KeyboardInputMechanism;
import io.p13i.ra.input.mock.MockSpeechRecognizer;
import io.p13i.ra.input.speech.SpeechInputMechanism;
import io.p13i.ra.models.ScoredDocument;
import io.p13i.ra.utils.IntegerUtils;
import io.p13i.ra.utils.URIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static io.p13i.ra.RemembranceAgentClient.APPLICATION_NAME;
import static io.p13i.ra.gui.User.Preferences.Pref.GmailMaxEmailsCount;
import static io.p13i.ra.gui.User.Preferences.Pref.GoogleDriveFolderID;
import static io.p13i.ra.gui.User.Preferences.Pref.KeystrokesLogFile;
import static io.p13i.ra.gui.User.Preferences.Pref.LocalDiskDocumentsFolderPath;
import static io.p13i.ra.gui.User.Preferences.Pref.RAClientLogFile;

public class GUI {
    public static final int WIDTH = 600;
    public static final int HEIGHT = 220;
    public static final int LINE_HEIGHT = 30;
    public static final int PADDING_LEFT = 10;
    public static final int PADDING_TOP = 10;
    public static final int PADDING_RIGHT = 10;
    public static final int BORDER_PADDING = 5;
    public static final Font FONT = new Font("monospaced", Font.PLAIN, 12);
    public static final int RA_NUMBER_SUGGESTIONS = 4;
    public static final int SUGGESTION_HEIGHT = 15;
    public static final int SUGGESTION_PADDING_LEFT = 25;
    public static final int SCORE_WIDTH = 100;
    public static final int SUGGESTION_BUTTON_WIDTH = 440;
    public static final int SUGGESTION_PADDING = 10;

    // Swing elements
    private static BorderedJLabel sKeystrokeBufferLabel;
    private static BorderedJPanel sSuggestionsPanel;

    private static final JFrame sJFrame = new JFrame(APPLICATION_NAME) {{
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(GUI.WIDTH, GUI.HEIGHT);
        setResizable(false);
        setAlwaysOnTop(true);
        add(new JPanel() {{
            setLayout(null);
            add(Box.createHorizontalGlue());
            setJMenuBar(new JMenuBar() {{
                add(new JMenu("RA Settings") {{
                    add(new JMenuItem("Reinitialize remembrance agent") {{
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                RemembranceAgentClient.getInstance().initializeRAEngine(true);
                                JOptionPane.showMessageDialog(sJFrame, "Reinitialized!");
                            }
                        });
                    }});
                    add(new JSeparator());
                    add(new JMenuItem("Invalidate/reload cache") {{
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                JOptionPane.showMessageDialog(sJFrame, "Reloading with new cache! GUI will freeze");
                                sJFrame.setEnabled(false);
                                RemembranceAgentClient.getInstance().initializeRAEngine(false);
                                JOptionPane.showMessageDialog(sJFrame, "Reinitialized with new cache!");
                                sJFrame.setEnabled(true);
                            }
                        });
                    }});
                    add(new JSeparator());
                    add(new JMenuItem("Select document database directory...") {{
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                JFileChooser fileChooser = new JFileChooser();
                                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                // disable the "All files" option.
                                fileChooser.setAcceptAllFileFilterUsed(false);
                                if (fileChooser.showOpenDialog(sJFrame) == JFileChooser.APPROVE_OPTION) {
                                    User.Preferences.set(LocalDiskDocumentsFolderPath, fileChooser.getSelectedFile().toPath().toString());
                                }
                            }
                        });
                    }});
                    add(new JMenuItem("Set Google Drive folder ID...") {{
                        addActionListener(e -> {
                            String inputId = JOptionPane.showInputDialog("Enter a Google Drive Folder ID (leave blank to cancel):", User.Preferences.getString(GoogleDriveFolderID));
                            if (inputId != null && inputId.length() > 0) {
                                User.Preferences.set(GoogleDriveFolderID, inputId);
                            }
                        });
                    }});
                    add(new JSeparator());
                    add(new JMenuItem("Set max Gmail email index count...") {{
                        addActionListener(e -> {
                            String inputId = JOptionPane.showInputDialog("Enter a count for recent emails to index (leave blank to cancel):", User.Preferences.getInt(GmailMaxEmailsCount));
                            if (inputId != null && inputId.length() > 0 && IntegerUtils.isInt(inputId)) {
                                User.Preferences.set(GmailMaxEmailsCount, inputId);
                            }
                        });
                    }});
                    add(new JSeparator());
                    add(new JMenuItem("Select ra-client.log directory...") {{
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                JFileChooser fileChooser = new JFileChooser();
                                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                // disable the "All files" option.
                                fileChooser.setAcceptAllFileFilterUsed(false);
                                if (fileChooser.showOpenDialog(sJFrame) == JFileChooser.APPROVE_OPTION) {
                                    User.Preferences.set(RAClientLogFile, fileChooser.getSelectedFile().toPath().toString() + File.separator + "ra-client.log");
                                    JOptionPane.showMessageDialog(sJFrame, "Selected ra-client.log file: " + User.Preferences.getString(RAClientLogFile));
                                }
                            }
                        });
                    }});
                    add(new JMenuItem("Open ra-client.log log...") {{
                        addActionListener(e -> {
                            try {
                                Desktop.getDesktop().open(new File(User.Preferences.getString(RAClientLogFile)));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });
                    }});
                }});
                add(new JMenu("Keylogger") {{
                    add(new JMenuItem("Clear buffer") {{
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                RemembranceAgentClient.getInstance().mInputBuffer.clear();
                                sKeystrokeBufferLabel.setText("");
                            }
                        });
                    }});
                    add(new JSeparator());
                    add(new JMenuItem("Select keystrokes.log directory...") {{
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                JFileChooser fileChooser = new JFileChooser();
                                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                // disable the "All files" option.
                                fileChooser.setAcceptAllFileFilterUsed(false);
                                if (fileChooser.showOpenDialog(sJFrame) == JFileChooser.APPROVE_OPTION) {
                                    User.Preferences.set(KeystrokesLogFile, fileChooser.getSelectedFile().toPath().toString() + File.separator + "keystrokes.log");
                                    sKeystrokeBufferLabel.setBorder(BorderFactory.createCompoundBorder(
                                            BorderFactory.createTitledBorder("Keylogger Buffer (writing to " + User.Preferences.getString(KeystrokesLogFile) + ")"),
                                            BorderFactory.createEmptyBorder(GUI.BORDER_PADDING, GUI.BORDER_PADDING, GUI.BORDER_PADDING, GUI.BORDER_PADDING)));
                                }
                            }
                        });
                    }});
                    add(new JMenuItem("Open keystrokes.log log...") {{
                        addActionListener(e -> {
                            try {
                                Desktop.getDesktop().open(new File(User.Preferences.getString(KeystrokesLogFile)));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });
                    }});
                }});
                add(new JMenu("Speech") {{
                    add(new JMenuItem("Recognize") {{
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {

                                SwingUtilities.invokeLater(() -> {
                                    sKeystrokeBufferLabel.setBorderTitle("Speech input...", GUI.BORDER_PADDING);
                                    sKeystrokeBufferLabel.invalidate();
                                    sKeystrokeBufferLabel.repaint();

                                    SpeechInputMechanism speechRecognizer = InputMechanismManager.getInstance()
                                            .setActiveInputMechanism(SpeechInputMechanism.class)
                                            .getInputMechanismInstance(SpeechInputMechanism.class);

                                    sKeystrokeBufferLabel.setBorderTitle(speechRecognizer.getInputMechanismName(), GUI.BORDER_PADDING);
                                    sKeystrokeBufferLabel.invalidate();
                                    sKeystrokeBufferLabel.repaint();

                                    setTitle("Start speaking...");
                                    for (int i = 0; i < 1; i++) {
                                        speechRecognizer.startInput();
                                    }
                                    setTitle("Stop speaking...");

                                    String keyboardInputName = InputMechanismManager.getInstance()
                                            .setActiveInputMechanism(KeyboardInputMechanism.class)
                                            .getInputMechanismInstance(KeyboardInputMechanism.class)
                                            .getInputMechanismName();

                                    sKeystrokeBufferLabel.setBorderTitle(keyboardInputName, GUI.BORDER_PADDING);
                                    sKeystrokeBufferLabel.invalidate();
                                    sKeystrokeBufferLabel.repaint();
                                });

                            }
                        });
                    }});
                }});
                add(new JMenu("About") {{
                    add(new JMenuItem("Show...") {{
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                JOptionPane.showMessageDialog(sJFrame, "© Pramod Kotipalli, http://remem.p13i.io/");

                            }
                        });
                    }});
                }});
            }});
            add(sSuggestionsPanel = new BorderedJPanel() {{
                setBounds(GUI.PADDING_LEFT, GUI.PADDING_TOP, GUI.WIDTH - (GUI.PADDING_LEFT + GUI.PADDING_RIGHT), GUI.RA_NUMBER_SUGGESTIONS * GUI.LINE_HEIGHT);
                setBorderTitle("Suggestions (from " + User.Preferences.getString(LocalDiskDocumentsFolderPath) + ")", GUI.BORDER_PADDING);
                setFont(GUI.FONT);
            }});
            add(sKeystrokeBufferLabel = new BorderedJLabel() {{
                setBounds(GUI.PADDING_LEFT, GUI.PADDING_TOP + GUI.RA_NUMBER_SUGGESTIONS * GUI.LINE_HEIGHT, GUI.WIDTH - (GUI.PADDING_LEFT + GUI.PADDING_RIGHT), GUI.LINE_HEIGHT + GUI.BORDER_PADDING * 2);
                setBorderTitle("", GUI.BORDER_PADDING);
                setFont(GUI.FONT);
            }});
        }});
    }};

    public static void removeScoredDocuments() {
        sSuggestionsPanel.removeAll();
    }

    public static void addScoredDocument(ScoredDocument doc, int i) {
        sSuggestionsPanel.add(new JLabel() {{
            setText(Double.toString(doc.getScore()));
            setBounds(GUI.SUGGESTION_PADDING_LEFT, GUI.PADDING_TOP + GUI.BORDER_PADDING * 2 + i * (GUI.SUGGESTION_HEIGHT + SUGGESTION_PADDING), GUI.SCORE_WIDTH, GUI.SUGGESTION_HEIGHT);
            setPreferredSize(new Dimension(GUI.SCORE_WIDTH, GUI.SUGGESTION_HEIGHT));
        }});
        sSuggestionsPanel.add(new JButton() {{
            setText(doc.toShortString());
            setBounds(GUI.SUGGESTION_PADDING_LEFT + GUI.SCORE_WIDTH, GUI.PADDING_TOP + GUI.BORDER_PADDING * 2 + i * (GUI.SUGGESTION_HEIGHT + SUGGESTION_PADDING), GUI.SUGGESTION_BUTTON_WIDTH, GUI.SUGGESTION_HEIGHT);
            setPreferredSize(new Dimension(GUI.SUGGESTION_BUTTON_WIDTH, GUI.SUGGESTION_HEIGHT));
            setHorizontalAlignment(SwingConstants.LEFT);
            addActionListener(e -> {

                boolean error = false;
                try {
                    Desktop.getDesktop().open(new File(doc.getDocument().getURL()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    error = true;
                }
                if (error) {
                    try {
                        Desktop.getDesktop().browse(URIUtils.get(doc.getDocument().getURL()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            setEnabled(true);
        }});
        sSuggestionsPanel.invalidate();
        sSuggestionsPanel.repaint();
    }

    public static void setKeyStrokeBufferTitle(String title) {
        sKeystrokeBufferLabel.setBorderTitle(title, GUI.BORDER_PADDING);
        sKeystrokeBufferLabel.invalidate();
        sKeystrokeBufferLabel.repaint();
    }

    public static void setTitle(String text) {
        sJFrame.setTitle(text);
    }

    public static void setVisible(boolean visible) {
        sJFrame.setVisible(visible);
    }

    public static void setKeystrokesBufferText(String text) {
        sKeystrokeBufferLabel.setText(text);
        sKeystrokeBufferLabel.invalidate();
        sKeystrokeBufferLabel.repaint();
    }

    public static void setSuggestionsPanelTitle(String s) {
        sSuggestionsPanel.setBorderTitle(s, GUI.BORDER_PADDING);
        sSuggestionsPanel.invalidate();
        sSuggestionsPanel.repaint();
    }
}
