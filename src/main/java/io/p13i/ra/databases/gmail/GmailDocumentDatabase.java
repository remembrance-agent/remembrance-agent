package io.p13i.ra.databases.gmail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.cache.CachableDocument;
import io.p13i.ra.databases.cache.CachableDocumentDatabase;
import io.p13i.ra.databases.googledrive.GoogleDriveFolderDocumentDatabase;
import io.p13i.ra.models.Document;
import io.p13i.ra.utils.GoogleAPIUtils;
import io.p13i.ra.utils.ListUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static io.p13i.ra.RemembranceAgentClient.APPLICATION_NAME;

public class GmailDocumentDatabase implements DocumentDatabase, CachableDocumentDatabase {

    private List<GmailDocument> gmailDocuments;

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
            List<Message> response = service.users().messages().list("me").setMaxResults(10L)
                    .execute().getMessages();
            for (Message message : response) {
                Message r = service.users().messages().get("me", message.getId()).setFormat("full")
                        .execute();
                if (r == null) {
                    continue;
                }

                String body = getMessageContent(r);
                String subject = getMessageSubject(r);
                String sender = getMessageSender(r);
                Date receivedDate = getReceivedDate(r);
                this.gmailDocuments.add(new GmailDocument(body, subject, sender, receivedDate));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Date getReceivedDate(Message message) {
        try {
            List<String> received = getHeaderValues(message, "Received");
            List<String> utcIncludingHeaders = ListUtils.filter(received, new ListUtils.Filter<String>() {
                @Override
                public boolean shouldInclude(String item) {
                    return item.contains("+0000");
                }
            });
            String[] receivedHeaderParts = utcIncludingHeaders.get(0).split(";");
            String dateString = receivedHeaderParts[1].trim();
            dateString = dateString.substring(0, dateString.indexOf("+0000") + "+0000".length());
            DateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            return format.parse(dateString);
        } catch (ParseException | IndexOutOfBoundsException e) {
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

    public static void main(String... args) {
        // Build a new authorized API client service.
        GmailDocumentDatabase database = new GmailDocumentDatabase();
        database.loadDocuments();
        for (Document document : database.getAllDocuments()) {
            System.out.println(document.getContext().getDate());
        }
    }

    private Gmail getGmailService() {
        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, GoogleAPIUtils.JSON_FACTORY,  GoogleAPIUtils.getCredentials(HTTP_TRANSPORT, SCOPES))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            return service;
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static List<String> getHeaderValues(Message message, String headerName) {
        List<String> matchingValues = new ArrayList<>();
        for (MessagePartHeader header : message.getPayload().getHeaders()) {
            if (header.getName().equals(headerName)) {
                matchingValues.add(header.getValue());
            }
        }
        return matchingValues;
    }

    private String getMessageSender(Message message) {
        return getHeaderValues(message, "From").get(0);
    }

    private static String getMessageSubject(Message message) {
        return getHeaderValues(message, "Subject").get(0);
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
