package uk.louistarvin.module.detection;

import uk.ac.warwick.dcs.sherlock.api.annotation.EventHandler;
import uk.ac.warwick.dcs.sherlock.api.annotation.SherlockModule;
import uk.ac.warwick.dcs.sherlock.api.event.EventInitialisation;
import uk.ac.warwick.dcs.sherlock.api.event.EventPreInitialisation;
import uk.ac.warwick.dcs.sherlock.api.model.detection.DetectionType;
import uk.ac.warwick.dcs.sherlock.api.registry.SherlockRegistry;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaLexer;
import uk.louistarvin.module.postprocessing.GreedyStringTilingPostProcessor;
import uk.louistarvin.module.postprocessing.GreedyStringTilingRawResult;
import uk.louistarvin.module.preprocessing.Tokenizer;

@SherlockModule
public class GreedyStringTiling {

    @EventHandler
	public void preInitialisation(EventPreInitialisation event) {
		SherlockRegistry.registerLanguage("Java", JavaLexer.class);

		SherlockRegistry.registerGeneralPreProcessor(Tokenizer.class);
	}

	@EventHandler
	public void initialisation(EventInitialisation event) {
		SherlockRegistry.registerDetectionType(new DetectionType("GST_MATCH", "Token Match", 
					"These code blocks contain identical sequences of tokens", 1.0));

		SherlockRegistry.registerDetector(GreedyStringTilingDetector.class);
		SherlockRegistry.registerPostProcessor(GreedyStringTilingPostProcessor.class, GreedyStringTilingRawResult.class);
	}
}
