package com.dslplatform.compiler.plugin;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.DslPath;
import com.dslplatform.compiler.client.parameters.Targets;
import com.dslplatform.compiler.client.parameters.Targets.Option;
import com.dslplatform.compiler.client.parameters.build.BuildAction;

import java.io.File;
import java.util.*;

/**
 2 additional parameters:

 	source - should there be source?

 only source
 source: generate sources (because not present in targets)
 source: format sources
 source: copy sources to src/generated/java
 java -jar dsl-clc.jar … -source:java_client=src/generated/java

 target + source example
 target: generate sources
 target: compile sources (jar)
 source: format sources in tmp (java_client)
 source: compile sources (jar)
 source: copy sources to src/generated/java
 java -jar dsl-clc.jar … -target=revenj,java_client -source:java_client=src/generated/java

 	pack-source - package formatted sources (phar (PHP), zip (C#), jar (java, scala))

 target + pack-sources example
 target: generate sources
 target: compile java_client & revenj
 pack-sources: format sources (java_client)
 pack-sources: compile sources (jar)
 pack-sources: archive sources from tmp (java_client) to lib/foo-source.jar
 java -jar dsl-clc.jar … -target=revenj,java_client -pack-sources:java_client=lib/foo-source.jar -pack-sources:php_ui=models/generated-ui.phar

 */
public class SourcePlugin implements CompileParameter, ParameterParser {

	public final static SourcePlugin INSTANCE = new SourcePlugin();

	@Override
	public String getAlias() { return "source"; }

	@Override
	public String getUsage() { return "options"; }

	static final String CACHE_NAME = "source_option_cache";

	/**
	 * Called after parse to check the validity of arguments.
	 * @param context
	 * @return
	 * @throws ExitException
	 */
	@Override
	public boolean check(final Context context) throws ExitException {
		final List<String> targets = new ArrayList<String>();
		final Set<String> distinctSources = new HashSet<String>();
		/* Check if source was among parameters currently stored in context */
		if (context.contains(INSTANCE)) {
			final String value = context.get(INSTANCE);
			if (value == null || value.length() == 0) {
				context.error("Source not provided. ");
				listOptions(context);
				return false;
			}
			/* Split the value part for chosen sources. */
			for (final String t : value.split(",")) {
				if (distinctSources.add(t.toLowerCase())) {
					targets.add(t);
				}
			}
		}
		/* No targets chosen throw message and exit */
		if (targets.size() == 0) {
			if (context.contains(INSTANCE)) {
				context.error("Source not provided. ");
				listOptions(context);
				return false;
			}
			return true;
		}
		/* Check it targets are valid, and accumulate the target options for source*/
		final List<Option> options = new ArrayList<Option>(targets.size());
		for (final String name : targets) {
			final Option o = Option.from(name);
			if (o == null || FormattedSourceBuild.from(o) != null) {
				context.error("Unknown source, or not supported: " + name);
				listOptions(context);
				return false;
			}
			options.add(o);
		}
		/* Check if DSL was provided */
		final Map<String, String> dsls = DslPath.getCurrentDsl(context);
		if (dsls.size() == 0) {
			context.error("Can't compile DSL to targets since no DSL was provided.");
			context.error("Please check your DSL folder: " + context.get(DslPath.INSTANCE));
			return false;
		}
		/*
			Preform target build action check for building the chosen source(target) options.
			At this point in code, tryParse has already been processed.
			TryParse has switched target builds for custom build implementations.
			So checks preformed should be on custom builds.
		*/
		for (final Option o : options) {
			if (!o.getAction().check(context)) {
				return false;
			}
		}
		/* Add them to cache so they can be later loaded from run */
		context.cache(CACHE_NAME, options);

		return true;
	}

	/**
	 * Called with parsed and validated arguments.
	 */
	@Override
	public void run(final Context context) throws ExitException {
		final List<Option> sources = context.load(CACHE_NAME);
		if (sources == null) return;
		final List<Option> targets = context.load(Targets.CACHE_NAME);
		final List<Option> notargetsources = new LinkedList<Option>();

		for (final Option source : sources) {
			/*
				If target action is called, i.e. target parameter was specified for this language
				then this action will be preformed as target, so no need to call build.
				Only build for languages (Target.Options) with source only specified will be called here.
			*/
			if (targets.contains(source)) continue;
			notargetsources.add(source);
		}
		Targets.INSTANCE.compileOffline(context, notargetsources);
	}

	static String sourceOptionCache(final String value) {
		return "source:" + value;
	}
	/**
	 * Called for CompileParameters which also implement ParameterParser (dsl-clc:Main:113)
	 */
	@Override
	public Either<Boolean> tryParse(final String name, final String value, final Context context) {
		System.out.println(name + value);
		System.out.println("tryParse");
		for (final Option o : Option.values()) {
			final String sourceOptionCache = sourceOptionCache(o.value);
			if (sourceOptionCache.equalsIgnoreCase(name)) {
				if (value == null || value.length() == 0) {
					return Either.fail("Target source parameter detected, but it's missing path as argument. Parameter: " + name);
				}
				final File path = new File(value);
				if (path.exists() && !path.isDirectory()) {
					return Either.fail("Target source path found, but it's not a directory. Parameter: " + name);
				}
				final BuildAction build = o.getAction();
				o.setAction(FormattedSourceBuild.from(o, build));
				context.put(sourceOptionCache, value);
				return Either.success(true);
			}
		}
		return Either.success(false);
	}

	@Override
	public String getShortDescription() {
		return "Format sources.";
	}

	@Override
	public String getDetailedDescription() {
		return "If you want to version the DSL compiler source output, you can format the files using this option.";
	}

	private static void listOptions(final Context context) {
		StringBuilder sb = new StringBuilder();
		final FormattedSourceBuild[] values = FormattedSourceBuild.values();
		sb.append(values[0]);
		for (int i = 1; i < values.length; ++i) sb.append(", ").append(values[i].targetOption.value);
		context.show("Available sources:", sb.toString());

		context.show("Example usages:");
		context.show("	-source=java_client,revenj");
		context.show("	-java_client -revenj=./model/SeverModel.dll");
	}

}