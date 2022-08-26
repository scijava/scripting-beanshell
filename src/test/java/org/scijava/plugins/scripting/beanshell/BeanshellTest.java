/*
 * #%L
 * JSR-223-compliant BeanShell scripting language plugin.
 * %%
 * Copyright (C) 2011 - 2022 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.plugins.scripting.beanshell;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.script.AbstractScriptLanguageTest;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptModule;
import org.scijava.script.ScriptService;

import bsh.BshScriptEngine;

/**
 * Unit tests for the BeanShell support.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class BeanshellTest extends AbstractScriptLanguageTest {

	private Context context;
	private ScriptService scriptService;

	@Before
	public void setUp() {
		context = new Context(ScriptService.class);
		scriptService = context.getService(ScriptService.class);

	}

	@After
	public void tearDown() {
		context.dispose();
		context = null;
		scriptService = null;
	}

	@Test
	public void testDiscovery() {
		assertDiscovered(BeanshellScriptLanguage.class);
	}

	@Test
	public void testBasic() throws InterruptedException, ExecutionException {
		final String script = "x = 1 + 2;";
		final ScriptModule m = scriptService.run("add.bsh", script, true).get();
		final Integer result = (Integer) m.getReturnValue();
		assertEquals(3, result.intValue());
	}

	@Test
	public void testLocals() throws ScriptException {
		final ScriptLanguage language = scriptService.getLanguageByExtension("bsh");
		final ScriptEngine engine = language.getScriptEngine();
		assertEquals(BshScriptEngine.class, engine.getClass());
		engine.put("hello", 17);
		assertEquals("17", engine.eval("hello").toString());
		assertEquals("17", engine.get("hello").toString());

		final Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.clear();
		assertEquals(null, engine.get("hello"));
	}

	@Test
	public void testParameters() throws InterruptedException, ExecutionException {
		final String script = "" + //
			"#@ ScriptService ss\n" + //
			"#@output String language\n" + //
			"language = ss.getLanguageByName(\"BeanShell\").getLanguageName();\n";
		final ScriptModule m = scriptService.run("hello.bsh", script, true).get();

		final Object actual = m.getOutput("language");
		final String expected =
			scriptService.getLanguageByName("BeanShell").getLanguageName();
		assertEquals(expected, actual);

		final Object result = m.getReturnValue();
		assertEquals(expected, result);
	}

	@Test
	public void testNull() throws InterruptedException, ExecutionException {
		final String script = "" + //
			"#@ ScriptService ss\n" + //
			"#@output String ternary\n" + //
			"a = null;\n" + //
			"ternary = a == null ? \"NULL\" : \"HUH?\";\n";
		final ScriptModule m = scriptService.run("nullTest.bsh", script, true).get();

		final Object actual = m.getOutput("ternary");
		final String expected = "NULL";
		assertEquals(expected, actual);

		final Object result = m.getReturnValue();
		assertEquals(expected, result);
	}
}
