package org.hl7.davinci.endpoint.cql;

import java.io.InputStream;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;

/**
 * This file mostly copied from the CQL Engine test files. Created by Bryn on 12/29/2016.
 */
public class LocalLibrarySourceProvider implements LibrarySourceProvider {
//  @Override
//  public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
//    String libraryFileName = String.format("stu3/%s%s.cql",
//        libraryIdentifier.getId(), libraryIdentifier.getVersion() != null ? ("-" + libraryIdentifier.getVersion()) : "");
//    return TestLibrarySourceProviderClone.class.getResourceAsStream(libraryFileName);
//  }

  @Override
  public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
    InputStream s = getClass().getClassLoader().getResourceAsStream("FHIRHelpers-3.0.0.cql");
    return s;
    //.getClassLoader()
    //.getResourceAsStream("FHIRHelpers-3.0.0.cql").());
//    byte[] fileBytes = Files.readAllBytes(path);
//    String libraryFileName = String.format("stu3/%s%s.cql",
//        libraryIdentifier.getId(), libraryIdentifier.getVersion() != null ? ("-" + libraryIdentifier.getVersion()) : "");
//    return TestLibrarySourceProviderClone.class.getResourceAsStream(libraryFileName);
  }
}
