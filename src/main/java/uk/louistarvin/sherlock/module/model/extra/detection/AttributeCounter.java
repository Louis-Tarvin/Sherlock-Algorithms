package uk.louistarvin.sherlock.module.model.extra.detection;

import uk.ac.warwick.dcs.sherlock.api.annotation.EventHandler;
import uk.ac.warwick.dcs.sherlock.api.annotation.SherlockModule;
import uk.ac.warwick.dcs.sherlock.api.event.EventInitialisation;
import uk.ac.warwick.dcs.sherlock.api.event.EventPreInitialisation;
import uk.ac.warwick.dcs.sherlock.api.registry.SherlockRegistry;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaLexer;
import uk.louistarvin.sherlock.module.model.extra.postprocessing.AttributeCountPostProcessor;
import uk.louistarvin.sherlock.module.model.extra.postprocessing.AttributeCountRawResult;
import uk.louistarvin.sherlock.module.model.extra.preprocessing.OperatorExtractor;
import uk.louistarvin.sherlock.module.model.extra.preprocessing.OperatorExtractorJava;

@SherlockModule
public class AttributeCounter {

	@EventHandler
	public void initialisation(EventInitialisation event) {
		SherlockRegistry.registerAdvancedPreProcessorImplementation("uk.louistarvin.sherlock.module.model.extra.preprocessing.OperatorExtractor", OperatorExtractorJava.class);

		SherlockRegistry.registerDetector(AttributeCountDetector.class);
		SherlockRegistry.registerPostProcessor(AttributeCountPostProcessor.class, AttributeCountRawResult.class);
	}

	@EventHandler
	public void preInitialisation(EventPreInitialisation event) {
		SherlockRegistry.registerLanguage("Java", JavaLexer.class);

		SherlockRegistry.registerAdvancedPreProcessorGroup(OperatorExtractor.class);
	}

}
