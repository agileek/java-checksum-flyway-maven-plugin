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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    @Parameter(name = "generatedSourcesFolder", defaultValue = "${project.build.directory}/generated-sources")
    String generatedSourcesFolder;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;


    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        JCodeModel javaFile = generateEnumWithFilesChecksum(getJavaFiles(project.getCompileSourceRoots(), location));
        try {
            File file = new File(generatedSourcesFolder);
            if (!file.exists()) {
                boolean mkdirs = file.mkdirs();
                if (!mkdirs) {
                    getLog().warn("Couldn't create " + generatedSourcesFolder);
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
        JFieldVar checksumField = enumClass.field(JMod.PRIVATE | JMod.FINAL, String.class, "checksum");

        //Define the enum constructor
        JMethod enumConstructor = enumClass.constructor(JMod.PRIVATE);
        enumConstructor.param(String.class, "checksum");
        enumConstructor.body().assign(JExpr._this().ref("checksum"), JExpr.ref("checksum"));

        JMethod getterColumnMethod = enumClass.method(JMod.PUBLIC, String.class, "getChecksum");
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