package uk.louistarvin.sherlock.module.model.extra.postprocessing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.warwick.dcs.sherlock.api.util.SherlockHelper;
import uk.ac.warwick.dcs.sherlock.api.util.Tuple;
import uk.ac.warwick.dcs.sherlock.api.component.ICodeBlockGroup;
import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile;
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.IPostProcessor;
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.ModelTaskProcessedResults;

public class AttributeCountPostProcessor implements IPostProcessor<AttributeCountRawResult> {

	public int totalOperatorWindowSize = 5;
	public int totalOperatorImportance = 6;

	public int uniqueOperatorWindowSize = 3;
	public int uniqueOperatorImportance = 5;

	public int totalOperandWindowSize = 5;
	public int totalOperandImportance = 6;

	public int uniqueOperandWindowSize = 3;
	public int uniqueOperandImportance = 5;

	public int codeLinesWindowSize = 3;
	public int codeLinesImportance = 5;

	public int declaredVarsWindowSize = 2;
	public int declaredVarsImportance = 3;

	public int controlStatementsWindowSize = 1;
	public int controlStatementsImportance = 2;

	@Override
	public ModelTaskProcessedResults processResults(List<ISourceFile> files, List<AttributeCountRawResult> rawResults) {
		ModelTaskProcessedResults results = new	ModelTaskProcessedResults();
		Map<ISourceFile, Integer> totals = new HashMap<>();

		for (AttributeCountRawResult res : rawResults) {
			// Calculate correlation
			int correlation = 0;

			// total operators
			int diff = Math.abs(res.getFile1TotalOperators() - res.getFile2TotalOperators());
			if (diff <= totalOperatorWindowSize) {
				correlation += totalOperatorImportance - diff;
			}

			// total operands
			diff = Math.abs(res.getFile1TotalOperands() - res.getFile2TotalOperands());
			if (diff <= totalOperandWindowSize) {
				correlation += totalOperandImportance - diff;
			}

			// unique operators
			diff = Math.abs(res.getFile1UniqueOperators() - res.getFile2UniqueOperators());
			if (diff <= uniqueOperatorWindowSize) {
				correlation += uniqueOperatorImportance - diff;
			}

			// unique operands
			diff = Math.abs(res.getFile1UniqueOperands() - res.getFile2UniqueOperands());
			if (diff <= uniqueOperandWindowSize) {
				correlation += uniqueOperandImportance - diff;
			}

			// code lines
			diff = Math.abs(res.getFile1Lines() - res.getFile2Lines());
			if (diff <= codeLinesWindowSize) {
				correlation += codeLinesImportance - diff;
			}

			// control statements
			diff = Math.abs(res.getFile1ControlStatements() - res.getFile2ControlStatements());
			if (diff <= controlStatementsWindowSize) {
				correlation += controlStatementsImportance - diff;
			}

			// variables declared and used
			diff = Math.abs(res.getFile1Variables() - res.getFile2Variables());
			if (diff <= declaredVarsWindowSize) {
				correlation += declaredVarsImportance - diff;
			}

			float correlation_norm = correlation / 32.0f; // normalise the result
			totals.put(SherlockHelper.getSourceFile(res.getFile1id()), (int)Math.round(correlation_norm * 100.0));
			correlation_norm *= files.size() - 1; // hacky way to get Sherlock to display correct number

			ICodeBlockGroup group = results.addGroup();
			group.setComment("Line count: " + res.getFile1Lines());
			try {
				group.setDetectionType("ATTRIBUTE_COUNT");
			} catch(Exception e){
				e.printStackTrace();
			}
			//System.out.println(SherlockHelper.getSourceFile(res.getFile1id()).getFileDisplayName() 
					//+ " & " + SherlockHelper.getSourceFile(res.getFile2id()) 
					//+ " correlation: " + correlation);
			group.addCodeBlock(SherlockHelper.getSourceFile(res.getFile1id()), correlation_norm, new Tuple<Integer, Integer>(1, res.getFile1Lines() + 1));
			group.addCodeBlock(SherlockHelper.getSourceFile(res.getFile2id()), correlation_norm, new Tuple<Integer, Integer>(1, res.getFile2Lines() + 1));
		}

		return results;
	}
	
}
