package io.p13i.ra.databases.googledrive;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.cache.CachableDocument;
import io.p13i.ra.databases.cache.CachableDocumentDatabase;
import io.p13i.ra.models.Document;
import io.p13i.ra.utils.GoogleAPIUtils;
import io.p13i.ra.utils.ListUtils;
import io.p13i.ra.utils.LoggerUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class GoogleDriveFolderDocumentDatabase implements DocumentDatabase, CachableDocumentDatabase {

    private static final Logger LOGGER = LoggerUtils.getLogger(GoogleDriveFolderDocumentDatabase.class);

    private static final String APPLICATION_NAME = "Remembrance Agent";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_READONLY);

    private final String rootFolderID;
    private final List<GoogleDriveDocument> googleDriveDocuments;

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
            return new Drive.Builder(HTTP_TRANSPORT, GoogleAPIUtils.JSON_FACTORY, GoogleAPIUtils.getCredentials(HTTP_TRANSPORT, SCOPES))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
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

            if (file.getName().contains("ra:no-index")) {
                continue;
            }

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
        return ListUtils.castUp(this.googleDriveDocuments);
    }

    @Override
    public List<CachableDocument> getDocumentsForSavingToCache() {
        return ListUtils.castUp(this.googleDriveDocuments);
    }
}
