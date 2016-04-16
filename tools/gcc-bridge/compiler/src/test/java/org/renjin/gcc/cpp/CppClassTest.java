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
		compile("cppclass.cpp");

		Class<?> clazz = Class.forName("org.renjin.gcc.cppclass");
		Method method = clazz.getMethod("create");

		Object rect = method.invoke(null);
		assertThat(rect, is(not(nullValue())));
	}
}