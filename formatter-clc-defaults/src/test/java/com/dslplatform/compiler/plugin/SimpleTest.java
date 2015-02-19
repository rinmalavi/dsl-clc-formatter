package com.dslplatform.compiler.plugin;

import com.dslplatform.compiler.client.Main;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class SimpleTest {

	/* Tests expect you to have credentials here */
	private final static String livePropsPath = System.getProperty("user.home") + "/.config/dsl-compiler-client/dsl-clc-live.props";
	private final String dslResourcePath = Thread.currentThread().getContextClassLoader().getResource("dsl").getPath();
	private final File tmp = new File(System.getProperty("java.io.tmpdir"), "dsl-test");

	@Before
	public void setUp() {
		if (!new File(livePropsPath).exists())
			throw new RuntimeException("Missing dsl-platform credentials at " + livePropsPath);
	}

	@Test
	public void CallSourceTest() throws URISyntaxException, IOException {
		final File currentTest = new File(tmp, "" + System.currentTimeMillis());
		final File generatedJavaPath = new File(currentTest, "java");

		final String[] args = {
				"-properties=" + livePropsPath,
				"-dsl=" + dslResourcePath,
				"-source:java_client=" + generatedJavaPath.getAbsolutePath(),
				"-log"
		};
		Main.main(args);
		assertTrue(new File(generatedJavaPath, "model/Guards.java").exists());
	}

	@Test
	public void CallOnlyBuildSpec() throws URISyntaxException, IOException {
		final File currentTest = new File(tmp, "" + System.currentTimeMillis());
		currentTest.mkdir();
		final File generatedJavaJar = new File(currentTest, "java.jar");

		final String[] args = {
				"-log",
				"-download",
				"-properties=" + livePropsPath,
				"-dsl=" + dslResourcePath,
				"-java_client=" + generatedJavaJar.getAbsolutePath(),
				"-log"
		};
		Main.main(args);
		assertTrue(new File(generatedJavaJar.getAbsolutePath()).exists());
	}

	@Test
	public void CallSourceAndBuildSpec() throws URISyntaxException, IOException {
		final File currentTest = new File(tmp, "" + System.currentTimeMillis());
		currentTest.mkdir();
		final File generatedJavaPath = new File(currentTest, "java");
		final File generatedJavaJar = new File(currentTest, "java.jar");

		final String[] args = {
				"-log",
				"-download",
				"-properties=" + livePropsPath,
				"-dsl=" + dslResourcePath,
				"-source:java_client=" + generatedJavaPath.getAbsolutePath(),
				"-java_client=" + generatedJavaJar.getAbsolutePath(),
				"-log"
		};
		Main.main(args);
		assertTrue(new File(generatedJavaPath, "model/Guards.java").exists());
		assertTrue(new File(generatedJavaJar.getAbsolutePath()).exists());
	}

}