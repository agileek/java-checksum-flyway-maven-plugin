package io.github.agileek.flyway;

public enum JavaMigrationChecksums {
    Toto(-1927787285),
    NewMigration(1704905491);
    private final int checksum;

    private JavaMigrationChecksums(int checksum) {
        this.checksum = checksum;
    }

    public int getChecksum() {
        return checksum;
    }
}
