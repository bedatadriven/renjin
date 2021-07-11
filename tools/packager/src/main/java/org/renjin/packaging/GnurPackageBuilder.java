package org.renjin.packaging;

import io.airlift.airline.*;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.PackageLoader;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Command(name = "build")
public class GnurPackageBuilder implements BuildContext  {

    @Option(name = "--home", required = true)
    private File homeDir;

    @Option(name = "--groupId", required = true)
    private String groupId;

    @Option(name = "--name", required = true, description = "The name of the package")
    private String packageName;

    @Option(name = "--r-source-directory", description = "Override the location of R sources")
    private String sourceDirectory;

    private PackageSource source;

    @Inject
    private DefaultPackageList defaultPackages;

    @Override
    public BuildLogger getLogger() {
        return new SimpleLogger();
    }

    @Override
    public void setupNativeCompilation() {
    }

    @Override
    public File getGccBridgePlugin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getGnuRHomeDir() {
        return homeDir;
    }

    @Override
    public File getUnpackedIncludesDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getOutputDir() {
        return new File("build/namespace");
    }

    @Override
    public File getCompileLogDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getPackageOutputDir() {
        return new File("build/namespace/" +
            source.getGroupId().replace('.', '/') + "/" +
            source.getPackageName());
    }

    @Override
    public PackageLoader getPackageLoader() {
        return new ClasspathPackageLoader(getClassLoader());
    }

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    @Override
    public List<String> getDefaultPackages() {
        return defaultPackages.getList();
    }

    public void compile() throws IOException {

        source = new PackageSource.Builder(new File("."))
            .setDefaultGroupId(groupId)
            .setPackageName(packageName)
            .setSourceDir(findRSourceDirectory())
            .setNativeSourceDir(new File("src"))
            .setDataDir(new File("data"))
            .setDescription(PackageDescription.fromFile(new File("DESCRIPTION")))
            .build();

        getPackageOutputDir().mkdirs();

        PackageBuilder builder = new PackageBuilder(source, this);
        builder.writePackageName();
        builder.compileNamespace();
        builder.compileDatasets();
    }

    private File findRSourceDirectory() {
        if(this.sourceDirectory == null) {
            return new File("R");
        } else {
            return new File(this.sourceDirectory);
        }
    }

    @Override
    public Map<String, String> getPackageGroupMap() {
        return Collections.emptyMap();
    }

    public static void main(String[] args) throws IOException {

        SingleCommand<GnurPackageBuilder> command =
            SingleCommand.singleCommand(GnurPackageBuilder.class);

        GnurPackageBuilder compiler;
        try {
            compiler = command.parse(args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.err.println();
            Help.help(command.getCommandMetadata());

            System.exit(-1);
            return;
        }

        compiler.compile();
    }
}
