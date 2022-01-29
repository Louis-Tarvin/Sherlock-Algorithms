package uk.louistarvin.module.postprocessing;

import java.io.Serializable;

import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile;

/**
 * This class is exactly the same as GreedyStringTilingRawResult.
 * Since only one postprocessor can be mapped to a RawResult the class couldn't be reused.
 */
public class WinnowingRawResult<T extends Serializable> extends GreedyStringTilingRawResult<T> {

    public WinnowingRawResult(ISourceFile file1, ISourceFile file2) {
        super(file1, file2);
    }
    
}
