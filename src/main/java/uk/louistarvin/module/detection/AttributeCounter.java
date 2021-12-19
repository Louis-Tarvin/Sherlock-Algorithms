package uk.louistarvin.sherlock.module.model.extra.detection;

import uk.ac.warwick.dcs.sherlock.api.annotation.EventHandler;
import uk.ac.warwick.dcs.sherlock.api.annotation.SherlockModule;
import uk.ac.warwick.dcs.sherlock.api.event.EventInitialisation;
import uk.ac.warwick.dcs.sherlock.api.event.EventPreInitialisation;
import uk.ac.warwick.dcs.sherlock.api.model.detection.DetectionType;
import uk.ac.warwick.dcs.sherlock.api.registry.SherlockRegistry;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaLexer;
import uk.louistarvin.sherlock.module.model.extra.postprocessing.AttributeCountPostProcessor;
import uk.louistarvin.sherlock.module.model.extra.postprocessing.AttributeCountRawResult;
import uk.louistarvin.sherlock.module.model.extra.preprocessing.TokenCounts;
import uk.louistarvin.sherlock.module.model.extra.preprocessing.TokenCountsJava;
import uk.louistarvin.sherlock.module.model.extra.preprocessing.VarsDeclaredAndUsed;
import uk.louistarvin.sherlock.module.model.extra.preprocessing.VarsDeclaredAndUsedJava;

@SherlockModule
public class AttributeCounter {

	@EventHandler
	public void preInitialisation(EventPreInitialisation event) {
		SherlockRegistry.registerLanguage("Java", JavaLexer.class);

		SherlockRegistry.registerAdvancedPreProcessorGroup(TokenCounts.class);
		SherlockRegistry.registerAdvancedPreProcessorGroup(VarsDeclaredAndUsed.class);
	}

	@EventHandler
	public void initialisation(EventInitialisation event) {
		SherlockRegistry.registerDetectionType(new DetectionType("ATTRIBUTE_COUNT", "Counted Attributes", 
					"This file has had attributes counted. This does not necessarily mean the code has been plagiarised", 1.0));

		SherlockRegistry.registerAdvancedPreProcessorImplementation("uk.louistarvin.sherlock.module.model.extra.preprocessing.TokenCounts", TokenCountsJava.class);
		SherlockRegistry.registerAdvancedPreProcessorImplementation("uk.louistarvin.sherlock.module.model.extra.preprocessing.VarsDeclaredAndUsed", VarsDeclaredAndUsedJava.class);

		SherlockRegistry.registerDetector(AttributeCountDetector.class);
		SherlockRegistry.registerPostProcessor(AttributeCountPostProcessor.class, AttributeCountRawResult.class);
	}

}
