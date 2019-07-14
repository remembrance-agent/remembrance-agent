package io.p13i.ra.databases.googledrive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.cache.CachableDocument;
import io.p13i.ra.databases.cache.CachableDocumentDatabase;
import io.p13i.ra.models.Document;
import io.p13i.ra.utils.ListUtils;
import io.p13i.ra.utils.LoggerUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class GoogleDriveFolderDocumentDatabase implements DocumentDatabase, CachableDocumentDatabase {

    private static Logger LOGGER = LoggerUtils.getLogger(GoogleDriveFolderDocumentDatabase.class);

    private static final String APPLICATION_NAME = "Remembrance Agent";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveFolderDocumentDatabase.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private String rootFolderID;
    private List<GoogleDriveDocument> googleDriveDocuments;

    public GoogleDriveFolderDocumentDatabase(String rootFolderID) {
        this.rootFolderID = rootFolderID;
        this.googleDriveDocuments = new ArrayList<>();
    }

    @Override
    public String getName() {
        return "Google Drive folder: " + this.rootFolderID;
    }

    @Override
    public void loadDocuments() {
        try {
            loadDocumentsRecursive(getClient(), this.googleDriveDocuments, this.rootFolderID /* recursive: */);
        } catch (IOException e) {
            LOGGER.warning(e.toString());
            e.printStackTrace();
        }
    }

    private Drive getClient() {
        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            return service;
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void loadDocumentsRecursive(Drive service, List<GoogleDriveDocument> documents, String parentFolderID) throws IOException {
        FileList filesList = service.files().list()
                .setQ("'" + parentFolderID + "' in parents and mimeType != 'application/vnd.google-apps.folder'")
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name, parents, modifiedTime)")
                .execute();

        for (File file : filesList.getFiles()) {

            LOGGER.info("Loading: " + file.getName());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            service.files().export(file.getId(), "text/plain")
                    .executeMediaAndDownloadTo(outputStream);
            LOGGER.info("Done.");

            String fileContents = new String(outputStream.toByteArray(), Charset.defaultCharset());

            documents.add(new GoogleDriveDocument(file.getId(), fileContents, file.getName(), new Date(file.getModifiedTime().getValue())));
        }

        FileList foldersList = service.files().list()
                .setQ("'" + parentFolderID + "' in parents and mimeType = 'application/vnd.google-apps.folder'")
                .setSpaces("drive")
                .setFields("nextPageToken, files(id)")
                .execute();
        for (File file : foldersList.getFiles()) {
            // recurse
            loadDocumentsRecursive(service, documents, file.getId() /* recursive: */);
        }

    }

    @Override
    public List<Document> getAllDocuments() {
        return ListUtils.castUp(this.googleDriveDocuments, Document.class);
    }


    @Override
    public List<CachableDocument> getDocumentsForSavingToCache() {
        return ListUtils.castUp(this.googleDriveDocuments, CachableDocument.class);
    }
}
