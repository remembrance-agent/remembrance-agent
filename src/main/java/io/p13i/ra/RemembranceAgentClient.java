package io.p13i.ra;

import java.awt.*;
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

    private static final Logger LOGGER = Logger.getLogger(RemembranceAgentClient.class.getName());

    private static JFrame jFrame;
    private static JLabel sKeystrokeBuffer;
    private static JTextArea sTextArea;

    private static KeyboardLoggerBreakingBuffer sBreakingBuffer = new KeyboardLoggerBreakingBuffer(30);
    private static Timer remembranceAgentUpdateTimer = new Timer();
    private static RemembranceAgent remembranceAgent;
    private static JPanel sSuggestionsPanel;

    public static void main(String[] args) {

        jFrame = new JFrame("Remembrance Agent") {{
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(600, 260);
            setResizable(false);
            setAlwaysOnTop(true);
            add(new JPanel() {{
                setLayout(null);
                add(sSuggestionsPanel = new JPanel() {{
                    setBounds(10, 10, 580, 75);
                    setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Suggestions"), BorderFactory.createEmptyBorder(5,5,5,5)));
                    setFont(new Font("monospaced", Font.PLAIN, 12));
                }});
                add(sKeystrokeBuffer = new JLabel() {{
                    setBounds(10, 85, 580, 50);
                    setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Keylogger Buffer"), BorderFactory.createEmptyBorder(5,5,5,5)));
                    setFont(new Font("monospaced", Font.PLAIN, 12));
                }});
                add(new JScrollPane(sTextArea = new JTextArea() {{
                    setFont(new Font("monospaced", Font.PLAIN, 12));
                    setEditable(false);
                }}, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS) {{
                    getViewport().setPreferredSize(new Dimension(580, 50 + 150));
                    setBounds(10, 140, 580, 100);
                    setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Logs"), BorderFactory.createEmptyBorder(5,5,5,5)));
                    new SmartScroller(this, SmartScroller.VERTICAL, SmartScroller.END);
                }});
            }});
            setVisible(true);
        }};

        try {
            InputStream stream = ResourceUtil.getResourceStream(RemembranceAgentClient.class, "logging.properties");
            LogManager.getLogManager().readConfiguration(stream);
            LOGGER.addHandler(new ConsoleHandler());
            LOGGER.addHandler(new Handler() {
                @Override
                public void publish(LogRecord record) {
                    String message = null;
                    if (!isLoggable(record))
                        return;
                    Formatter formatter = getFormatter();
                    if (formatter != null) {
                        message = formatter.format(record);
                    } else {
                        message = record.getMessage();
                    }
                    sTextArea.append(DateUtils.timestamp());
                    sTextArea.append(" | ");
                    sTextArea.append(message);
                    sTextArea.append("\n");
                }

                @Override
                public void flush() {

                }

                @Override
                public void close() throws SecurityException {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // jnativehook produces a lot of logs
        Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);

        String directoryPath = ResourceUtil.getResourcePath(RemembranceAgentClient.class, "sample-documents");
        LOGGER.info("Got directory path: " + directoryPath);
        DocumentDatabase database = new LocalDiskDocumentDatabase(directoryPath);
        LOGGER.info("Using " + database.getName());

        remembranceAgent = new RemembranceAgent(database);
        LOGGER.info("Initialized Remembrance Agent.");

        LOGGER.info("Indexing documents...");
        List<Document> documentsIndexed = remembranceAgent.indexDocuments();
        LOGGER.info(String.format("Indexing complete. Added %d documents:", documentsIndexed.size()));
        for (Document document : documentsIndexed) {
            LOGGER.info(document.toString());
        }

        // Add the key logger
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new RemembranceAgentClient());
        } catch (NativeHookException e) {
            System.exit(1);
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
                List<ScoredDocument> suggestions = remembranceAgent.determineSuggestions(query, context, numSuggestions);

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
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        LOGGER.info("Keystroke: " + keyText);

        Character characterToAdd = null;
        if (e.isActionKey()) {
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
                if (keyText.equals("Space")) {
                    characterToAdd = ' ';
                }
            }
        }

        if (characterToAdd != null) {
            sBreakingBuffer.addCharacter(characterToAdd);
            LOGGER.info(String.format("[Buffer count=%04d:] %s", sBreakingBuffer.getTotalTypedCharactersCount(), sBreakingBuffer.toString()));
            sKeystrokeBuffer.setText("$>" + sBreakingBuffer.toString());
        }

    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        // Nothing
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        // Nothing here
    }
}
