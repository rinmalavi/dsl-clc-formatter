package com.dslplatform.compiler.plugin;

import com.dslplatform.compiler.client.Main;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class SimpleTest {

	/* Tests expect you to have the credentials on this path. */
	private final static String livePropsPath = System.getProperty("user.home") + "/.config/dsl-compiler-client/dsl-clc-live.props";
	private final String dslResourcePath = Thread.currentThread().getContextClassLoader().getResource("dsl").getPath();
	private final File tmp = new File(System.getProperty("java.io.tmpdir"), "dsl-test");
	private final File tmpJavaLib = new File(tmp, "javaLib");

	@Before
	public void setUp() {
		if (!new File(livePropsPath).exists())
			throw new RuntimeException("Missing dsl-platform credentials at " + livePropsPath);
	}

	@Test
	public void SourceCallTest() throws URISyntaxException, IOException {
		final File currentTest = new File(tmp, "SourceCallTest" + System.currentTimeMillis());
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
	public void BuildCallSpec() throws URISyntaxException, IOException {
		final File currentTest = new File(tmp, "BuildCallSpec" + System.currentTimeMillis());
		currentTest.mkdir();
		final File generatedJavaJar = new File(currentTest, "java.jar");

		final String[] args = {
				"-log",
				"-download",
				"-properties=" + livePropsPath,
				"-dsl=" + dslResourcePath,
				"-dependencies:java_client=" + tmpJavaLib.getAbsolutePath(),
				"-java_client=" + generatedJavaJar.getAbsolutePath(),
				"-log"
		};
		Main.main(args);
		assertTrue(new File(generatedJavaJar.getAbsolutePath()).exists());
	}

	@Test
	public void BuildCallWithDefaultTarget1Spec() throws URISyntaxException, IOException {
		final File currentTest = new File(tmp, "BuildCallSpec" + System.currentTimeMillis());
		currentTest.mkdir();
		final File generatedJavaJar = new File(currentTest, "java.jar");

		final String[] args = {
				"-log",
				"-download",
				"-properties=" + livePropsPath,
				"-dsl=" + dslResourcePath,
				"-dependencies:java_client=" + tmpJavaLib.getAbsolutePath(),
				"-target=java_client",
				"-log"
		};
		Main.main(args);
		final File file = new File("generated-model-java.jar");
		assertTrue(file.exists());
		file.delete();
	}

	@Test
	public void BuildCallWithDefaultTarget2Spec() throws URISyntaxException, IOException {
		final File currentTest = new File(tmp, "BuildCallSpec" + System.currentTimeMillis());
		currentTest.mkdir();
		final File generatedJavaJar = new File(currentTest, "java.jar");

		final String[] args = {
				"-log",
				"-download",
				"-properties=" + livePropsPath,
				"-dsl=" + dslResourcePath,
				"-dependencies:java_client=" + tmpJavaLib.getAbsolutePath(),
				"-java_client",
				"-log"
		};
		Main.main(args);
		final File file = new File("generated-model-java.jar");
		assertTrue(file.exists());
		file.delete();
	}

	@Test
	public void SourceAndBuildSpec() throws URISyntaxException, IOException {
		final File currentTest = new File(tmp, "SourceAndBuildSpec" + System.currentTimeMillis());
		currentTest.mkdir();
		final File generatedJavaPath = new File(currentTest, "java");
		final File generatedJavaJar = new File(currentTest, "java.jar");

		final String[] args = {
				"-log",
				"-download",
				"-properties=" + livePropsPath,
				"-dsl=" + dslResourcePath,
				"-dependencies:java_client=" + tmpJavaLib.getAbsolutePath(),
				"-source:java_client=" + generatedJavaPath.getAbsolutePath(),
				"-java_client=" + generatedJavaJar.getAbsolutePath(),
				"-log"
		};
		Main.main(args);
		assertTrue(new File(generatedJavaPath, "model/Guards.java").exists());
		assertTrue(new File(generatedJavaJar.getAbsolutePath()).exists());
	}

}