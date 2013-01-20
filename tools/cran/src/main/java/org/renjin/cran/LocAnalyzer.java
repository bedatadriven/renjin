package org.renjin.cran;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.Maps;
import com.google.common.io.InputSupplier;

public class LocAnalyzer extends CranVisitor {
	
	private static final Logger LOGGER = Logger.getLogger(LocAnalyzer.class.getName());
	
	public static void main(String[] args) {
		LocAnalyzer analyzer = new LocAnalyzer();
		CranLocalExplorer explorer = new CranLocalExplorer();
		explorer.accept(CranIntegrationTests.getPackageRoot(), analyzer);
		
		analyzer.print();
		
	}
	
	private void print() {
		for(String pkgName : packages.keySet()) {
			System.out.println(pkgName + ": " + packages.get(pkgName).loc);
		}
	}

	private Map<String, PackageVisitor> packages = Maps.newHashMap();

	@Override
	public CranPackageVisitor visitPackage(String name) {
		PackageVisitor visitor = new PackageVisitor();
		packages.put(name, visitor);
		return visitor;
	}
	
	private class PackageVisitor extends CranPackageVisitor {

		private Map<String, Integer> loc = Maps.newHashMap();
		
		@Override
		protected void visitRSource(String fileName, InputSupplier<Reader> in) throws IOException {
			addSource("R", in);
		}
		
		@Override
		protected void visitNativeSource(String fileName,
				InputSupplier<Reader> in) throws IOException {
			if(fileName.endsWith(".h")) {
				addSource("C", in);
			} else if(fileName.endsWith(".c")) {
				addSource("C", in);
			} else if(fileName.endsWith(".cpp")) {
				addSource("C++", in);
			} else if(fileName.endsWith(".cc")) {
				addSource("C++", in);
			} else if(fileName.endsWith(".f") || fileName.endsWith(".f95") ||
					fileName.endsWith(".f90")) {
				addSource("Fortran", in);
			} else {
				LOGGER.warning("native source file of unknown type: " + fileName);
			}
		}


		private void addSource(String language, InputSupplier<Reader> supplier) throws IOException {
			int count = countPhysicalLinesOfCode(supplier);
			if(loc.containsKey(language)) {
				loc.put(language, loc.get(language) + count);
			} else {
				loc.put(language, count);
			}
		}
		
		private int countPhysicalLinesOfCode(InputSupplier<Reader> supplier) throws IOException {
			BufferedReader reader = new BufferedReader(supplier.getInput());
			int count = 0;
			while(reader.readLine() != null) {
				count++;
			}
			return count;
		}
	}
}
