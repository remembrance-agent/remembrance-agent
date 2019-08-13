package io.p13i.ra.gui;

import io.p13i.ra.RemembranceAgentClient;
import io.p13i.ra.input.AbstractInputMechanism;
import io.p13i.ra.input.KeyboardInputMechanism;
import io.p13i.ra.input.SpeechInputMechanism;
import io.p13i.ra.models.ScoredDocument;
import io.p13i.ra.utils.IntegerUtils;
import io.p13i.ra.utils.URIUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import static io.p13i.ra.RemembranceAgentClient.APPLICATION_NAME;
import static io.p13i.ra.gui.User.Preferences.Preference.GmailMaxEmailsCount;
import static io.p13i.ra.gui.User.Preferences.Preference.GoogleDriveFolderID;
import static io.p13i.ra.gui.User.Preferences.Preference.KeystrokesLogFile;
import static io.p13i.ra.gui.User.Preferences.Preference.LocalDiskDocumentsFolderPath;
import static io.p13i.ra.gui.User.Preferences.Preference.RAClientLogFile;


public class GUI {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 220;
    private static final int LINE_HEIGHT = 30;
    private static final int PADDING_LEFT = 10;
    private static final int PADDING_TOP = 10;
    private static final int PADDING_RIGHT = 10;
    private static final int BORDER_PADDING = 5;
    private static final int SUGGESTION_HEIGHT = 15;
    private static final int SUGGESTION_PADDING_LEFT = 25;
    private static final int SCORE_WIDTH = 100;
    private static final int SUGGESTION_BUTTON_WIDTH = 440;
    private static final int SUGGESTION_PADDING = 5;
    private static final int SUGGESTION_PANEL_PADDING_TOP = 15;

    private static final Font FONT = new Font("monospaced", Font.PLAIN, 12);

    // Swing elements
    private BorderedJLabel mKeystrokeBufferLabel;
    private BorderedJPanel mSuggestionsPanel;

    private final JFrame mJFrame = new JFrame(APPLICATION_NAME) {{
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
                                JOptionPane.showMessageDialog(mJFrame, "Reinitialized!");
                            }
                        });
                    }});
                    add(new JSeparator());
                    add(new JMenuItem("Invalidate/reload cache") {{
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                JOptionPane.showMessageDialog(mJFrame, "Reloading with new cache! GUI will freeze");
                                mJFrame.setEnabled(false);
                                RemembranceAgentClient.getInstance().initializeRAEngine(false);
                                JOptionPane.showMessageDialog(mJFrame, "Reinitialized with new cache!");
                                mJFrame.setEnabled(true);
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
                                if (fileChooser.showOpenDialog(mJFrame) == JFileChooser.APPROVE_OPTION) {
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
                                if (fileChooser.showOpenDialog(mJFrame) == JFileChooser.APPROVE_OPTION) {
                                    User.Preferences.set(RAClientLogFile, fileChooser.getSelectedFile().toPath().toString() + File.separator + "ra-client.log");
                                    JOptionPane.showMessageDialog(mJFrame, "Selected ra-client.log file: " + User.Preferences.getString(RAClientLogFile));
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
                                mKeystrokeBufferLabel.setText("");
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
                                if (fileChooser.showOpenDialog(mJFrame) == JFileChooser.APPROVE_OPTION) {
                                    User.Preferences.set(KeystrokesLogFile, fileChooser.getSelectedFile().toPath().toString() + File.separator + "keystrokes.log");
                                    mKeystrokeBufferLabel.setBorder(BorderFactory.createCompoundBorder(
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

                                // All code inside SwingWorker runs on a seperate thread
                                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                                    @Override
                                    public Boolean doInBackground() {

                                        SpeechInputMechanism speechRecognizer = new SpeechInputMechanism(1, 10);

                                        RemembranceAgentClient.getInstance().startInputMechanism(speechRecognizer);

                                        RemembranceAgentClient.getInstance().startInputMechanism(new KeyboardInputMechanism());

                                        return true;
                                    }
                                    @Override
                                    public void done() {

                                    }
                                };

                                // Call the SwingWorker from within the Swing thread
                                worker.execute();
                            }
                        });
                    }});
                }});
                add(new JMenu("About") {{
                    add(new JMenuItem("Show...") {{
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                JOptionPane.showMessageDialog(mJFrame, "© Pramod Kotipalli, http://remem.p13i.io/");

                            }
                        });
                    }});
                }});
            }});
            add(mSuggestionsPanel = new BorderedJPanel() {{
                setBounds(
                        GUI.PADDING_LEFT,
                        GUI.PADDING_TOP,
                        GUI.WIDTH - (GUI.PADDING_LEFT + GUI.PADDING_RIGHT),
                        RemembranceAgentClient.RA_NUMBER_SUGGESTIONS * GUI.LINE_HEIGHT);
                setBorderTitle("Suggestions (from " + User.Preferences.getString(LocalDiskDocumentsFolderPath) + ")", GUI.BORDER_PADDING);
                setFont(GUI.FONT);
            }});
            add(mKeystrokeBufferLabel = new BorderedJLabel() {{
                setBounds(
                        GUI.PADDING_LEFT,
                        GUI.PADDING_TOP + RemembranceAgentClient.RA_NUMBER_SUGGESTIONS * GUI.LINE_HEIGHT,
                        GUI.WIDTH - (GUI.PADDING_LEFT + GUI.PADDING_RIGHT),
                        GUI.LINE_HEIGHT + GUI.BORDER_PADDING * 2);
                setBorderTitle("Initializing input mechanism...", GUI.BORDER_PADDING);
                setFont(GUI.FONT);
            }});
        }});
    }};

    public void removeScoredDocuments() {
        mSuggestionsPanel.removeAll();
        mSuggestionsPanel.invalidate();
        mSuggestionsPanel.repaint();
    }

    public void addScoredDocument(ScoredDocument doc, int i) {
        mSuggestionsPanel.add(new JLabel() {{
            setText(Double.toString(doc.getScore()));
            setBounds(
                    GUI.SUGGESTION_PADDING_LEFT,
                    GUI.PADDING_TOP + GUI.SUGGESTION_PANEL_PADDING_TOP + i * (GUI.SUGGESTION_HEIGHT + SUGGESTION_PADDING),
                    GUI.SCORE_WIDTH,
                    GUI.SUGGESTION_HEIGHT);
            setPreferredSize(new Dimension(GUI.SCORE_WIDTH, GUI.SUGGESTION_HEIGHT));
        }});
        mSuggestionsPanel.add(new JButton() {{
            setText(doc.toShortString());
            setBounds(
                    GUI.SUGGESTION_PADDING_LEFT +
                            GUI.SCORE_WIDTH, GUI.PADDING_TOP + GUI.SUGGESTION_PANEL_PADDING_TOP + i * (GUI.SUGGESTION_HEIGHT + SUGGESTION_PADDING),
                    GUI.SUGGESTION_BUTTON_WIDTH,
                    GUI.SUGGESTION_HEIGHT);
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
        mSuggestionsPanel.invalidate();
        mSuggestionsPanel.repaint();
    }

    public void setTitle(String text) {
        mJFrame.setTitle(text);
    }

    public void setVisible(boolean visible) {
        mJFrame.setVisible(visible);
    }

    public void setKeystrokesBufferText(String text) {
        mKeystrokeBufferLabel.setText(text);
        mKeystrokeBufferLabel.invalidate();
        mKeystrokeBufferLabel.repaint();
    }

    public void setSuggestionsPanelTitle(String s) {
        mSuggestionsPanel.setBorderTitle(s, GUI.BORDER_PADDING);
        mSuggestionsPanel.invalidate();
        mSuggestionsPanel.repaint();
    }

    public void setInputMechanism(AbstractInputMechanism currentInputMechanism) {
        mKeystrokeBufferLabel.setBorderTitle(currentInputMechanism.getInputMechanismName(), GUI.BORDER_PADDING);
    }
}
