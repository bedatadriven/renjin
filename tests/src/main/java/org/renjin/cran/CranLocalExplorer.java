package org.renjin.cran;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

public class CranLocalExplorer {

	private static final Logger LOGGER = Logger.getLogger(CranLocalExplorer.class.getName());
	
	public void accept(File folder, CranVisitor cranVisitor) {
		
		for(File archive : folder.listFiles()) {
			if(archive.getName().endsWith(".tar.gz")) {
				try {
					String packageName = parsePackageRoot(archive);
					CranPackageVisitor packageVisitor = cranVisitor.visitPackage(packageName);
					if(packageVisitor != null) {
						
					}
				} catch(Exception e) {
					LOGGER.log(Level.WARNING, "Exception caught while visiting package '" + archive.getName() + "'", e);
				}
			}
		}
	}
	

	private void visitSourceArchive(File archive, CranPackageVisitor visitor) throws IOException {
		FileInputStream in = new FileInputStream(archive);
		GZIPInputStream gzipIn = new GZIPInputStream(in);
		TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn);
		
		String packageName = parsePackageRoot(archive);
		
//		TarArchiveEntry entry;
//		while((entry=tarIn.getNextTarEntry()) != null) {
//			if(entry.getName().endsWith(".Rd")) {
//				visitor.visitRdFile(tarIn, entry);
//			} else if(entry.getName().startsWith(packageName + "/src/") &&
//					entry.getSize() != 0) {
//				visitNativeSource(entry.getName(), tarIn);
//			} else if(entry.getName().equals(packageName + "/DESCRIPTION")) {
//				visitDescription(PackageDescription.fromInputStream(tarIn));
//			}
//		}
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
