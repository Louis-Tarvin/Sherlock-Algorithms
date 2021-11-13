package uk.louistarvin.sherlock.module.model.extra.detection;

import java.util.List;

import uk.louistarvin.sherlock.module.model.extra.postprocessing.AttributeCountRawResult;
import uk.ac.warwick.dcs.sherlock.api.model.detection.IDetector;
import uk.ac.warwick.dcs.sherlock.api.model.detection.ModelDataItem;
import uk.ac.warwick.dcs.sherlock.api.model.detection.PairwiseDetectorWorker;
import uk.ac.warwick.dcs.sherlock.api.util.IndexedString;

public class AttributeCountDetectorWorker extends PairwiseDetectorWorker<AttributeCountRawResult> {

	public AttributeCountDetectorWorker(IDetector parent, ModelDataItem file1Data, ModelDataItem file2Data) {
		super(parent, file1Data, file2Data);
	}

	@Override
	public void execute() {
		List<IndexedString> linesF1 = this.file1.getPreProcessedLines("comments");
		List<IndexedString> linesF2 = this.file2.getPreProcessedLines("comments");
		List<IndexedString> operatorsF1 = this.file1.getPreProcessedLines("operators");
		List<IndexedString> operatorsF2 = this.file2.getPreProcessedLines("operators");

		AttributeCountRawResult res = new AttributeCountRawResult(this.file1.getFile(), this.file2.getFile());

		// Set line counts
		res.setFile1Lines(linesF1.size());
		res.setFile2Lines(linesF2.size());

		// Set total operator counts
		res.setFile1TotalOperators(Integer.parseInt(operatorsF1.get(0).getValue()));
		res.setFile2TotalOperators(Integer.parseInt(operatorsF2.get(0).getValue()));

		// Set unique operator counts
		res.setFile1UniqueOperators(Integer.parseInt(operatorsF1.get(1).getValue()));
		res.setFile2UniqueOperators(Integer.parseInt(operatorsF2.get(1).getValue()));

		// Set total operand counts
		res.setFile1TotalOperands(Integer.parseInt(operatorsF1.get(2).getValue()));
		res.setFile2TotalOperands(Integer.parseInt(operatorsF2.get(2).getValue()));

		// Set unique operand counts
		res.setFile1UniqueOperands(Integer.parseInt(operatorsF1.get(3).getValue()));
		res.setFile2UniqueOperands(Integer.parseInt(operatorsF2.get(3).getValue()));

		// Set control statement counts
		res.setFile1ControlStatements(Integer.parseInt(operatorsF1.get(4).getValue()));
		res.setFile2ControlStatements(Integer.parseInt(operatorsF2.get(4).getValue()));

		this.result = res;
	}
	
}
