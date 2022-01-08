package uk.louistarvin.module.postprocessing;

import java.util.List;

import uk.ac.warwick.dcs.sherlock.api.component.ICodeBlockGroup;
import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile;
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.IPostProcessor;
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.ModelTaskProcessedResults;
import uk.ac.warwick.dcs.sherlock.api.util.SherlockHelper;
import uk.louistarvin.module.detection.GSTMatch;

public class GreedyStringTilingPostProcessor implements IPostProcessor<GreedyStringTilingRawResult> {

    @Override
    public ModelTaskProcessedResults processResults(List<ISourceFile> files,
            List<GreedyStringTilingRawResult> rawResults) {
            ModelTaskProcessedResults results = new	ModelTaskProcessedResults();

            for (GreedyStringTilingRawResult<GSTMatch> rawRes : rawResults) {
                for (GSTMatch match : rawRes.getObjects()) {
                    ICodeBlockGroup group = results.addGroup();
                    try {
                        group.setDetectionType("BASE_COPIED_BLOCK");
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    group.addCodeBlock(SherlockHelper.getSourceFile(match.getFirstFileID()), 1.0f, match.getFirstLines());
                    group.addCodeBlock(SherlockHelper.getSourceFile(match.getSecondFileID()), 1.0f, match.getSecondLines());
                }
            }

            return results;
    }
    
}
