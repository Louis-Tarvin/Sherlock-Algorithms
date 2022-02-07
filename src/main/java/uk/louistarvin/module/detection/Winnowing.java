package uk.louistarvin.module.detection;

import uk.ac.warwick.dcs.sherlock.api.annotation.EventHandler;
import uk.ac.warwick.dcs.sherlock.api.annotation.SherlockModule;
import uk.ac.warwick.dcs.sherlock.api.event.EventInitialisation;
import uk.ac.warwick.dcs.sherlock.api.event.EventPreInitialisation;
import uk.ac.warwick.dcs.sherlock.api.model.detection.DetectionType;
import uk.ac.warwick.dcs.sherlock.api.registry.SherlockRegistry;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaLexer;
import uk.louistarvin.module.postprocessing.WinnowingPostProcessor;
import uk.louistarvin.module.postprocessing.WinnowingRawResult;
import uk.louistarvin.module.preprocessing.WinnowingPreProcessor;

@SherlockModule
public class Winnowing {
    
    @EventHandler
	public void preInitialisation(EventPreInitialisation event) {
		SherlockRegistry.registerLanguage("Java", JavaLexer.class);

		SherlockRegistry.registerGeneralPreProcessor(WinnowingPreProcessor.class);
	}

	@EventHandler
	public void initialisation(EventInitialisation event) {
		SherlockRegistry.registerDetectionType(new DetectionType("WINNOWING_MATCH", "Winnowing Match", 
					"These code blocks contain identical sequences of K-grams", 1.0));

		SherlockRegistry.registerDetector(WinnowingDetector.class);
		SherlockRegistry.registerPostProcessor(WinnowingPostProcessor.class, WinnowingRawResult.class);
	}
}
