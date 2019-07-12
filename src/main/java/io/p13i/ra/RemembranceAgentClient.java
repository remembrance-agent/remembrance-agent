package io.p13i.ra;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.*;

import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocumentDatabase;
import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;
import io.p13i.ra.models.ScoredDocument;
import io.p13i.ra.utils.KeyboardLoggerBreakingBuffer;
import io.p13i.ra.utils.DateUtils;
import io.p13i.ra.utils.LimitedCapacityBuffer;
import io.p13i.ra.utils.ResourceUtil;
import jdk.internal.loader.Resource;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import static javax.swing.ScrollPaneConstants.*;

public class RemembranceAgentClient implements NativeKeyListener {

    private static final Logger LOGGER = Logger.getLogger(RemembranceAgentClient.class.getName());

    private static JFrame jFrame;
    private static JLabel mKeystrokeBuffer;
    private static JTextArea mTextArea;

    private static KeyboardLoggerBreakingBuffer breakingBuffer = new KeyboardLoggerBreakingBuffer(30);
    private static LimitedCapacityBuffer mUILogBuffer = new LimitedCapacityBuffer(2);
    private static Timer remembranceAgentUpdateTimer = new Timer();
    private static RemembranceAgent remembranceAgent;

    public static void main(String[] args) {

        jFrame = new JFrame("Remembrance Agent") {{
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(600, 195 + 150);
//            setResizable(false);
            add(new JPanel() {

                {
                setLayout(null);
                add(new JLabel() {{
                    setVerticalTextPosition(SwingConstants.TOP);
                    setBounds(10, 10, 580, 75);
                    setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Suggestions"), BorderFactory.createEmptyBorder(5,5,5,5)));
                    setFont(new Font("monospaced", Font.PLAIN, 12));
                }});
                add(mKeystrokeBuffer = new JLabel() {{
                    setVerticalTextPosition(SwingConstants.TOP);
                    setBounds(10, 85, 580, 50);
                    setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Keylogger Buffer"), BorderFactory.createEmptyBorder(5,5,5,5)));
                    setFont(new Font("monospaced", Font.PLAIN, 12));
                }});
                add(new JScrollPane(mTextArea = new JTextArea() {{
                    setFont(new Font("monospaced", Font.PLAIN, 12));
                }}, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS) {{
                    getViewport().setPreferredSize(new Dimension(580, 50 + 150));
                    setBounds(10, 130, 580, 50 + 150);
                    setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Logs"), BorderFactory.createEmptyBorder(5,5,5,5)));
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
                    mTextArea.append("\n");
                    mTextArea.append(DateUtils.timestamp() + message);
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
                String query = breakingBuffer.toString();
                Context context = new Context(null, "p13i", query, DateUtils.now());
                final int numSuggestions = 2;

                LOGGER.info("Sending query to RA: '" + query + "'");
                List<ScoredDocument> suggestions = remembranceAgent.determineSuggestions(query, context, numSuggestions);

                if (suggestions.isEmpty()) {
                    LOGGER.info("No suggestions :(");
                } else {
                    LOGGER.info(String.format("Got %d suggestion(s):", suggestions.size()));
                    mKeystrokeBuffer.setText(String.format("Got %d suggestion(s):", suggestions.size()));

                    for (ScoredDocument doc : suggestions) {
                        LOGGER.info(" -> " + doc.toString());
                    }
                }
            }
        }, 5000, 5000);
        LOGGER.info("Scheduled Remembrance Agent update task.");
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        LOGGER.info("Keystroke: " + keyText);

        Character characterToAdd = null;
        if (e.isActionKey() && e.getKeyCode() != NativeKeyEvent.VC_SHIFT) {
            characterToAdd = ' ';
        } else {
            if (keyText.length() == 1) {
                characterToAdd = keyText.charAt(0);
            } else {
                if (keyText.equals("Space")) {
                    characterToAdd = ' ';
                }
            }
        }

        if (characterToAdd != null) {
            breakingBuffer.addCharacter(characterToAdd);
            String bufferMessage = String.format("[Buffer count=%04d:] %s", breakingBuffer.getTotalTypedCharactersCount(), breakingBuffer.toString());
            LOGGER.info(bufferMessage);
            mKeystrokeBuffer.setText(bufferMessage);
        }

    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        // Nothing
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
        // Nothing here
    }
}
