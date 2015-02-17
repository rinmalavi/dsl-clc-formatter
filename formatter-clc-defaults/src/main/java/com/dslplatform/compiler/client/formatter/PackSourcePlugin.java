package com.dslplatform.compiler.client.formatter;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.DslPath;
import com.dslplatform.compiler.client.parameters.Targets.Option;
import com.dslplatform.compiler.client.parameters.build.BuildAction;

import java.util.*;

/**
 Source parameter, if set, source will be downloaded and packed to
 specified jar or default one if name not provided

 Example:
 java -jar dsl-clc.jar \
 	-u=username \
 	-p=password \
 	-target=revenj,java_client \
 	-pack-sources:java_client=lib/foo-source.jar \
 	-pack-sources:php_ui=models/generated-ui.phar

 */
public enum PackSourcePlugin implements CompileParameter, ParameterParser {
	INSTANCE;

	@Override
	public String getAlias() { return "pack-source"; }

	@Override
	public String getUsage() { return "options"; }

	static final String CACHE_NAME = "pack_option_cache";

	/**
	 * Called after parse to check the validity of arguments.
	 * @param context
	 * @return
	 * @throws com.dslplatform.compiler.client.ExitException
	 */
	@Override
	public boolean check(final Context context) throws ExitException {
		final List<String> targets = new ArrayList<String>();
		final Set<String> distinctSources = new HashSet<String>();
		/* Check if source was among parameters currently stored in context */
		if (context.contains(INSTANCE)) {
			final String value = context.get(INSTANCE);
			if (value == null || value.length() == 0) {
				context.error("Source was not provided.");
				listOptions(context);
				return false;
			}
			/* Split the value part for chosen sources packing. */
			for (final String t : value.split(",")) {
				if (distinctSources.add(t.toLowerCase())) {
					targets.add(t);
				}
			}
		}
		/* If no targets chosen throw message and exit */
		if (targets.size() == 0) {
			if (context.contains(INSTANCE)) {
				context.error("Target languages no specified for packing. ");
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
			/*
				This functionality is preformed in the Plugin class.
			*/
	}

	static String sourceOptionCache(final String value) {
		return "pack-source:" + value;
	}

	/**
	 * Called for CompileParameters which also implement ParameterParser (dsl-clc:Main:113)
	 */
	@Override
	public Either<Boolean> tryParse(final String name, final String value, final Context context) {
		for (final Option o : Option.values()) {
			final String sourceOptionCache = sourceOptionCache(o.value);
			if (sourceOptionCache.equalsIgnoreCase(name)) {
				if (value == null || value.length() == 0) {
					return Either.fail("Target source package parameter detected, but it's missing path as argument. " +
							"Parameter: " + name);
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
		return "Packs sources to specified locations.";
	}

	@Override
	public String getDetailedDescription() {
		return
				"package formatted sources \n" +
						"phar (PHP)\n" +
						" zip (C#)\n" +
						" jar (java, scala))";
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