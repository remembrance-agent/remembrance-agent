package io.p13i.ra;

import java.util.*;
import java.util.Timer;
import java.util.logging.Logger;

import io.p13i.ra.databases.cache.LocalDiskCacheDocumentDatabase;
import io.p13i.ra.databases.gmail.GmailDocumentDatabase;
import io.p13i.ra.databases.googledrive.GoogleDriveFolderDocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocumentDatabase;
import io.p13i.ra.engine.IRemembranceAgentEngine;
import io.p13i.ra.engine.RemembranceAgentEngine;
import io.p13i.ra.gui.GUI;
import io.p13i.ra.gui.User;
import io.p13i.ra.input.AbstractInputMechanism;
import io.p13i.ra.input.InputMechanismManager;
import io.p13i.ra.input.keyboard.KeyboardInputMechanism;
import io.p13i.ra.input.speech.SpeechInputMechanism;
import io.p13i.ra.models.AbstractDocument;
import io.p13i.ra.models.Context;
import io.p13i.ra.models.Query;
import io.p13i.ra.models.ScoredDocument;
import io.p13i.ra.utils.*;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;

import static io.p13i.ra.gui.User.Preferences.Pref.GmailMaxEmailsCount;
import static io.p13i.ra.gui.User.Preferences.Pref.GoogleDriveFolderID;
import static io.p13i.ra.gui.User.Preferences.Pref.KeystrokesLogFile;
import static io.p13i.ra.gui.User.Preferences.Pref.LocalDiskDocumentsFolderPath;

/**
 * Implementation of RA GUI
 *
 * @author Pramod Kotipalli
 */
public class RemembranceAgentClient implements Runnable, AbstractInputMechanism.OnInput {

    private static final Logger LOGGER = LoggerUtils.getLogger(RemembranceAgentClient.class);

    public static final String APPLICATION_NAME = "Remembrance Agent (v" + System.getenv("VERSION") + ")";

    /**
     * Maintain an instance for the RA for the GUI's updates
     */
    private static RemembranceAgentClient sInstance = new RemembranceAgentClient();

    /**
     * @return the RA instance
     */
    public static RemembranceAgentClient getInstance() {
        return sInstance;
    }

    /**
     * The number of characters to store in the buffer AND display in the GUI
     */
    private static final int KEYBOARD_BUFFER_SIZE = 75;

    /**
     * The number of milliseconds between consecutive queries to the RA
     */
    private static final int RA_CLIENT_UPDATE_PERIOD_MS = 2500;

    /**
     * Stores the character buffer sent to the RA
     */
    public final KeyboardLoggerBreakingBuffer mInputBuffer = new KeyboardLoggerBreakingBuffer(KEYBOARD_BUFFER_SIZE);

    /**
     * Used to write to the keylogger log file with a buffer instead of writing once each time
     */
    private final BufferingLogFileWriter mKeyLoggerBufferLogFileWriter;

    /**
     * Used query the RA on a schedule
     */
    private Timer mRAUpdateTimer = new Timer();

    /**
     * The backing RA engine
     */
    private IRemembranceAgentEngine mRemembranceAgentEngine;

    /**
     * Entry-point for the RA client
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(RemembranceAgentClient.getInstance());
    }

    /**
     * Default (and only) constructor
     */
    private RemembranceAgentClient() {
        // Open the keylogger file
        mKeyLoggerBufferLogFileWriter = new BufferingLogFileWriter(User.Preferences.getString(KeystrokesLogFile));
        mKeyLoggerBufferLogFileWriter.open();

        // Add input mechanisms we can use
        InputMechanismManager.getInstance()
                .addInputMechanism(new KeyboardInputMechanism())
                .addInputMechanism(new SpeechInputMechanism(
                        /* trials: */ 1,
                        /* duration per trial: */ 5))
                .initializeAllInputMechanisms()
                .setOnInputCallbacks(this)
                .setActiveInputMechanism(KeyboardInputMechanism.class);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutting down ...");
            } finally {
                mKeyLoggerBufferLogFileWriter.flush();
                mKeyLoggerBufferLogFileWriter.close();
            }
        }));
    }

    /**
     * Starts the RA
     */
    @Override
    public void run() {
        // Show the GUI
        GUI.sJFrame.setVisible(true);

        // init!
        mRemembranceAgentEngine = initializeRAEngine(true);
    }

    /**
     * Initializes the RA engine
     * @param useCache whether or not to use the local document case
     * @return the created engine
     */
    public RemembranceAgentEngine initializeRAEngine(boolean useCache) {

        // Clear the timer
        if (mRAUpdateTimer != null) {
            mRAUpdateTimer.cancel();
            mRAUpdateTimer.purge();
            mRAUpdateTimer = null;
        }

        // Use the RA cache directory
        LocalDiskCacheDocumentDatabase localDiskCacheDatabase =
                new LocalDiskCacheDocumentDatabase(User.Home.Documents.RA.Cache.DIR);

        // Load all the documents into memory and then into the disk
        if (!useCache) {
            localDiskCacheDatabase
                .addDocumentsToMemory(new LocalDiskDocumentDatabase(User.Preferences.getString(LocalDiskDocumentsFolderPath)) {{
                    loadDocuments();
                }})
                .addDocumentsToMemory(new GoogleDriveFolderDocumentDatabase(User.Preferences.getString(GoogleDriveFolderID)) {{
                    loadDocuments();
                }})
                .addDocumentsToMemory(new GmailDocumentDatabase(User.Preferences.getInt(GmailMaxEmailsCount)) {{
                    loadDocuments();
                }})
                .saveDocumentsInMemoryToDisk();
        }

        // Update the GUI with where the suggestions are coming from
        GUI.sSuggestionsPanel.setBorderTitle("Suggestions (from " + localDiskCacheDatabase.getName() + ")", GUI.BORDER_PADDING);
        GUI.sSuggestionsPanel.invalidate();
        GUI.sSuggestionsPanel.repaint();

        // Initialize!
        RemembranceAgentEngine remembranceAgentEngine = new RemembranceAgentEngine(localDiskCacheDatabase);

        // Pull documents from disk
        remembranceAgentEngine.loadDocuments();

        // Index them all
        remembranceAgentEngine.indexDocuments();

        // Tell the user what input is being used
        String inputMechanism = InputMechanismManager.getInstance()
                .getActiveInputMechanism()
                .getInputMechanismName();
        GUI.sKeystrokeBufferLabel.setBorderTitle(inputMechanism, GUI.BORDER_PADDING);

        // Start the RA task
        mRAUpdateTimer = new Timer();
        mRAUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendQueryToRemembranceAgent();
            }
        }, RA_CLIENT_UPDATE_PERIOD_MS, RA_CLIENT_UPDATE_PERIOD_MS);

        return remembranceAgentEngine;
    }

    private void sendQueryToRemembranceAgent() {
        GUI.sJFrame.setTitle("* SEARCHING * " + APPLICATION_NAME + " * SEARCHING *");

        // Build a query
        String queryString = mInputBuffer.toString();
        Context context = new Context(null, User.NAME, queryString, DateUtils.now());
        List<ScoredDocument> suggestions = mRemembranceAgentEngine.determineSuggestions(new Query(queryString, context, GUI.RA_NUMBER_SUGGESTIONS));

        LOGGER.info("Sending query to RA: '" + queryString + "'");

        // Update the GUI
        GUI.sSuggestionsPanel.removeAll();

        // Add the suggestion's elements to the GUI
        LOGGER.info(String.format("Got %d suggestion(s).", suggestions.size()));
        for (int i = 0; i < suggestions.size(); i++) {
            ScoredDocument scoredDocument = suggestions.get(i);

            // And tell the logger
            LOGGER.info(" -> (" + scoredDocument.getScore() + ") " + scoredDocument.toShortString());

            LINQList.from(GUI.getComponentsForScoredDocument(scoredDocument, i))
                .forEach(GUI.sSuggestionsPanel::add);
        }

        // Need to be called for the GUI to update
        GUI.sSuggestionsPanel.validate();
        GUI.sSuggestionsPanel.repaint();

        // Reset the title
        GUI.sJFrame.setTitle(APPLICATION_NAME);
    }

    @Override
    public void onInput(Character c) {
        LOGGER.info("Keystroke: " + c);
        mKeyLoggerBufferLogFileWriter.queue(DateUtils.longTimestamp() + " " + c + "\n");
        mInputBuffer.addCharacter(c);

        LOGGER.info(String.format("[Buffer count=%04d:] %s", mInputBuffer.getTotalTypedCharactersCount(), mInputBuffer.toString()));

        GUI.sKeystrokeBufferLabel.setText(mInputBuffer.toString());
    }
}
