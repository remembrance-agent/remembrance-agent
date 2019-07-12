package io.p13i.ra;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private static final int SYS_EXIT_CODE = 1;

    private static JFrame jFrame;
    private static JLabel sKeystrokeBuffer;
    public static JTextArea sTextArea;

    private static KeyboardLoggerBreakingBuffer sBreakingBuffer = new KeyboardLoggerBreakingBuffer(30);
    private static Timer remembranceAgentUpdateTimer = new Timer();
    private static RemembranceAgent sRemembranceAgent;
    private static JPanel sSuggestionsPanel;

    public static void main(String[] args) {

        jFrame = new JFrame("Remembrance Agent") {{
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
                                    switch (fileChooser.showOpenDialog(jFrame)) {
                                        case JFileChooser.APPROVE_OPTION:
                                            String selectedDirectory = fileChooser.getSelectedFile().toPath().toString();
                                            LOGGER.info("Selected directory: " + selectedDirectory);
                                            initializeRemembranceAgent(selectedDirectory);
                                            break;
                                    }
                                }
                            });
                        }});
                    }});
                }});
                add(sSuggestionsPanel = new JPanel() {{
                    setBounds(10, 10, 580, 75);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Suggestions"),
                            BorderFactory.createEmptyBorder(5,5,5,5)));
                    setFont(new Font("monospaced", Font.PLAIN, 12));
                }});
                add(sKeystrokeBuffer = new JLabel() {{
                    setBounds(10, 85, 580, 50);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Keylogger Buffer"),
                            BorderFactory.createEmptyBorder(5,5,5,5)));
                    setFont(new Font("monospaced", Font.PLAIN, 12));
                }});
                add(new JScrollPane(sTextArea = new JTextArea() {{
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
            ;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(SYS_EXIT_CODE);
        }

        // jnativehook produces a lot of logs
        Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);

        String directoryPath = ResourceUtil.getResourcePath(RemembranceAgentClient.class, "sample-documents");
        initializeRemembranceAgent(directoryPath);

        // Add the key logger
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new RemembranceAgentClient());
        } catch (NativeHookException e) {
            e.printStackTrace();
            System.exit(SYS_EXIT_CODE);
        }
        LOGGER.info("Added native key logger.");

        // Start the RA task
        remembranceAgentUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String query = sBreakingBuffer.toString();
                Context context = new Context(null, "p13i", query, DateUtils.now());
                final int numSuggestions = 2;

                LOGGER.info("Sending query to RA: '" + query + "'");
                List<ScoredDocument> suggestions = sRemembranceAgent.determineSuggestions(query, context, numSuggestions);

                if (suggestions.isEmpty()) {
                    LOGGER.info("No suggestions :(");
                } else {
                    LOGGER.info(String.format("Got %d suggestion(s):", suggestions.size()));

                    sSuggestionsPanel.removeAll();
                    for (ScoredDocument doc : suggestions) {
                        sSuggestionsPanel.add(new JLabel(doc.getDocument().getContext().getSubject()));
                        LOGGER.info(" -> " + doc.toString());
                    }
                    sSuggestionsPanel.validate();
                }
            }
        }, 5000, 5000);
        LOGGER.info("Scheduled Remembrance Agent update task.");

        jFrame.setVisible(true);
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
            } else {
            }
        }

        if (characterToAdd != null) {
            sBreakingBuffer.addCharacter(characterToAdd);
            LOGGER.info(String.format("[Buffer count=%04d:] %s", sBreakingBuffer.getTotalTypedCharactersCount(), sBreakingBuffer.toString()));
            sKeystrokeBuffer.setText(sBreakingBuffer.toString());
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

    public void nativeKeyReleased(NativeKeyEvent e) {
        // Nothing
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        // Nothing here
    }
}
