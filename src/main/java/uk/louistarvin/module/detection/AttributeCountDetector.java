package uk.louistarvin.module.detection;

import uk.louistarvin.module.preprocessing.TokenCounts;
import uk.louistarvin.module.preprocessing.VarsDeclaredAndUsed;
import uk.ac.warwick.dcs.sherlock.api.model.detection.PairwiseDetector;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.PreProcessingStrategy;
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.CommentRemover;

public class AttributeCountDetector extends PairwiseDetector<AttributeCountDetectorWorker> {

	public AttributeCountDetector() {
		super("Attribute Counting", AttributeCountDetectorWorker.class, PreProcessingStrategy.of("comments", CommentRemover.class), PreProcessingStrategy.of("operators", TokenCounts.class), PreProcessingStrategy.of("variablesDeclaredAndUsed", VarsDeclaredAndUsed.class));
	}
	
}
