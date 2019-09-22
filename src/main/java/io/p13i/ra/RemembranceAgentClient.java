package io.p13i.ra;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import io.p13i.ra.cache.Cache;
import io.p13i.ra.databases.cache.LocalDiskCacheDocumentDatabase;
import io.p13i.ra.databases.gmail.GmailDocumentDatabase;
import io.p13i.ra.databases.googledrive.GoogleDriveFolderDocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocumentDatabase;
import io.p13i.ra.engine.IRemembranceAgentEngine;
import io.p13i.ra.engine.RemembranceAgentEngine;
import io.p13i.ra.gui.GUI;
import io.p13i.ra.gui.User;
import io.p13i.ra.input.AbstractInputMechanism;
import io.p13i.ra.input.KeyboardInputMechanism;
import io.p13i.ra.models.*;
import io.p13i.ra.utils.BufferingLogFileWriter;
import io.p13i.ra.utils.DateUtils;
import io.p13i.ra.utils.KeyboardLoggerBreakingBuffer;
import io.p13i.ra.utils.LoggerUtils;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.swing.SwingUtilities;

import static io.p13i.ra.gui.User.Preferences.Preference.GmailMaxEmailsCount;
import static io.p13i.ra.gui.User.Preferences.Preference.GoogleDriveFolderIDs;
import static io.p13i.ra.gui.User.Preferences.Preference.KeystrokesLogFile;
import static io.p13i.ra.gui.User.Preferences.Preference.LocalDiskDocumentsFolderPath;

/**
 * Implementation of RA GUI
 *
 * @author Pramod Kotipalli
 */
public class RemembranceAgentClient implements Runnable, AbstractInputMechanism.InputMechanismEventsListener {

    private static final Logger LOGGER = LoggerUtils.getLogger(RemembranceAgentClient.class);

    public static final String VERSION = "2.0";

    public static final String APPLICATION_NAME = "Remembrance Agent (v" + VERSION + ")";

    private static RemembranceAgentClient sInstance = new RemembranceAgentClient();

    /**
     * @return the RA instance
     */
    public static RemembranceAgentClient getInstance() {
        return sInstance;
    }

    /**
     * The GUI reference
     */
    private GUI mGUI = new GUI();

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
     * Used to append to the keylogger log file with a buffer instead of writing once each time
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
     * The backing data store for the RA
     */
    private LocalDiskCacheDocumentDatabase mLocalDiskCacheDatabase;

    /**
     * Tracks the currently used input mechanism
     */
    private AbstractInputMechanism mCurrentInputMechanism;

    /**
     * Tracks whether a query is pending to the RA
     */
    private boolean mQueryingRemembranceAgentEngine = false;

    /**
     * Entry-point for the RA client
     *
     * @param args see usage
     */
    public static void main(String[] args) {
        Options options = new Options() {{
            addOption(new Option("h", "home", /* hasArg: */ true, "the user's home directory") {{
                setRequired(true);
            }});
        }};

        CommandLine commandLine;

        try {
            commandLine = new BasicParser().parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            new HelpFormatter().printHelp("remebrance-agent-gui", options);
            System.exit(1);
            return;
        }

        User.Home.setDirectory(commandLine.getOptionValue("home"));

        SwingUtilities.invokeLater(RemembranceAgentClient.getInstance());
    }

    /**
     * Default (and only) constructor
     */
    public RemembranceAgentClient() {
        // Open the keylogger file
        mKeyLoggerBufferLogFileWriter = new BufferingLogFileWriter(User.Preferences.getString(KeystrokesLogFile));
        mKeyLoggerBufferLogFileWriter.open();

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
        mGUI.setVisible(true);

        // init!
        mRemembranceAgentEngine = initializeRAEngine(true);
    }

    public void startInputMechanism(AbstractInputMechanism inputMechanism) {
        if (mCurrentInputMechanism != null) {
            mCurrentInputMechanism.closeInputMechanism();
        }
        mCurrentInputMechanism = inputMechanism;
        mCurrentInputMechanism.startInputMechanism();
    }

    public void addDocumentToDataStore(AbstractDocument document) {
        String filePath = this.mLocalDiskCacheDatabase.saveSingleDocumentToDisk(document);
        this.mLocalDiskCacheDatabase.loadSingleDocumentFromDiskIntoMemory(filePath);
        Cache.invalidateManagedCaches();
    }

    /**
     * Initializes the RA engine
     *
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
        mLocalDiskCacheDatabase =
                new LocalDiskCacheDocumentDatabase(User.Home.Documents.RA.Cache.getDirectory());

        // Load all the documents into memory and then into the disk
        if (!useCache) {
            // Load files from the local documents directory
            mLocalDiskCacheDatabase.addDocumentsToMemory(new LocalDiskDocumentDatabase(User.Preferences.getString(LocalDiskDocumentsFolderPath)) {{
                loadDocuments();
            }});

            // Load files from Google Drive
            String googleDriveFolderIDs = User.Preferences.getString(GoogleDriveFolderIDs);

            String[] ids = googleDriveFolderIDs.split(",");
            for (String s : ids) {
                String id = s.trim();
                LOGGER.info("Loading files from Google Drive folder with: " + id);

                mLocalDiskCacheDatabase.addDocumentsToMemory(new GoogleDriveFolderDocumentDatabase(id) {{
                    loadDocuments();
                }});
            }

            // Load files from Gmail
            mLocalDiskCacheDatabase.addDocumentsToMemory(new GmailDocumentDatabase(User.Preferences.getInt(GmailMaxEmailsCount)) {{
                loadDocuments();
            }});

            // Save these files to disk
            mLocalDiskCacheDatabase.saveDocumentsInMemoryToDisk();

            // Reload the new documents from disk
            mLocalDiskCacheDatabase.loadDocuments();
        }

        // Update the GUI with where the suggestions are coming from
        mGUI.setSuggestionsPanelTitle("Suggestions (from " + mLocalDiskCacheDatabase.getName() + ")");

        // Initialize!
        RemembranceAgentEngine remembranceAgentEngine = new RemembranceAgentEngine(mLocalDiskCacheDatabase);

        // Pull documents from disk
        remembranceAgentEngine.loadDocuments();

        // Index them all
        remembranceAgentEngine.indexDocuments();

        // Set the input mechanism being used
        this.startInputMechanism(new KeyboardInputMechanism());

        // Start the RA task
        mRAUpdateTimer = new Timer();
        mRAUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();

                try {
                    sendQueryToRemembranceAgent();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                long endTime = System.currentTimeMillis();

                long timeElapsed = endTime - startTime;

                LOGGER.info("Query sending took " + timeElapsed + " ms");

            }
        }, RA_CLIENT_UPDATE_PERIOD_MS, RA_CLIENT_UPDATE_PERIOD_MS);

        return remembranceAgentEngine;
    }

    /**
     * Sends the contextual {@code Query} to the RA
     */
    private void sendQueryToRemembranceAgent() {
        if (mQueryingRemembranceAgentEngine) {
            return;
        }

        mQueryingRemembranceAgentEngine = true;

        mGUI.setTitle("* SEARCHING * " + APPLICATION_NAME + " * SEARCHING *");

        // Build a query
        String queryString = mInputBuffer.toString();
        Context context = new Context(null, User.NAME, queryString, null);
        Query query = new Query(queryString, context, GUI.RA_NUMBER_SUGGESTIONS) {{
            index();
        }};
        List<ScoredDocument> suggestions = mRemembranceAgentEngine.determineSuggestions(query);


        // Update the GUI
        mGUI.removeScoredDocuments();

        // Add the suggestion's elements to the GUI
        for (int i = 0; i < suggestions.size(); i++) {
            ScoredDocument scoredDocument = suggestions.get(i);
            
            // Add each of the components for the document to the GUI
            mGUI.addScoredDocument(scoredDocument, i);
        }

        // Reset the title
        mGUI.setTitle(APPLICATION_NAME);

        mQueryingRemembranceAgentEngine = false;
    }

    @Override
    public void onInputReady(AbstractInputMechanism inputMechanism) {
        mGUI.setInputMechanism(inputMechanism);
    }

    @Override
    public void onInput(AbstractInputMechanism inputMechanism, Character c) {
        // Write the timestamp and character to the keylogger log file
        mKeyLoggerBufferLogFileWriter.queue(DateUtils.longTimestamp() + " " + c + "\n");

        // Add to the buffer
        mInputBuffer.addCharacter(c);

        // Display on the GUI
        mGUI.setKeystrokesBufferText(mInputBuffer.toString());
    }
}
