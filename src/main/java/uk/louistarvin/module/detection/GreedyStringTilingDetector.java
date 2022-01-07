
package uk.louistarvin.module.detection;

import uk.louistarvin.module.preprocessing.Tokenizer;
import uk.ac.warwick.dcs.sherlock.api.model.detection.PairwiseDetector;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.PreProcessingStrategy;

public class GreedyStringTilingDetector extends PairwiseDetector<GreedyStringTilingDetectorWorker> {

	public GreedyStringTilingDetector() {
		super("GST", GreedyStringTilingDetectorWorker.class, PreProcessingStrategy.of("tokens", true, Tokenizer.class));
	}
	
}