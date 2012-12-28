package org.renjin.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal convert
 */
public class CranMojo extends AbstractMojo {

	/**
	 * Name of the R package located in CRAN
	 * @parameter expression="${cran.package}"
	 * @required
	 */
	private String packageName;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		
	
		
		
	}	
}
