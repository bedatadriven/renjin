package org.renjin.gcc.gimple;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class GimpleParserTest {

	@Test
	public void swilk() throws IOException {
		
		InputStream in = getClass().getResourceAsStream("swilk.c.143t.optimized");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		GimpleParser parser = new GimpleParser();
		List<GimpleFunction> functions = parser.parse(reader);
		
		for(GimpleFunction fn : functions) {
			System.out.println(fn.toString());
		}
	}
	
}
