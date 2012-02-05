package org.renjin.cran;

import java.io.File;
import java.io.IOException;


public class CranIntegrationTests {

	public static void main(String[] args) throws IOException {
		
		File destFolder = new File("target/packages");
		destFolder.mkdirs();
		
		for(CranPackage cranPackage : CRAN.fetchPackageList()) {
			CRAN.downloadSrc(cranPackage, destFolder);
		}
		
		
	}
	
}
