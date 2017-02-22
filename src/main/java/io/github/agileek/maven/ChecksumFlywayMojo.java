package io.github.agileek.maven;

import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JEnumConstant;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;
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
    String location;

    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}/generated-sources")
    String outputDirectory;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;


    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        JCodeModel javaFile = generateEnumWithFilesChecksum(getJavaFiles(project.getCompileSourceRoots(), location));
        try {
            File file = new File(outputDirectory);
            if (!file.exists()) {
                boolean mkdirs = file.mkdirs();
                if (!mkdirs) {
                    getLog().warn("Couldn't create " + outputDirectory);
                }
            }
            javaFile.build(file, (PrintStream) null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    JCodeModel generateEnumWithFilesChecksum(List<File> files) {
        JCodeModel codeModel = new JCodeModel();
        JDefinedClass enumClass;
        try {
            enumClass = codeModel._class("io.github.agileek.flyway.JavaMigrationChecksums", EClassType.ENUM);
        } catch (JClassAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        JFieldVar checksumField = enumClass.field(JMod.PRIVATE | JMod.FINAL, long.class, "checksum");

        //Define the enum constructor
        JMethod enumConstructor = enumClass.constructor(JMod.PRIVATE);
        enumConstructor.param(long.class, "checksum");
        enumConstructor.body().assign(JExpr._this().ref("checksum"), JExpr.ref("checksum"));

        JMethod getterColumnMethod = enumClass.method(JMod.PUBLIC, long.class, "getChecksum");
        getterColumnMethod.body()._return(checksumField);

        for (File file : files) {
            JEnumConstant enumConst = enumClass.enumConstant(file.getName().split("\\.")[0]);
            enumConst.arg(JExpr.lit(computeFileChecksum(file)));
        }

        return codeModel;
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

    long computeFileChecksum(File file) {
        final CRC32 crc32 = new CRC32();

        try {
            RandomAccessFile r = new RandomAccessFile(file, "r");
            byte[] b = new byte[(int) r.length()];
            r.readFully(b);
            crc32.update(b);
        } catch (IOException e) {
            String message = "Unable to calculate checksum for " + file.getAbsolutePath();
            throw new RuntimeException(message, e);
        }
        return crc32.getValue();
    }
}