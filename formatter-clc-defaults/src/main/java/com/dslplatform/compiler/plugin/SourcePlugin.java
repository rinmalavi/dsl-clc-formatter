package com.dslplatform.compiler.plugin;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.DslPath;
import com.dslplatform.compiler.client.parameters.Targets;
import com.dslplatform.compiler.client.parameters.Targets.Option;
import com.dslplatform.compiler.client.parameters.build.BuildAction;

import java.io.File;
import java.util.*;

/**
 * Source parameter, if set, source will be downloaded.
 * Formatted with language specific formatter.
 * Finally it will be moved to specified location.
 * <p>
 * Example:
 * java -jar dsl-clc.jar \
 * -u=username \
 * -p=password \
 * -target=java_client \
 * -sources:java_client=gen
 */
public class SourcePlugin implements CompileParameter, ParameterParser {

	public final static SourcePlugin INSTANCE = new SourcePlugin();

	private List<Option> sourceOptions = new LinkedList<Option>();

	@Override
	public String getAlias() {
		return "source";
	}

	@Override
	public String getUsage() {
		return "options";
	}

	static final String CACHE_NAME = "source_option_cache";

	/**
	 * Called after parse to check the validity of arguments.
	 *
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

		for (final Option o : Option.values()) {
			final String lc = o.value.toLowerCase();
			if (context.contains(sourceOptionCache(o.value)) && !distinctSources.contains(lc)) {
				targets.add(o.value);
				distinctSources.add(lc);
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
			if (o == null || FormattedSourceBuild.from(o) == null) {
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
			TODO:
			Preform target build action check for building the chosen source(target) options.
			At this point in code, tryParse has already been processed.
			TryParse has switched target builds for custom build implementations.
			So checks preformed should be on custom builds.

		/* Add them to cache so they can be later loaded from run
		 	Actually local variable sourceOptions are used here instead.
		 	But this how usually it happens in dsl-clc so I'll keep this comment here will comments are moved there.
		 */
		context.cache(CACHE_NAME, options);

		return true;
	}

	/**
	 * Called with parsed and validated arguments.
	 */
	@Override
	public void run(final Context context) throws ExitException {
		if (sourceOptions.isEmpty()) return;
		final List<Option> targets = context.load(Targets.CACHE_NAME);
		if (targets == null) {
			Targets.INSTANCE.compile(context, sourceOptions);
		} else {
			final List<Option> notargetsources = new LinkedList<Option>();
			for (final Option source : sourceOptions) {
				/*
					If target action is called, i.e. target parameter was specified for this language
					then this action will be preformed as target, so no need to call build.
					Only build for languages (Target.Options) with source only specified will be called here.
				*/
				if (targets.contains(source)) continue;
				notargetsources.add(source);

			}
			Targets.INSTANCE.compile(context, notargetsources);
		}

	}

	static String sourceOptionCache(final String value) {
		return "source:" + value;
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
					return Either.fail("Target source parameter detected, but it's missing path as argument. Parameter: " + name);
				}
				final File path = new File(value);
				if (path.exists() && !path.isDirectory()) {
					return Either.fail("Target source path found, but it's not a directory. Parameter: " + name);
				}
				/*  Usually this is held in context cache.  */
				sourceOptions.add(o);
				/*
					Switch action for a custom one,
					pass a regular build so it can be called for target parameter.
				*/
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