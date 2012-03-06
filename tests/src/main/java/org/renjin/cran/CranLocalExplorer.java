package org.renjin.cran;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

public class CranLocalExplorer {

	private static final Logger LOGGER = Logger.getLogger(CranLocalExplorer.class.getName());
	
	public void accept(File folder, CranVisitor cranVisitor) {
		
		for(File archive : folder.listFiles()) {
			if(archive.getName().endsWith(".tar.gz")) {
				try {
					String packageName = parsePackageRoot(archive);
					CranPackageVisitor packageVisitor = cranVisitor.visitPackage(packageName);
					if(packageVisitor != null) {
						visitSourceArchive(packageName, archive, packageVisitor);
					}
				} catch(Exception e) {
					LOGGER.log(Level.WARNING, "Exception caught while visiting package '" + archive.getName() + "'", e);
				}
			}
		}
		cranVisitor.visitComplete();
	}
	
	private void visitSourceArchive(String packageName, File archive, CranPackageVisitor visitor) throws IOException {
		FileInputStream in = new FileInputStream(archive);
		GZIPInputStream gzipIn = new GZIPInputStream(in);
		TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn);
				
		TarArchiveEntry entry;
		while((entry=tarIn.getNextTarEntry()) != null) {
			if(entry.getName().endsWith(".Rd")) {
				visitor.visitRdFile(entry.getName(), new TarEntryReaderSupplier(tarIn));
				
			} else if(entry.getName().startsWith(packageName + "/src/") &&
					entry.getSize() != 0) {
				visitor.visitNativeSource(entry.getName(), new TarEntryReaderSupplier(tarIn));
				
			} else if(entry.getName().startsWith(packageName + "/R/") && entry.getSize() != 0) {
				visitor.visitRSource(entry.getName(), new TarEntryReaderSupplier(tarIn));
				
			} else if(entry.getName().equals(packageName + "/DESCRIPTION")) {
				visitor.visitDescription(PackageDescription.fromInputStream(tarIn));
			}
		}
		
		visitor.visitComplete();
	}

	private class TarEntryReaderSupplier implements InputSupplier<Reader> {

		private TarArchiveInputStream in;
		private boolean opened = false;
		
		public TarEntryReaderSupplier(TarArchiveInputStream in) {
			super();
			this.in = in;
		}

		@Override
		public Reader getInput() throws IOException {
			if(opened) {
				throw new UnsupportedOperationException("opening multiple times not yet impl");
			}
			opened = true;
			return new InputStreamReader(in);
		}	
	}
	

	private String parsePackageRoot(File archive) {
		String name = archive.getName();
		int underscore = name.indexOf('_');
		if(underscore == -1) {
			throw new IllegalArgumentException(archive.getName());
		}
		return name.substring(0, underscore);
	}
	
	
}
