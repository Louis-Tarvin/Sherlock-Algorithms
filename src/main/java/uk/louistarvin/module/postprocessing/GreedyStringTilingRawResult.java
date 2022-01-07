package uk.louistarvin.module.postprocessing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile;
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.AbstractModelTaskRawResult;

public class GreedyStringTilingRawResult<T extends Serializable> extends AbstractModelTaskRawResult {

    /**
	 * The ID number of the first file in the compared pair.
	 */
	long file1id;

	/**
	 * The ID number of the second file in the compared pair.
	 */
	long file2id;

	/**
	 * The list of match objects (containers).
	 */
	List<T> objects;

	/**
	 * The number of match objects stored in the container
	 */
	int size;

    /**
	 * Object constructor, saves the compared file ids, initialises interior list, and sets size to zero.
	 * @param file1 File ID of the first file in the compared pair.
	 * @param file2 File ID of the second file in the compared pair.
	 */
    public GreedyStringTilingRawResult(ISourceFile file1, ISourceFile file2) {
        this.file1id = file1.getPersistentId();
        this.file2id = file2.getPersistentId();

        this.objects = new ArrayList<>();
        this.size = 0;
    }

    public List<T> getObjects() {
        return objects;
    }

	public void addObject(T object) {
		objects.add(object);
		size++;
	}

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean testType(AbstractModelTaskRawResult baseline) {
        if (baseline instanceof GreedyStringTilingRawResult) {
            GreedyStringTilingRawResult<T> bl = (GreedyStringTilingRawResult<T>) baseline;
			return bl.getObjects().get(0).getClass().equals(this.getObjects().get(0).getClass()); // Check generic type is the same
        }
		return false;
    }
    
}
