package org.renjin.cran;

import java.io.File;
import java.io.IOException;

public class ExamplesPrinter extends CranPackageVisitor {

	@Override
	protected void visitRdFile(String name, String content) {
		System.out.println(name);
	}

	public static void main(String[] args) throws IOException {
		new ExamplesPrinter().visitPackages(new File("target/packages"));
	}
	
	
}
