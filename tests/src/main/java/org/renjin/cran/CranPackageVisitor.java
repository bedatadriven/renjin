package org.renjin.cran;

import java.io.Reader;

import com.google.common.io.InputSupplier;

public class CranPackageVisitor  {

	

	protected void visitDescription(PackageDescription description) {
		
	}

	protected void visitNativeSource(String fileName, InputSupplier<Reader> in) {
		
	}

	protected void visitRdFile(String fileName, InputSupplier<Reader> in) {
		
	}
	
	protected void visitRSource(String fileName, InputSupplier<Reader> in) {
		
	}
}
