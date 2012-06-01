package org.renjin.maven;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.script.ScriptException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.renjin.RVersion;
import org.renjin.packaging.PackagingUtils;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;

/**
 * @goal package
 * @phase prepare-package
 * @requiresProject true
 */
public class PackageMojo extends AbstractMojo {


	/**
	 * Name of the R package
	 * @parameter expression="${project.artifactId}"
	 * @required
	 */
	private String packageName;

	/**
	 * Name of the R package
	 * @parameter expression="src/main/R"
	 * @required
	 */
	private File sourceDirectory;	

	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			getLog().info("Copying R sources");
			installPackageSources();
			getLog().debug("Installing metadata and enabling lazy loading");

			eval(String.format(
					"tools:::.install_package_description('%s', '%s')\n" +
					"tools:::makeLazyLoading('%s', lib.loc = '%s')\n",
							getProject().getBasedir(),
							getPackageTarget().getAbsolutePath(),
							packageName,
							getStagingDir()));
			
		} catch (IOException e) {
			throw new MojoExecutionException("building package failed", e);
		} catch (ScriptException e) {
			throw new MojoExecutionException("building package failed", e);
		}
	}

	private File getPackageTarget() {
		return new File(getStagingDir(), packageName);
	}

	/**
	 * Concatenates all R sources files into a single script in the package
	 * staging folder.
	 */
	private void installPackageSources() throws IOException, MojoExecutionException {

		if(!sourceDirectory.exists()) {
			throw new MojoExecutionException("R source directory does not exist");
		}

		List<File> sources = findSourceFiles();

		new File(getStagingDir(), packageName).mkdirs();

		copyDescription(packageName);

		if(getNamespaceFile().exists()) {
			Files.copy(getNamespaceFile(), new File(getPackageTarget(), "NAMESPACE"));
		}

		PackagingUtils.concatSources(sources, getStagingDir(), packageName);
	}


	private void copyDescription(String packageName) throws IOException {
		File dest = new File(getPackageTarget(), "DESCRIPTION");
		Files.copy(getDescriptionFile(), dest);
		Files.append(String.format("Build: R %s; ; %s; unix", RVersion.STRING, new Date().toString()),
				dest, Charsets.UTF_8);
	}

	private List<File> findSourceFiles() {
		List<File> srcFiles = Lists.newArrayList();
		for(File file : sourceDirectory.listFiles()) {
			if(file.getName().toLowerCase().endsWith(".r") ||
			   file.getName().toLowerCase().endsWith(".s")) {
				srcFiles.add(file);
			}
		}

		Collections.sort(srcFiles, Ordering.natural().onResultOf(new Function<File, Comparable<String>>() {
			@Override
			public Comparable<String> apply(File input) {
				return input.getName();
			}
		}));

		return srcFiles;
	}

	private void eval(String source) throws ScriptException, IOException {
		getLog().debug("Starting eval");
		RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
		RenjinScriptEngine engine = factory.withOptions().withNoDefaultPackages().get();
		getLog().debug("Created engine");

		engine.eval(source); 
		getLog().debug("Source evaled");

		engine.printWarnings();
	}
	
	private MavenProject getProject() {
		return (MavenProject) getPluginContext().get("project");
	}

	private File getStagingDir() {
		return new File(getProject().getBuild().getOutputDirectory());
	}
	
	private File getDescriptionFile() {
		return new File(getProject().getBasedir(), "DESCRIPTION");
	}
	
	private File getNamespaceFile() {
		return new File(getProject().getBasedir(), "NAMESPACE");
	}
}
