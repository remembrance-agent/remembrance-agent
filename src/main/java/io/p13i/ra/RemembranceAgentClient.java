package io.p13i.ra;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import io.p13i.ra.databases.cache.LocalDiskCacheDocumentDatabase;
import io.p13i.ra.databases.googledrive.GoogleDriveFolderDocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocumentDatabase;
import io.p13i.ra.engine.RemembranceAgentEngine;
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

public class RemembranceAgentClient implements NativeKeyListener {

    /**
     * GUI settings
     */
    private static final int GUI_WIDTH = 600;
    private static final int GUI_HEIGHT = 220;
    private static final int GUI_LINE_HEIGHT = 40;
    private static final int GUI_PADDING_LEFT = 10;
    private static final int GUI_PADDING_TOP = 10;
    private static final int GUI_PADDING_RIGHT = 10;
    private static final int GUI_BORDER_PADDING = 5;
    private static final Font GUI_FONT = new Font("monospaced", Font.PLAIN, 12);

    private static final int KEYBOARD_BUFFER_SIZE = 60;
    private static final int RA_UPDATE_PERIOD_MS = 2500;
    private static final int RA_NUMBER_SUGGESTIONS = 3;

    private static final String KEYSTROKES_LOG_FILE_PATH_PREFS_NODE_NAME = "KEYSTROKES_LOG_FILE_PATH_PREFS_NODE_NAME";
    private static String sKeystrokesLogFilePath = Preferences.userNodeForPackage(RemembranceAgentClient.class).get(
            KEYSTROKES_LOG_FILE_PATH_PREFS_NODE_NAME,
            /* default: */ System.getProperty("user.home") + File.separator + "keystrokes.log");

    private static final String RA_CLIENT_LOG_FILE_PATH_PREFS_NODE_NAME = "RA_CLIENT_LOG_FILE_PATH_PREFS_NODE_NAME";
    public static String sRAClientLogFilePath = Preferences.userNodeForPackage(RemembranceAgentClient.class).get(
            RA_CLIENT_LOG_FILE_PATH_PREFS_NODE_NAME,
            /* default: */ System.getProperty("user.home") + File.separator + "ra-client.log");

    private static final String LOCAL_DISK_DOCUMENTS_FOLDER_PATH_PREFS_NODE_NAME = "LOCAL_DISK_DOCUMENTS_FOLDER_PATH_PREFS_NODE_NAME";
    private static String sLocalDiskDocumentsFolderPath = Preferences.userNodeForPackage(RemembranceAgentClient.class).get(
            LOCAL_DISK_DOCUMENTS_FOLDER_PATH_PREFS_NODE_NAME,
            /* default: */ ResourceUtil.getResourcePath(RemembranceAgentClient.class, "sample-documents"));

    private static final String GOOGLE_DRIVE_FOLDER_ID_PREFS_NODE_NAME = "GOOGLE_DRIVE_FOLDER_ID_PREFS_NODE_NAME";
    private static String sGoogleDriveFolderID = Preferences.userNodeForPackage(RemembranceAgentClient.class).get(
            GOOGLE_DRIVE_FOLDER_ID_PREFS_NODE_NAME,
            /* default: */ null
    );

    /**
     * "local" variables
     */
    private static JFrame sJFrame;
    private static JLabel sKeystrokeBufferLabel;
    private static KeyboardLoggerBreakingBuffer sBreakingBuffer = new KeyboardLoggerBreakingBuffer(KEYBOARD_BUFFER_SIZE);
    private static Timer sRemembranceAgentUpdateTimer = new Timer();
    private static RemembranceAgentEngine sRemembranceAgentEngine;
    private static JPanel sSuggestionsPanel;
    private static String sPriorQuery;

    private static final Logger LOGGER = LoggerUtils.getLogger(RemembranceAgentClient.class);

    public static void main(String[] args) {

        sJFrame = new JFrame("REMEMBRANCE AGENT") {{
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(GUI_WIDTH, GUI_HEIGHT);
            setResizable(false);
            setAlwaysOnTop(true);
            add(new JPanel() {{
                setLayout(null);
                add(Box.createHorizontalGlue());
                setJMenuBar(new JMenuBar() {{
                    add(new JMenu("Remembrance Agent") {{
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
                                    initializeRemembranceAgent(false);
                                    sJFrame.setEnabled(false);
                                    JOptionPane.showMessageDialog(sJFrame, "Reinitialized with new cache!");
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
                                    switch (fileChooser.showOpenDialog(sJFrame)) {
                                        case JFileChooser.APPROVE_OPTION:
                                            Preferences prefs = Preferences.userNodeForPackage(RemembranceAgentClient.class);
                                            prefs.put(LOCAL_DISK_DOCUMENTS_FOLDER_PATH_PREFS_NODE_NAME, sLocalDiskDocumentsFolderPath = fileChooser.getSelectedFile().toPath().toString());
                                            LOGGER.info("Selected directory: " + sLocalDiskDocumentsFolderPath);
                                            break;
                                    }
                                }
                            });
                        }});
                        add(new JMenuItem("Set Google Drive folder ID...") {{
                            addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    String inputId = JOptionPane.showInputDialog("Enter a Google Drive Folder ID (leave blank to cancel):", sGoogleDriveFolderID);
                                    if (inputId != null && inputId.length() > 0) {
                                        Preferences prefs = Preferences.userNodeForPackage(RemembranceAgentClient.class);
                                        prefs.put(GOOGLE_DRIVE_FOLDER_ID_PREFS_NODE_NAME, sGoogleDriveFolderID = inputId);
                                        LOGGER.info("Set Google Drive Folder ID: " + sGoogleDriveFolderID);
                                    }
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
                                    switch (fileChooser.showOpenDialog(sJFrame)) {
                                        case JFileChooser.APPROVE_OPTION:
                                            Preferences prefs = Preferences.userNodeForPackage(RemembranceAgentClient.class);
                                            prefs.put(RA_CLIENT_LOG_FILE_PATH_PREFS_NODE_NAME, sRAClientLogFilePath = fileChooser.getSelectedFile().toPath().toString() + File.separator + "ra-client.log");
                                            LOGGER.info("Selected ra-client.log file: " + sRAClientLogFilePath);
                                            JOptionPane.showMessageDialog(sJFrame, "Selected ra-client.log file: " + sRAClientLogFilePath);
                                            break;
                                    }
                                }
                            });
                        }});
                        add(new JMenuItem("Open ra-client.log log...") {{
                            addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        Desktop.getDesktop().open(new File(sRAClientLogFilePath));
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
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
                                        JOptionPane.showMessageDialog(sJFrame, "Cleared keyboard buffer.");
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
                                    switch (fileChooser.showOpenDialog(sJFrame)) {
                                        case JFileChooser.APPROVE_OPTION:
                                            Preferences prefs = Preferences.userNodeForPackage(RemembranceAgentClient.class);
                                            prefs.put(KEYSTROKES_LOG_FILE_PATH_PREFS_NODE_NAME, sKeystrokesLogFilePath = fileChooser.getSelectedFile().toPath().toString() + File.separator + "keystrokes.log");
                                            LOGGER.info("Selected keystrokes.log file directory: " + sKeystrokesLogFilePath);
                                            sKeystrokeBufferLabel.setBorder(BorderFactory.createCompoundBorder(
                                                    BorderFactory.createTitledBorder("Keylogger Buffer (writing to " + sKeystrokesLogFilePath + ")"),
                                                    BorderFactory.createEmptyBorder(GUI_BORDER_PADDING, GUI_BORDER_PADDING, GUI_BORDER_PADDING, GUI_BORDER_PADDING)));
                                            break;
                                    }
                                }
                            });
                        }});
                        add(new JMenuItem("Open keystrokes.log log...") {{
                            addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        Desktop.getDesktop().open(new File(sKeystrokesLogFilePath));
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
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
                    setBounds(GUI_PADDING_LEFT, GUI_PADDING_TOP, GUI_WIDTH - (GUI_PADDING_LEFT + GUI_PADDING_RIGHT), RA_NUMBER_SUGGESTIONS * GUI_LINE_HEIGHT);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Suggestions (from " + sLocalDiskDocumentsFolderPath + ")"),
                            BorderFactory.createEmptyBorder(GUI_BORDER_PADDING, GUI_BORDER_PADDING, GUI_BORDER_PADDING, GUI_BORDER_PADDING)));
                    setFont(GUI_FONT);
                }});
                add(sKeystrokeBufferLabel = new JLabel() {{
                    setBounds(GUI_PADDING_LEFT, GUI_PADDING_TOP + RA_NUMBER_SUGGESTIONS * GUI_LINE_HEIGHT, GUI_WIDTH - (GUI_PADDING_LEFT + GUI_PADDING_RIGHT), GUI_LINE_HEIGHT);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Keylogger Buffer (writing to " + sKeystrokesLogFilePath + ")"),
                            BorderFactory.createEmptyBorder(GUI_BORDER_PADDING, GUI_BORDER_PADDING, GUI_BORDER_PADDING, GUI_BORDER_PADDING)));
                    setFont(GUI_FONT);
                }});
            }});
        }};

        // jnativehook produces a lot of logs
        Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);

        // init!
        System.out.println(ListUtils.asString(Arrays.asList(args)));

        boolean useCache = false;
        if (args.length > 0 && args[0].equals("--use-cache")) {
            useCache = true;
        }

        initializeRemembranceAgent(useCache);

        // Add the key logger
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new RemembranceAgentClient());
        } catch (NativeHookException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("Added native key logger.");

        sJFrame.setVisible(true);
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());

        FileIO.write(sKeystrokesLogFilePath, DateUtils.longTimestamp() + " " + keyText + "\n");

        LOGGER.info("Keystroke: " + keyText);

        Character characterToAdd = keyText.charAt(0);

        sBreakingBuffer.addCharacter(characterToAdd);
        LOGGER.info(String.format("[Buffer count=%04d:] %s", sBreakingBuffer.getTotalTypedCharactersCount(), sBreakingBuffer.toString()));
        sKeystrokeBufferLabel.setText(sBreakingBuffer.toString());
    }

    private static void initializeRemembranceAgent(boolean useCache) {

        // Clear the timer
        if (sRemembranceAgentUpdateTimer != null) {
            sRemembranceAgentUpdateTimer.cancel();
            sRemembranceAgentUpdateTimer.purge();
            sRemembranceAgentUpdateTimer = null;
        }

        LocalDiskCacheDocumentDatabase database = new LocalDiskCacheDocumentDatabase("/Users/p13i/Documents/RA/~cache");

        if (!useCache) {
            database.saveDocumentsToCache(new LocalDiskDocumentDatabase(sLocalDiskDocumentsFolderPath) {{
                loadDocuments();
            }}.getDocumentsForSavingToCache());
            database.saveDocumentsToCache(new GoogleDriveFolderDocumentDatabase(sGoogleDriveFolderID) {{
                loadDocuments();
            }}.getDocumentsForSavingToCache());
        }

        LOGGER.info("Using " + database.getName());

        sRemembranceAgentEngine = new RemembranceAgentEngine(database);
        LOGGER.info("Initialized Remembrance Agent.");

        LOGGER.info("Loading/indexing documents...");
        List<Document> documentsIndexed = sRemembranceAgentEngine.indexDocuments();
        LOGGER.info(String.format("Indexing complete. Added %d documents:", documentsIndexed.size()));
        for (Document document : documentsIndexed) {
            LOGGER.info(document.toString());
        }

        sSuggestionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Suggestions (from " + database.getName() + ")"),
                BorderFactory.createEmptyBorder(GUI_BORDER_PADDING, GUI_BORDER_PADDING, GUI_BORDER_PADDING, GUI_BORDER_PADDING)));
        sSuggestionsPanel.invalidate();
        sSuggestionsPanel.repaint();

        // Start the RA task
        sRemembranceAgentUpdateTimer = new Timer();
        sRemembranceAgentUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendQueryToRemembranceAgent();
            }
        }, RA_UPDATE_PERIOD_MS, RA_UPDATE_PERIOD_MS);
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
            List<ScoredDocument> suggestions = sRemembranceAgentEngine.determineSuggestions(new Query(query, context, RA_NUMBER_SUGGESTIONS));

            sSuggestionsPanel.removeAll();

            if (suggestions.isEmpty()) {
                LOGGER.info("No suggestions :(");
            } else {
                LOGGER.info(String.format("Got %d suggestion(s):", suggestions.size()));

                int startY = GUI_PADDING_TOP;

                for (ScoredDocument doc : suggestions) {
                    final int yPos = startY;
                    sSuggestionsPanel.add(new JButton() {{
                        setText(doc.toShortString());
                        setBounds(25, yPos, 540, 15);
                        setPreferredSize(new Dimension(540, 15));
                        addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {

                                boolean error = false;
                                try {
                                    Desktop.getDesktop().open(new File(doc.getDocument().getUrl()));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    error = true;
                                }
                                if (error) {
                                    try {
                                        Desktop.getDesktop().browse(URIUtils.get(doc.getDocument().getUrl()));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        error = true;
                                    }
                                }
                            }
                        });
                    }});
                    LOGGER.info(" -> " + doc.toString());
                    startY += 15;
                }
            }

            sSuggestionsPanel.validate();
            sSuggestionsPanel.repaint();

            sPriorQuery = query;
        }

        sJFrame.setTitle("REMEMBRANCE AGENT");
    }

    public void nativeKeyReleased(NativeKeyEvent e) { /***/}

    public void nativeKeyTyped(NativeKeyEvent e) { /***/}
}
