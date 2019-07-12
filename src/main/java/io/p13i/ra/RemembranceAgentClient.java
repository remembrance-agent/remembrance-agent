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

import static javax.swing.ScrollPaneConstants.*;

public class RemembranceAgentClient implements NativeKeyListener {

    private static final Logger LOGGER = LoggerUtils.getLogger(RemembranceAgentClient.class);

    private static final int KEYBOARD_BUFFER_SIZE = 60;
    private static final int RA_UPDATE_PERIOD_MS = 5000;  // ms

    private static JFrame sJFrame;
    private static JLabel sKeystrokeBufferLabel;
    public static JTextArea sLogTextArea;

    private static KeyboardLoggerBreakingBuffer sBreakingBuffer = new KeyboardLoggerBreakingBuffer(KEYBOARD_BUFFER_SIZE);
    private static Timer sRemembranceAgentUpdateTimer = new Timer();
    private static String sSelectedDirectory;
    private static RemembranceAgent sRemembranceAgent;
    private static JPanel sSuggestionsPanel;

    public static void main(String[] args) {

        sJFrame = new JFrame("Remembrance Agent") {{
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(600, 260);
            setResizable(false);
            setAlwaysOnTop(true);
            add(new JPanel() {{
                setLayout(null);
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
                                            sSelectedDirectory = fileChooser.getSelectedFile().toPath().toString();
                                            LOGGER.info("Selected directory: " + sSelectedDirectory);
                                            initializeRemembranceAgent(sSelectedDirectory);
                                            break;
                                    }
                                }
                            });
                        }});
                        add(new JMenuItem("Reinitialize") {{
                            addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    initializeRemembranceAgent(sSelectedDirectory);
                                }
                            });
                        }});
                    }});
                    add(new JMenu("© Pramod Kotipalli"));
                }});
                add(sSuggestionsPanel = new JPanel() {{
                    setBounds(10, 10, 580, 75);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Suggestions"),
                            BorderFactory.createEmptyBorder(5,5,5,5)));
                    setFont(new Font("monospaced", Font.PLAIN, 12));
                }});
                add(sKeystrokeBufferLabel = new JLabel() {{
                    setBounds(10, 85, 580, 50);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Keylogger Buffer"),
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

        sSelectedDirectory = ResourceUtil.getResourcePath(RemembranceAgentClient.class, "sample-documents");
        initializeRemembranceAgent(sSelectedDirectory);

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
        LOGGER.info("Keystroke: " + keyText);

        Character characterToAdd = null;
        if (keyText.equals("Space") || keyText.equals("␣")) {
            characterToAdd = ' ';
        } else if (e.isActionKey()) {
            if (e.getKeyCode() != NativeKeyEvent.VC_SHIFT) {
                characterToAdd = ' ';
            }
        } else {
            if (keyText.length() == 1) {
                char charToAdd = keyText.charAt(0);
                if (CharacterUtils.isAlphanumeric(charToAdd)) {
                    characterToAdd = charToAdd;
                }
            }
        }

        if (characterToAdd != null) {
            sBreakingBuffer.addCharacter(characterToAdd);
            LOGGER.info(String.format("[Buffer count=%04d:] %s", sBreakingBuffer.getTotalTypedCharactersCount(), sBreakingBuffer.toString()));
            sKeystrokeBufferLabel.setText(sBreakingBuffer.toString());

            sendQueryToRemembranceAgent();
        }

    }

    private static void initializeRemembranceAgent(String folderPath) {
        LOGGER.info("Got directory path: " + folderPath);
        DocumentDatabase database = new LocalDiskDocumentDatabase(folderPath);
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
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        // Nothing
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        // Nothing here
    }
}
