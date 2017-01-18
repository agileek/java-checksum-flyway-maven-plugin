package io.github.agileek.flyway;

public enum JavaMigrationChecksums {
    Toto("60e90cc0aedc457d95ebfa601f366c10");
    private final String checksum;

    private JavaMigrationChecksums(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksum() {
        return checksum;
    }
}
