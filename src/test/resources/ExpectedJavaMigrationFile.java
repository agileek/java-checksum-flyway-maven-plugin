package io.github.agileek.flyway;

public enum JavaMigrationChecksums {
    Toto(-1927787285);
    private final int checksum;

    private JavaMigrationChecksums(int checksum) {
        this.checksum = checksum;
    }

    public int getChecksum() {
        return checksum;
    }
}
