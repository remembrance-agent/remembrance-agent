package io.p13i.ra.databases.gmail;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import io.p13i.ra.databases.IDocumentDatabase;
import io.p13i.ra.databases.cache.ICachableDocumentDatabase;
import io.p13i.ra.utils.GoogleAPIUtils;
import io.p13i.ra.utils.LoggerUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static io.p13i.ra.RemembranceAgentClient.APPLICATION_NAME;

/**
 * Enables use of Gmail as a document database.
 * Only email with the label "RA" (or equivalently "ra") are indexed.
 */
public class GmailDocumentDatabase implements IDocumentDatabase<GmailDocument>, ICachableDocumentDatabase {

    private static final Logger LOGGER = LoggerUtils.getLogger(GmailDocumentDatabase.class);

    /**
     * The format of dates in the message.
     */
    private static final DateFormat MESSAGE_DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

    /**
     * The emails loaded into the database
     */
    private List<GmailDocument> gmailDocuments;

    /**
     * The maximum number of results to pull from the Gmail API
     */
    private long gmailResultsLimit;

    /**
     * @param gmailResultsLimit the number of emails to limit the search to
     */
    public GmailDocumentDatabase(long gmailResultsLimit) {
        this.gmailResultsLimit = gmailResultsLimit;
    }

    @Override
    public void loadDocuments() {
        this.gmailDocuments = new ArrayList<>();

        Gmail service = getGmailService();

        // Print the labels in the user's account.
        try {
            List<Message> response = service
                    .users()
                    .messages()
                    .list("me")
                    .setQ("label:ra")
                    .setMaxResults(this.gmailResultsLimit)
                    .execute()
                    .getMessages();


            for (Message message : response) {

                LOGGER.info("Loading message: " + message.getId());

                Message fullMessage = service
                        .users()
                        .messages()
                        .get("me", message.getId())
                        .setFormat("full")
                        .execute();


                if (fullMessage == null) {
                    continue;
                }

                GmailDocument gmailDocument = new GmailDocument(fullMessage.getId(), getMessageContent(fullMessage), getMessageSubject(fullMessage), getMessageSender(fullMessage), getReceivedDate(fullMessage));
                this.gmailDocuments.add(gmailDocument);

            }
        } catch (IOException e) {
            LOGGER.throwing(GmailDocumentDatabase.class.getSimpleName(), "loadDocuments", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the receive date for an email
     *
     * @param message the email
     * @return the parsed date
     */
    private Date getReceivedDate(Message message) {
        return getHeaderValues(message, "Date")
                .map(GmailDocumentDatabase::tryParseMessageDate)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<GmailDocument> getAllDocuments() {
        return this.gmailDocuments;
    }

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);
    private static final String TOKENS_DIRECTORY_PATH = "gmail-tokens";

    /**
     * Gets a Google Mail client
     *
     * @return a Gmail client
     */
    private Gmail getGmailService() {
        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            return new Gmail.Builder(HTTP_TRANSPORT, GoogleAPIUtils.JSON_FACTORY, GoogleAPIUtils.getCredentials(HTTP_TRANSPORT, SCOPES, TOKENS_DIRECTORY_PATH))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets all the header values for a name in the message
     *
     * @param message    the email
     * @param headerName the name of the header
     * @return list of matching values for this header
     */
    private static Stream<String> getHeaderValues(Message message, String headerName) {
        return message.getPayload().getHeaders().stream()
                .filter(header -> header.getName().equals(headerName))
                .map(MessagePartHeader::getValue);
    }

    /**
     * Trys to parse a date or returns null
     *
     * @param dateString the date string
     * @return a Date or null if there's a parse error
     */
    private static Date tryParseMessageDate(String dateString) {
        try {
            return MESSAGE_DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param message the email
     * @return the sender value
     */
    private String getMessageSender(Message message) {
        return getHeaderValues(message, "From").findFirst().orElse(null);
    }

    /**
     * @param message the email
     * @return the subject value
     */
    private static String getMessageSubject(Message message) {
        return getHeaderValues(message, "Subject").findFirst().orElse(null);
    }

    /**
     * https://stackoverflow.com/a/38828761
     */
    private static String getMessageContent(Message message) {
        StringBuilder stringBuilder = new StringBuilder();
        getPlainTextFromMessagePartsRecursive(message.getPayload().getParts(), stringBuilder);
        try {
            return new String(Base64.getDecoder().decode(stringBuilder.toString()), Charset.defaultCharset());
        } catch (IllegalArgumentException ignored) {
            return "";
        }
    }

    /**
     * https://stackoverflow.com/a/38828761
     */
    private static void getPlainTextFromMessagePartsRecursive(List<MessagePart> messageParts, StringBuilder stringBuilder) {
        if (messageParts == null) {
            stringBuilder.append("null");
            return;
        }

        for (MessagePart messagePart : messageParts) {
            if (messagePart.getMimeType().equals("text/plain")) {
                stringBuilder.append(messagePart.getBody().getData());
            }

            if (messagePart.getParts() != null) {
                getPlainTextFromMessagePartsRecursive(messagePart.getParts(), stringBuilder);
            }
        }
    }
}
