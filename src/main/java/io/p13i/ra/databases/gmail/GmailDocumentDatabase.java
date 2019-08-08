package io.p13i.ra.databases.gmail;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.cache.CachableDocument;
import io.p13i.ra.databases.cache.CachableDocumentDatabase;
import io.p13i.ra.models.Document;
import io.p13i.ra.utils.GoogleAPIUtils;
import io.p13i.ra.utils.ListUtils;
import io.p13i.ra.utils.LINQ;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.p13i.ra.RemembranceAgentClient.APPLICATION_NAME;

public class GmailDocumentDatabase implements DocumentDatabase, CachableDocumentDatabase {

    private static final DateFormat MESSAGE_DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

    private List<GmailDocument> gmailDocuments;
    private long gmailResultsLimit;

    public GmailDocumentDatabase(long gmailResultsLimit) {
        this.gmailResultsLimit = gmailResultsLimit;
    }

    @Override
    public String getName() {
        return GmailDocumentDatabase.class.getSimpleName();
    }

    @Override
    public void loadDocuments() {
        this.gmailDocuments = new ArrayList<>();

        Gmail service = getGmailService();

        // Print the labels in the user's account.
        try {
            List<Message> response = service.users().messages().list("me").setMaxResults(this.gmailResultsLimit)
                    .execute().getMessages();
            for (Message message : response) {
                Message r = service.users().messages().get("me", message.getId()).setFormat("full")
                        .execute();
                if (r == null) {
                    continue;
                }

                this.gmailDocuments.add(new GmailDocument(r.getId(), getMessageContent(r), getMessageSubject(r), getMessageSender(r), getReceivedDate(r)));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Date getReceivedDate(Message message) {
        try {
            List<String> received = getHeaderValues(message, "Date");

            if (received.size() == 0) {
                return null;
            }

            return MESSAGE_DATE_FORMAT.parse(received.get(0));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Document> getAllDocuments() {
        return ListUtils.castUp(this.gmailDocuments);
    }

    @Override
    public List<CachableDocument> getDocumentsForSavingToCache() {
        return ListUtils.castUp(this.gmailDocuments);
    }


    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);

    private Gmail getGmailService() {
        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            return new Gmail.Builder(HTTP_TRANSPORT, GoogleAPIUtils.JSON_FACTORY,  GoogleAPIUtils.getCredentials(HTTP_TRANSPORT, SCOPES))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static List<String> getHeaderValues(Message message, String headerName) {
        return new LINQ<>(message.getPayload().getHeaders())
                .where(header -> header.getName().equals(headerName))
                .select(MessagePartHeader::getValue)
                .toList();
    }

    private String getMessageSender(Message message) {
        return new LINQ<>(getHeaderValues(message, "From")).firstOrDefault();
    }

    private static String getMessageSubject(Message message) {
        return new LINQ<>(getHeaderValues(message, "Subject")).firstOrDefault();
    }

    /*
    https://stackoverflow.com/a/38828761
     */
    private static String getMessageContent(Message message) {
        StringBuilder stringBuilder = new StringBuilder();
        getPlainTextFromMessagePartsRecursive(message.getPayload().getParts(), stringBuilder);
        return new String(Base64.decodeBase64(stringBuilder.toString()), StandardCharsets.UTF_8);
    }

    /*
    https://stackoverflow.com/a/38828761
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
