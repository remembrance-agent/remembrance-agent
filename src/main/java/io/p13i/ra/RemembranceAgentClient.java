package io.p13i.ra;

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.util.*;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import io.p13i.ra.databases.cache.LocalDiskCacheDocumentDatabase;
import io.p13i.ra.databases.gmail.GmailDocumentDatabase;
import io.p13i.ra.databases.googledrive.GoogleDriveFolderDocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocumentDatabase;
import io.p13i.ra.engine.RemembranceAgentEngine;
import io.p13i.ra.gui.GUI;
import io.p13i.ra.gui.User;
import io.p13i.ra.input.AbstractInputMechanism;
import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;
import io.p13i.ra.models.Query;
import io.p13i.ra.models.ScoredDocument;
import io.p13i.ra.utils.BufferingLogFileWriter;
import io.p13i.ra.utils.DateUtils;
import io.p13i.ra.utils.KeyboardLoggerBreakingBuffer;
import io.p13i.ra.utils.LoggerUtils;
import io.p13i.ra.utils.URIUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.dispatcher.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import static io.p13i.ra.gui.User.Preferences.Pref.GmailMaxEmailsCount;
import static io.p13i.ra.gui.User.Preferences.Pref.GoogleDriveFolderID;
import static io.p13i.ra.gui.User.Preferences.Pref.KeystrokesLogFile;
import static io.p13i.ra.gui.User.Preferences.Pref.LocalDiskDocumentsFolderPath;

public class RemembranceAgentClient implements Runnable, NativeKeyListener, AbstractInputMechanism.OnInput {

    private static final Logger LOGGER = LoggerUtils.getLogger(RemembranceAgentClient.class);

    public static final String APPLICATION_NAME = "Remembrance Agent (v" + System.getenv("VERSION") + ")";

    // Settings
    private static final int KEYBOARD_BUFFER_SIZE = 75;
    private static final int RA_CLIENT_UPDATE_PERIOD_MS = 2500;

    /**
     * "local" variables
     */
    public static final KeyboardLoggerBreakingBuffer sBreakingBuffer = new KeyboardLoggerBreakingBuffer(KEYBOARD_BUFFER_SIZE);
    private static BufferingLogFileWriter sKeyLoggerBufferLogFileWriter;
    private static Timer sRemembranceAgentUpdateTimer = new Timer();

    // RA variables
    private static RemembranceAgentEngine sRemembranceAgentEngine;
    private static String sPriorQuery;
    private static RemembranceAgentClient sInstance = new RemembranceAgentClient();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RemembranceAgentClient.getInstance());
    }

    public static RemembranceAgentClient getInstance() {
        return sInstance;
    }

    @Override
    public void run() {
        sKeyLoggerBufferLogFileWriter = new BufferingLogFileWriter(User.Preferences.getString(KeystrokesLogFile));
        sKeyLoggerBufferLogFileWriter.open();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    System.out.println("Shutting down ...");
                } finally {
                    sKeyLoggerBufferLogFileWriter.flush();
                    sKeyLoggerBufferLogFileWriter.close();
                    sKeyLoggerBufferLogFileWriter = null;
                }
            }
        });

        // Clear previous logging configurations.
        LogManager.getLogManager().reset();

        // Get the logger for "org.jnativehook" and set the level to off.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        // init!
        initializeRemembranceAgent(true);

        // Add the key logger
        try {
            GlobalScreen.setEventDispatcher(new SwingDispatchService());
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(RemembranceAgentClient.getInstance());
        } catch (NativeHookException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("Added native key logger.");

        GUI.sJFrame.setVisible(true);
    }

    public void initializeRemembranceAgent(boolean useCache) {

        // Clear the timer
        if (sRemembranceAgentUpdateTimer != null) {
            sRemembranceAgentUpdateTimer.cancel();
            sRemembranceAgentUpdateTimer.purge();
            sRemembranceAgentUpdateTimer = null;
        }

        LocalDiskCacheDocumentDatabase localDiskCacheDatabase = new LocalDiskCacheDocumentDatabase("/Users/p13i/Documents/RA/~cache");

        if (!useCache) {
            localDiskCacheDatabase
                    .addDocumentsToMemory(new LocalDiskDocumentDatabase(User.Preferences.getString(LocalDiskDocumentsFolderPath)) {{ loadDocuments(); }})
                    .addDocumentsToMemory(new GoogleDriveFolderDocumentDatabase(User.Preferences.getString(GoogleDriveFolderID)) {{ loadDocuments(); }})
                    .addDocumentsToMemory(new GmailDocumentDatabase(User.Preferences.getInt(GmailMaxEmailsCount)) {{ loadDocuments(); }})
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

        GUI.sSuggestionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Suggestions (from " + localDiskCacheDatabase.getName() + ")"),
                BorderFactory.createEmptyBorder(GUI.BORDER_PADDING, GUI.BORDER_PADDING, GUI.BORDER_PADDING, GUI.BORDER_PADDING)));
        GUI.sSuggestionsPanel.invalidate();
        GUI.sSuggestionsPanel.repaint();

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

    private void sendQueryToRemembranceAgent() {
        GUI.sJFrame.setTitle("*** " + APPLICATION_NAME + " ***");

        String query = sBreakingBuffer.toString();

        if (query.equals(sPriorQuery)) {
            LOGGER.info("Skipping prior query: '" + sPriorQuery + "'");
        } else {

            Context context = new Context(null, "p13i", query, DateUtils.now());

            LOGGER.info("Sending query to RA: '" + query + "'");
            List<ScoredDocument> suggestions = sRemembranceAgentEngine.determineSuggestions(new Query(query, context, GUI.RA_NUMBER_SUGGESTIONS));

            GUI.sSuggestionsPanel.removeAll();

            if (suggestions.isEmpty()) {
                LOGGER.info("No suggestions :(");
            } else {
                LOGGER.info(String.format("Got %d suggestion(s).", suggestions.size()));

                int startY = GUI.PADDING_TOP;

                for (ScoredDocument doc : suggestions) {
                    LOGGER.info(" -> (" + doc.getScore() + ") " + doc.toShortString());

                    final int yPos = startY;

                    GUI.sSuggestionsPanel.add(new JLabel() {{
                        setText(Double.toString(doc.getScore()));
                        setBounds(GUI.SUGGESTION_PADDING_LEFT, yPos, GUI.SCORE_WIDTH, GUI.SUGGESTION_HEIGHT);
                        setPreferredSize(new Dimension(GUI.SCORE_WIDTH, GUI.SUGGESTION_HEIGHT));
                    }});

                    GUI.sSuggestionsPanel.add(new JButton() {{
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

            GUI.sSuggestionsPanel.validate();
            GUI.sSuggestionsPanel.repaint();

            sPriorQuery = query;
        }

        GUI.sJFrame.setTitle(APPLICATION_NAME);
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        String keyText = NativeKeyEvent.getKeyText(nativeKeyEvent.getKeyCode());

        sKeyLoggerBufferLogFileWriter.queue(DateUtils.longTimestamp() + " " + keyText + "\n");

        LOGGER.info("Keystroke: " + keyText);

        char characterToAdd = keyText.charAt(0);

        sBreakingBuffer.addCharacter(characterToAdd);
        LOGGER.info(String.format("[Buffer count=%04d:] %s", sBreakingBuffer.getTotalTypedCharactersCount(), sBreakingBuffer.toString()));
        GUI.sKeystrokeBufferLabel.setText(sBreakingBuffer.toString());
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void onInput(Character c) {
        sBreakingBuffer.addCharacter(c);
        GUI.sKeystrokeBufferLabel.setText(sBreakingBuffer.toString());
    }
}
