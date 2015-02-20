package com.dslplatform.compiler.plugin;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Targets;
import com.dslplatform.compiler.client.parameters.Targets.Option;

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
public class PluginRunner implements CompileParameter, ParameterParser {

	public final static PluginRunner INSTANCE = new PluginRunner();

	@Override
	public String getAlias() {
		return "asd";
	}

	@Override
	public String getUsage() {
		return null;
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		return true;
	}

	/**
	 * Called with parsed and validated arguments.
	 */
	@Override
	public void run(final Context context) throws ExitException {
		final List<Targets.Option> sourceOptions = context.load(SourcePlugin.CACHE_NAME);
		final List<Targets.Option> packSourceOptions = context.load(PackSourcePlugin.CACHE_NAME);
		if ((sourceOptions == null || sourceOptions.isEmpty()) && (packSourceOptions == null || packSourceOptions.isEmpty())) return;
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
			if (notargetsources.size() > 0)
				Targets.INSTANCE.compile(context, notargetsources);
		}

	}

	/**
	 * Called for CompileParameters which also implement ParameterParser (dsl-clc:Main:113)
	 */
	@Override
	public Either<Boolean> tryParse(final String name, final String value, final Context context) {
		return Either.success(false);
	}

	@Override
	public String getShortDescription() {
		return "Just a runner.";
	}

	@Override
	public String getDetailedDescription() {
		return "Just a runner.";
	}

}