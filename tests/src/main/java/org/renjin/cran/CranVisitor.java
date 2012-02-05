package org.renjin.cran;

public abstract class CranVisitor {
	
	public abstract CranPackageVisitor visitPackage(String name);
	
}
