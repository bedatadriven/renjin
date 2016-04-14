package org.renjin.gcc.cpp;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;

import org.junit.Test;
import org.renjin.gcc.GimpleCompiler;
import org.renjin.gcc.codegen.lib.cpp.CppSymbolLibrary;
import org.renjin.gcc.gimple.GimpleCompilationUnit;

public class CppClassTest extends AbstractGccCppTest {

	@Test
	public void createClass() throws Exception {
		GimpleCompilationUnit unit = compileToGimple("cppclass.cpp");

		GimpleCompiler compiler = new GimpleCompiler();
		compiler.setOutputDirectory(new File("target/test-classes"));
		compiler.setPackageName("org.renjin.gcc");
		compiler.setVerbose(true);
		compiler.addMathLibrary();
		compiler.addLibrary(new CppSymbolLibrary());
//		compiler.addRecordClass("JvmRect", JvmRect.class);
//		compiler.addMethod("jvm_area", JvmRect.class, "area");
//		compiler.addMethod("jvm_areas", JvmRect.class, "areas");

		compiler.compile(Collections.singletonList(unit));

		Class<?> clazz = Class.forName("org.renjin.gcc.cppclass");
		Method method = clazz.getMethod("create");

		Object rect = method.invoke(null);
		assertThat(rect, is(not(nullValue())));
//		method = rect.getClass().getMethod("volume");
//		Object volume = method.invoke(rect);
//		assertThat(volume, is((Object)Integer.valueOf(0)));
	}
}