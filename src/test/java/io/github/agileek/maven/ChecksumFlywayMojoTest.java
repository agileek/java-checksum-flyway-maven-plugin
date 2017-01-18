package io.github.agileek.maven;


import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ChecksumFlywayMojoTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldGenerateFileChecksum() throws Exception {
        List<File> files = new ArrayList<File>();
        ChecksumFlywayMojo tested = new ChecksumFlywayMojo();
        files.add(new File("src/test/resources/java/db/migration/Toto.java"));
        JavaFile actual = tested.generateEnumWithFilesChecksum(files);

        File generatedJavaFolder = temporaryFolder.newFolder();
        actual.writeTo(generatedJavaFolder);
        assertThat(new File(generatedJavaFolder.getAbsolutePath(), "io/github/agileek/flyway/JavaMigrationChecksums.java"))
                .hasSameContentAs(new File("src/test/resources/ExpectedJavaMigrationFile.java"));
    }

    @Test
    public void shouldComputeFileChecksum() throws Exception {
        ChecksumFlywayMojo tested = new ChecksumFlywayMojo();
        String actual = tested.computeFileChecksum(new File("src/test/resources/java/db/migration/Toto.java"));


        assertThat(actual).isEqualTo("60e90cc0aedc457d95ebfa601f366c10");
    }

    @Test
    public void shouldGetJavaFilesFromSourceRoots() throws Exception {
        List<String> compileSourceRoots = new ArrayList<String>();
        compileSourceRoots.add("src/test/resources/java");


        ChecksumFlywayMojo tested = new ChecksumFlywayMojo();
        List<File> javaFiles = tested.getJavaFiles(compileSourceRoots, "/db/migration");


        assertThat(javaFiles).containsExactly(new File("src/test/resources/java/db/migration/Toto.java"));
    }

    @Test
    public void shouldGetJavaFilesFromSourceRoots_noFiles() throws Exception {
        List<String> compileSourceRoots = new ArrayList<String>();
        compileSourceRoots.add("src/test/resources/");


        ChecksumFlywayMojo tested = new ChecksumFlywayMojo();
        List<File> javaFiles = tested.getJavaFiles(compileSourceRoots, "/db/migration");


        assertThat(javaFiles).isEmpty();
    }
}