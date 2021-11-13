package uk.louistarvin.sherlock.module.model.extra;

import uk.ac.warwick.dcs.sherlock.api.model.detection.PairwiseDetector;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.PreProcessingStrategy;
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.VariableExtractor;

public class ModuleExampleDetector extends PairwiseDetector<ModuleExampleDetectorWorker> {

	public ModuleExampleDetector() {
		super("Module Example Detector", ModuleExampleDetectorWorker.class, PreProcessingStrategy.of("variables", VariableExtractor.class));
	}
}

