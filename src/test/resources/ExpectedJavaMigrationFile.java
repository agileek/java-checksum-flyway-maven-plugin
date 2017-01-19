package io.github.agileek.flyway;

public enum JavaMigrationChecksums {
    Toto(2367180011L);
    private final long checksum;

    private JavaMigrationChecksums(long checksum) {
        this.checksum = checksum;
    }

    public long getChecksum() {
        return checksum;
    }
}
