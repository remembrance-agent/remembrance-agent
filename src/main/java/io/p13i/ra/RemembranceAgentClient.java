package io.p13i.ra;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.p13i.ra.databases.cache.LocalDiskCacheDocumentDatabase;
import io.p13i.ra.databases.gmail.GmailDocumentDatabase;
import io.p13i.ra.databases.googledrive.GoogleDriveFolderDocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocumentDatabase;
import io.p13i.ra.engine.RemembranceAgentEngine;
import io.p13i.ra.gui.GUI;
import io.p13i.ra.gui.User;
import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;
import io.p13i.ra.models.Query;
import io.p13i.ra.models.ScoredDocument;
import io.p13i.ra.utils.*;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;

import static io.p13i.ra.gui.User.Preferences.Pref.*;

public class RemembranceAgentClient {

    private static final Logger LOGGER = LoggerUtils.getLogger(RemembranceAgentClient.class);

    public static final String APPLICATION_NAME = "Remembrance Agent (v" + System.getenv("VERSION") + ")";

    // Settings
    private static final int KEYBOARD_BUFFER_SIZE = 75;
    private static final int RA_CLIENT_UPDATE_PERIOD_MS = 2500;

    // Swing elements
    private static JFrame sJFrame;
    private static JLabel sKeystrokeBufferLabel;
    private static JPanel sSuggestionsPanel;

    /**
     * "local" variables
     */
    private static final KeyboardLoggerBreakingBuffer sBreakingBuffer = new KeyboardLoggerBreakingBuffer(KEYBOARD_BUFFER_SIZE);
    private static BufferingLogFileWriter sKeyLoggerBufferLogFileWriter;
    private static Timer sRemembranceAgentUpdateTimer = new Timer();

    // RA variables
    private static RemembranceAgentEngine sRemembranceAgentEngine;
    private static String sPriorQuery;

    public static void main(String[] args) {

        sKeyLoggerBufferLogFileWriter = new BufferingLogFileWriter(User.Preferences.getString(KeystrokesLogFile));
        sKeyLoggerBufferLogFileWriter.open();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    System.out.println("Shutting down ...");
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    sKeyLoggerBufferLogFileWriter.flush();
                    sKeyLoggerBufferLogFileWriter.close();
                    sKeyLoggerBufferLogFileWriter = null;
                }
            }
        });

        sJFrame = new JFrame("REMEMBRANCE AGENT") {{
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
                                    initializeRemembranceAgent(true);
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
                                    initializeRemembranceAgent(false);
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
                                        LOGGER.info("Selected directory: " + User.Preferences.getString(LocalDiskDocumentsFolderPath));
                                    }
                                }
                            });
                        }});
                        add(new JMenuItem("Set Google Drive folder ID...") {{
                            addActionListener(e -> {
                                String inputId = JOptionPane.showInputDialog("Enter a Google Drive Folder ID (leave blank to cancel):", User.Preferences.getString(GoogleDriveFolderID));
                                if (inputId != null && inputId.length() > 0) {
                                    User.Preferences.set(GoogleDriveFolderID, inputId);
                                    LOGGER.info("Set Google Drive Folder ID: " + User.Preferences.getString(GoogleDriveFolderID));
                                }
                            });
                        }});
                        add(new JSeparator());
                        add(new JMenuItem("Set max Gmail email index count...") {{
                            addActionListener(e -> {
                                String inputId = JOptionPane.showInputDialog("Enter a count for recent emails to index (leave blank to cancel):", User.Preferences.getInt(GmailMaxEmailsCount));
                                if (inputId != null && inputId.length() > 0 && IntegerUtils.isInt(inputId)) {
                                    User.Preferences.set(GmailMaxEmailsCount, inputId);
                                    LOGGER.info("Set GmailMaxEmailsCount: " + User.Preferences.getInt(GmailMaxEmailsCount));
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
                                        LOGGER.info("Selected ra-client.log file: " + User.Preferences.getString(RAClientLogFile));
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
                                    if (sBreakingBuffer.isEmpty()) {
                                        JOptionPane.showMessageDialog(sJFrame, "Buffer is empty. No clearing required.");
                                    } else {
                                        sBreakingBuffer.clear();
                                        sKeystrokeBufferLabel.setText("");
                                    }
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
                                        User.Preferences.set(User.Preferences.Pref.KeystrokesLogFile, fileChooser.getSelectedFile().toPath().toString() + File.separator + "keystrokes.log");
                                        LOGGER.info("Selected keystrokes.log file directory: " + User.Preferences.getString(KeystrokesLogFile));
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
                add(sSuggestionsPanel = new JPanel() {{
                    setBounds(GUI.PADDING_LEFT, GUI.PADDING_TOP, GUI.WIDTH - (GUI.PADDING_LEFT + GUI.PADDING_RIGHT), GUI.RA_NUMBER_SUGGESTIONS * GUI.LINE_HEIGHT);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Suggestions (from " + User.Preferences.getString(LocalDiskDocumentsFolderPath) + ")"),
                            BorderFactory.createEmptyBorder(GUI.BORDER_PADDING, GUI.BORDER_PADDING, GUI.BORDER_PADDING, GUI.BORDER_PADDING)));
                    setFont(GUI.FONT);
                }});
                add(sKeystrokeBufferLabel = new JLabel() {{
                    setBounds(GUI.PADDING_LEFT, GUI.PADDING_TOP + GUI.RA_NUMBER_SUGGESTIONS * GUI.LINE_HEIGHT, GUI.WIDTH - (GUI.PADDING_LEFT + GUI.PADDING_RIGHT), GUI.LINE_HEIGHT + GUI.BORDER_PADDING * 2);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Keylogger Buffer (writing to " + User.Preferences.getString(KeystrokesLogFile) + ")"),
                            BorderFactory.createEmptyBorder(GUI.BORDER_PADDING, GUI.BORDER_PADDING, GUI.BORDER_PADDING, GUI.BORDER_PADDING)));
                    setFont(GUI.FONT);
                }});
            }});
        }};

        // jnativehook produces a lot of logs
        Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.WARNING);

        // init!
        initializeRemembranceAgent(true);

        // Add the key logger
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {

                @Override
                public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

                }

                @Override
                public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
                    String keyText = NativeKeyEvent.getKeyText(nativeKeyEvent.getKeyCode());

                    RemembranceAgentClient.sKeyLoggerBufferLogFileWriter.queue(DateUtils.longTimestamp() + " " + keyText + "\n");

                    LOGGER.info("Keystroke: " + keyText);

                    char characterToAdd = keyText.charAt(0);

                    sBreakingBuffer.addCharacter(characterToAdd);
                    LOGGER.info(String.format("[Buffer count=%04d:] %s", sBreakingBuffer.getTotalTypedCharactersCount(), sBreakingBuffer.toString()));
                    sKeystrokeBufferLabel.setText(sBreakingBuffer.toString());
                }

                @Override
                public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

                }
            });
        } catch (NativeHookException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("Added native key logger.");

        sJFrame.setVisible(true);
    }

    private static void initializeRemembranceAgent(boolean useCache) {

        // Clear the timer
        if (sRemembranceAgentUpdateTimer != null) {
            sRemembranceAgentUpdateTimer.cancel();
            sRemembranceAgentUpdateTimer.purge();
            sRemembranceAgentUpdateTimer = null;
        }

        LocalDiskCacheDocumentDatabase localDiskCacheDatabase = new LocalDiskCacheDocumentDatabase("/Users/p13i/Documents/RA/~cache");

        if (!useCache) {
            localDiskCacheDatabase
                    .addDocumentsToMemory(new LocalDiskDocumentDatabase(User.Preferences.getString(LocalDiskDocumentsFolderPath)) {{ loadDocuments(); }}.getDocumentsForSavingToCache())
                    .addDocumentsToMemory(new GoogleDriveFolderDocumentDatabase(User.Preferences.getString(GoogleDriveFolderID)) {{ loadDocuments(); }}.getDocumentsForSavingToCache())
                    .addDocumentsToMemory(new GmailDocumentDatabase(User.Preferences.getInt(GmailMaxEmailsCount)) {{ loadDocuments(); }}.getDocumentsForSavingToCache())
                    .saveDocumentsInMemoryToDisk();
        }

        LOGGER.info("Using " + localDiskCacheDatabase.getName());

        sRemembranceAgentEngine = new RemembranceAgentEngine(localDiskCacheDatabase);
        LOGGER.info("Initialized Remembrance Agent.");

        LOGGER.info("Loading/indexing documents...");
        List<Document> documentsIndexed = sRemembranceAgentEngine.indexDocuments();
        LOGGER.info(String.format("Indexing complete. Added %d documents:", documentsIndexed.size()));
        for (Document document : documentsIndexed) {
            LOGGER.info(document.toString());
        }

        sSuggestionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Suggestions (from " + localDiskCacheDatabase.getName() + ")"),
                BorderFactory.createEmptyBorder(GUI.BORDER_PADDING, GUI.BORDER_PADDING, GUI.BORDER_PADDING, GUI.BORDER_PADDING)));
        sSuggestionsPanel.invalidate();
        sSuggestionsPanel.repaint();

        // Start the RA task
        sRemembranceAgentUpdateTimer = new Timer();
        sRemembranceAgentUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendQueryToRemembranceAgent();
            }
        }, RA_CLIENT_UPDATE_PERIOD_MS, RA_CLIENT_UPDATE_PERIOD_MS);
        LOGGER.info("Scheduled Remembrance Agent update task.");
    }

    private static void sendQueryToRemembranceAgent() {
        sJFrame.setTitle("* REMEMBRANCE AGENT *");

        String query = sBreakingBuffer.toString();

        if (query.equals(sPriorQuery)) {
            LOGGER.info("Skipping prior query: '" + sPriorQuery + "'");
        } else {

            Context context = new Context(null, "p13i", query, DateUtils.now());

            LOGGER.info("Sending query to RA: '" + query + "'");
            List<ScoredDocument> suggestions = sRemembranceAgentEngine.determineSuggestions(new Query(query, context, GUI.RA_NUMBER_SUGGESTIONS));

            sSuggestionsPanel.removeAll();

            if (suggestions.isEmpty()) {
                LOGGER.info("No suggestions :(");
            } else {
                LOGGER.info(String.format("Got %d suggestion(s):", suggestions.size()));

                int startY = GUI.PADDING_TOP;

                for (ScoredDocument doc : suggestions) {
                    final int yPos = startY;

                    sSuggestionsPanel.add(new JLabel() {{
                        setText(Double.toString(doc.getScore()));
                        setBounds(GUI.SUGGESTION_PADDING_LEFT, yPos, GUI.SCORE_WIDTH, GUI.SUGGESTION_HEIGHT);
                        setPreferredSize(new Dimension(GUI.SCORE_WIDTH, GUI.SUGGESTION_HEIGHT));
                    }});

                    sSuggestionsPanel.add(new JButton() {{
                        setText(doc.toShortString());
                        setBounds(GUI.SUGGESTION_PADDING_LEFT + GUI.SCORE_WIDTH, yPos, GUI.SUGGESTION_BUTTON_WIDTH, GUI.SUGGESTION_HEIGHT);
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

                    startY += GUI.SUGGESTION_HEIGHT;
                }
            }

            sSuggestionsPanel.validate();
            sSuggestionsPanel.repaint();

            sPriorQuery = query;
        }

        sJFrame.setTitle("REMEMBRANCE AGENT");
    }
}
