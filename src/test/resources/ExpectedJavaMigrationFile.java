package io.github.agileek.flyway;

import java.lang.String;

public enum JavaMigrationChecksums {
  Toto("60e90cc0aedc457d95ebfa601f366c10");

  private final String checksum;

  JavaMigrationChecksums(String checksum) {
    this.checksum = checksum;
  }
}