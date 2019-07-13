package io.p13i.ra;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.*;
import java.util.prefs.Preferences;

import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocumentDatabase;
import io.p13i.ra.gui.SmartScroller;
import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;
import io.p13i.ra.models.ScoredDocument;
import io.p13i.ra.utils.*;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import static javax.swing.ScrollPaneConstants.*;

public class RemembranceAgentClient implements NativeKeyListener {

    private static final Logger LOGGER = LoggerUtils.getLogger(RemembranceAgentClient.class);

    private static final int KEYBOARD_BUFFER_SIZE = 60;
    private static final int RA_UPDATE_PERIOD_MS = 5000;  // ms

    private static final String KEYSTROKES_LOG_FILE_PATH_PREFS_NODE_NAME = "KEYSTROKES_LOG_FILE_PATH_PREFS_NODE_NAME";
    private static String sKeystrokesLogFilePath;

    private static final String LOCAL_DISK_DOCUMENTS_FOLDER_PATH_PREFS_NODE_NAME = "LOCAL_DISK_DOCUMENTS_FOLDER_PATH_PREFS_NODE_NAME";
    private static String sLocalDiskDocumentsFolderPath;

    private static JFrame sJFrame;
    private static JLabel sKeystrokeBufferLabel;
    public static JTextArea sLogTextArea;

    private static KeyboardLoggerBreakingBuffer sBreakingBuffer = new KeyboardLoggerBreakingBuffer(KEYBOARD_BUFFER_SIZE);
    private static Timer sRemembranceAgentUpdateTimer = new Timer();
    private static RemembranceAgent sRemembranceAgent;
    private static JPanel sSuggestionsPanel;

    static {
        // Load preferences
        Preferences prefs = Preferences.userNodeForPackage(RemembranceAgentClient.class);

        // Set keystrokes log file location
        sKeystrokesLogFilePath = prefs.get(KEYSTROKES_LOG_FILE_PATH_PREFS_NODE_NAME, null);
        if (sKeystrokesLogFilePath == null) {
            sKeystrokesLogFilePath = System.getProperty("user.home") + File.separator + "keystrokes.log";
        }

        // Set documents folder path preference
        sLocalDiskDocumentsFolderPath = prefs.get(LOCAL_DISK_DOCUMENTS_FOLDER_PATH_PREFS_NODE_NAME, null);
        if (sLocalDiskDocumentsFolderPath == null) {
            sLocalDiskDocumentsFolderPath = ResourceUtil.getResourcePath(RemembranceAgentClient.class, "sample-documents");
        }
    }

    public static void main(String[] args) {

        sJFrame = new JFrame("Remembrance Agent") {{
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(600, 260);
            setResizable(false);
            setAlwaysOnTop(true);
            add(new JPanel() {{
                setLayout(null);
                add(Box.createHorizontalGlue());
                setJMenuBar(new JMenuBar() {{
                    add(new JMenu("Settings") {{
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
                                            initializeRemembranceAgent();
                                            break;
                                    }
                                }
                            });
                        }});
                        add(new JMenuItem("Reinitialize remembrance agent") {{
                            addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    initializeRemembranceAgent();
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
                                            sKeystrokeBufferLabel.setText("Keylogger Buffer (writing to " + sKeystrokesLogFilePath + ")");
                                            break;
                                    }
                                }
                            });
                        }});
                    }});
                    add(new JMenu("About") {{
                        addMenuListener(new MenuListener() {
                            @Override
                            public void menuSelected(MenuEvent e) {
                                JOptionPane.showMessageDialog(sJFrame, "© Pramod Kotipalli, http://remem.p13i.io/");
                            }

                            @Override
                            public void menuDeselected(MenuEvent e) {

                            }

                            @Override
                            public void menuCanceled(MenuEvent e) {

                            }
                        });
                    }});
                }});
                add(sSuggestionsPanel = new JPanel() {{
                    setBounds(10, 10, 580, 75);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Suggestions (from " + sLocalDiskDocumentsFolderPath + ")"),
                            BorderFactory.createEmptyBorder(5,5,5,5)));
                    setFont(new Font("monospaced", Font.PLAIN, 12));
                }});
                add(sKeystrokeBufferLabel = new JLabel() {{
                    setBounds(10, 85, 580, 50);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Keylogger Buffer (writing to " + sKeystrokesLogFilePath + ")"),
                            BorderFactory.createEmptyBorder(5,5,5,5)));
                    setFont(new Font("monospaced", Font.PLAIN, 12));
                }});
                add(new JButton("Clear buffer") {{
                    setBounds(600 - 120 - 10 - 10, 100, 120, 25);
                    setFont(new Font("monospaced", Font.PLAIN, 12));
                    addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            sBreakingBuffer.clear();
                            sKeystrokeBufferLabel.setText("<cleared>");
                        }
                    });
                }});
                add(new JScrollPane(sLogTextArea = new JTextArea() {{
                    setFont(new Font("monospaced", Font.PLAIN, 12));
                    setEditable(false);
                }}, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS) {{
                    getViewport().setPreferredSize(new Dimension(580, 50 + 150));
                    setBounds(10, 140, 580, 100);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Logs"),
                            BorderFactory.createEmptyBorder(5,5,5,5)));
                    new SmartScroller(this, SmartScroller.VERTICAL, SmartScroller.END);
                }});
            }});
        }};

        try {
            InputStream stream = ResourceUtil.getResourceStream(RemembranceAgentClient.class, "logging.properties");
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // jnativehook produces a lot of logs
        Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);

        // init!
        initializeRemembranceAgent();

        // Add the key logger
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new RemembranceAgentClient());
        } catch (NativeHookException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("Added native key logger.");

        // Start the RA task
        sRemembranceAgentUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendQueryToRemembranceAgent();
            }
        }, RA_UPDATE_PERIOD_MS, RA_UPDATE_PERIOD_MS);
        LOGGER.info("Scheduled Remembrance Agent update task.");

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

//            sendQueryToRemembranceAgent();

    }

    private static void initializeRemembranceAgent() {
        LOGGER.info("Got directory path: " + sLocalDiskDocumentsFolderPath);
        DocumentDatabase database = new LocalDiskDocumentDatabase(sLocalDiskDocumentsFolderPath);
        LOGGER.info("Using " + database.getName());

        sRemembranceAgent = new RemembranceAgent(database);
        LOGGER.info("Initialized Remembrance Agent.");

        LOGGER.info("Loading/indexing documents...");
        List<Document> documentsIndexed = sRemembranceAgent.indexDocuments();
        LOGGER.info(String.format("Indexing complete. Added %d documents:", documentsIndexed.size()));
        for (Document document : documentsIndexed) {
            LOGGER.info(document.toString());
        }
    }

    private static void sendQueryToRemembranceAgent() {
        String query = sBreakingBuffer.toString();
        Context context = new Context(null, "p13i", query, DateUtils.now());
        final int numSuggestions = 2;

        LOGGER.info("Sending query to RA: '" + query + "'");
        List<ScoredDocument> suggestions = sRemembranceAgent.determineSuggestions(query, context, numSuggestions);

        sSuggestionsPanel.removeAll();

        if (suggestions.isEmpty()) {
            LOGGER.info("No suggestions :(");
        } else {
            LOGGER.info(String.format("Got %d suggestion(s):", suggestions.size()));

            int startY = 10;

            for (ScoredDocument doc : suggestions) {
                final int yPos = startY;
                sSuggestionsPanel.add(new JButton(doc.getDocument().getContext().getSubject()) {{
                    setBounds(25, yPos, 540, 15);
                    setPreferredSize(new Dimension(540, 15));
                    addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                Desktop.getDesktop().open(new File(doc.getDocument().getUrl()));
                            } catch (IOException ex) {
                                ex.printStackTrace();
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
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        // Nothing
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        // Nothing here
    }
}
