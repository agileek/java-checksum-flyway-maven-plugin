package io.github.agileek.maven;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(
        name = "generate",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
public class ChecksumFlywayMojo extends AbstractMojo {
    @Parameter(name = "location", defaultValue = "/db/migration")
    private String location;

    @Parameter(name = "generatedSourcesFolder", defaultValue = "${project.build.directory}/generated-sources")
    private String generatedSourcesFolder;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;


    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        JavaFile javaFile = generateEnumWithFilesChecksum(getJavaFiles(project.getCompileSourceRoots(), location));
        try {
            javaFile.writeTo(new File(generatedSourcesFolder));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    JavaFile generateEnumWithFilesChecksum(List<File> files) {
        TypeSpec.Builder builder = TypeSpec.enumBuilder("JavaMigrationChecksums").addModifiers(Modifier.PUBLIC);

        for (File file : files) {
            builder.addEnumConstant(file.getName().split("\\.")[0], TypeSpec.anonymousClassBuilder("$S", computeFileChecksum(file)).build());
        }
        return JavaFile
                .builder("io.github.agileek.flyway",
                        builder
                                .addField(String.class, "checksum", Modifier.PRIVATE, Modifier.FINAL)
                                .addMethod(
                                        MethodSpec
                                                .constructorBuilder()
                                                .addParameter(String.class, "checksum")
                                                .addStatement("this.$N = $N", "checksum", "checksum")
                                                .build()
                                )
                                .build()
                )
                .build();

    }

    List<File> getJavaFiles(List<String> compileSourceRoots, String location) {
        List<File> files = new ArrayList<File>();
        for (String compileSourceRoot : compileSourceRoots) {
            File file = new File(compileSourceRoot + location);
            File[] java = file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".java");
                }
            });
            if (java != null) {
                Collections.addAll(files, java);
            }
        }
        return files;
    }

    String computeFileChecksum(File file) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        RandomAccessFile f = null;
        try {
            try {
                f = new RandomAccessFile(file, "r");
                byte[] b = new byte[(int) f.length()];
                f.readFully(b);
                return this.toHexString(md.digest(b));
            } finally {
                if (f != null) {
                    f.close();
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    private String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}