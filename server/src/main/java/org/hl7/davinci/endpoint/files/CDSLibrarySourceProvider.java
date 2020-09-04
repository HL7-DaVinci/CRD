package org.hl7.davinci.endpoint.files;

import java.io.IOException;
import java.io.InputStream;

import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CDSLibrarySourceProvider implements LibrarySourceProvider {

  static final Logger logger = LoggerFactory.getLogger(CDSLibrarySourceProvider.class);

  public static String LIBRARY_TOPIC = "Shared";

  private FileStore fileStore;

  public CDSLibrarySourceProvider(FileStore fileStore) {
    this.fileStore = fileStore;
  }

	@Override
	public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
    String filename = libraryIdentifier.getId() + "-" + libraryIdentifier.getVersion() + ".cql";

    FileResource file = fileStore.getFile(LIBRARY_TOPIC, filename, "R4", false);

    if (file != null) {
      try {
        logger.info("Found " + filename + " CQL Library.");
        return file.getResource().getInputStream();
      } catch (IOException ioe) {
        logger.error("Error loading " + filename + " CQL Library.");
        return null;
      }
    } else {
      logger.warn("Could not find " + filename + " CQL Library.");
      return null;
    }
	}
  
}