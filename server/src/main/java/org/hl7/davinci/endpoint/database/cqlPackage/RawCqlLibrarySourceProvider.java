package org.hl7.davinci.endpoint.database.cqlPackage;

import java.io.InputStream;
import java.util.HashMap;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;


public class RawCqlLibrarySourceProvider implements LibrarySourceProvider {

  private HashMap<VersionedIdentifier, InputStream> rawCqlLibraries;

  public RawCqlLibrarySourceProvider(
      HashMap<VersionedIdentifier, InputStream> rawCqlLibraries) {
    this.rawCqlLibraries = rawCqlLibraries;
  }

  @Override
  public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
    return rawCqlLibraries.get(libraryIdentifier);
  }
}