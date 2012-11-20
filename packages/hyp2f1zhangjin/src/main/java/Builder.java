import java.io.File;

import org.renjin.packaging.NativeSourcesCompiler;


public class Builder {

	
	public static void main(String[] args) throws Exception {
		
		NativeSourcesCompiler compiler = new NativeSourcesCompiler();
		compiler.setPackageName("hyp2f1zhangjin");
		compiler.addSources(new File("src/main/fortran"));
		compiler.compile();
	}
	
}
