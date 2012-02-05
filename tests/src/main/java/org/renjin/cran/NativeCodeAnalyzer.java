package org.renjin.cran;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class NativeCodeAnalyzer extends CranPackageVisitor {

	@Override
	protected void visitNativeSource(String fileName, InputStream in) {
		System.out.println("Native source found: " + fileName);
	}
	
	public static void main(String [] args) throws IOException {
		new NativeCodeAnalyzer().visitPackages(new File("target/packages"));
	}

}
