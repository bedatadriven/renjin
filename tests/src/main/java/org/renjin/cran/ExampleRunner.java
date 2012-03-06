package org.renjin.cran;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import org.renjin.cran.PackageDescription.PackageDependency;

import com.google.common.collect.Lists;
import com.google.common.io.InputSupplier;

public class ExampleRunner extends CranVisitor {

	public static void main(String[] args) {
		new CranLocalExplorer().accept(CranIntegrationTests.getPackageRoot(), new ExampleRunner());
	}
	
	@Override
	public CranPackageVisitor visitPackage(String name) {
		return new PackageVisitor();

	}
	
	private class PackageVisitor extends CranPackageVisitor {
		
		private Iterable<PackageDependency> dependencies;
		private List<Example> examples = Lists.newArrayList();
		
		@Override
		protected void visitDescription(PackageDescription description) {
			this.dependencies = description.getDepends();
		}

		@Override
		protected void visitRdFile(String fileName, InputSupplier<Reader> in) throws IOException {
			RDocCrawler.crawl(in, new ExamplesCollector(examples));
		}

		@Override
		protected void visitComplete() {
			
			if(resolveDependencies()) {
				System.out.println("Deps resolved!");
				
				
			}	
		}

		private boolean resolveDependencies() {
			for(PackageDependency dep : dependencies) {
				if(dep.getName().equals("R")) {
					// ok
				} else {
					System.out.println("can't resolve dependency " + dep);
					return false;
				}
			}
			return true;
		}
		
		
	}
	
	private static class Example {
		private String name;
		private String code;
		
		public Example(String name, String code) {
			super();
			this.name = name;
			this.code = code;
		}
	}
	
	private class ExamplesCollector extends RDocVisitor {

		private Collection<Example> examples;
		private String currentName;
		
		public ExamplesCollector(Collection<Example> examples) {
			super();
			this.examples = examples;
		}

		@Override
		public void visitNameTag(String value) {
			this.currentName = value;
		}

		@Override
		public void visitExamples(String code) {
			examples.add(new Example(currentName, code));
		}
	}	
}
