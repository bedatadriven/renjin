package org.renjin.cran;

import java.io.File;
import java.io.IOException;


public class CranIntegrationTests {

	public static void main(String[] args) throws IOException {
		
		File destFolder = getPackageRoot();
		destFolder.mkdirs();
		
		for(CranPackage cranPackage : CRAN.fetchPackageList()) {
			if(aVersionAlreadyExists(cranPackage, destFolder)) {
				System.out.println(cranPackage.getName() + ": have a version already");
			} else {
				CRAN.downloadSrc(cranPackage, destFolder);
			}
		}
	}

	private static boolean aVersionAlreadyExists(CranPackage cranPackage,
			File destFolder) {
		for(File file : destFolder.listFiles()) {
			if(file.getName().startsWith(cranPackage.getName() + "_") && 
					file.getName().endsWith(".tar.gz")) {
				return true;
			}
		}
		return false;
	}

	public static File getPackageRoot() {
		return new File(getRenjinHome(), "cran-packages");
	}

	public static File getRenjinHome() {
		return new File(System.getProperty("user.home"), "renjin");
	}
}
