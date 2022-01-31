package uk.louistarvin.module.postprocessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import uk.ac.warwick.dcs.sherlock.api.component.ICodeBlockGroup;
import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile;
import uk.ac.warwick.dcs.sherlock.api.exception.UnknownDetectionTypeException;
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.IPostProcessor;
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.ModelTaskProcessedResults;
import uk.ac.warwick.dcs.sherlock.api.util.SherlockHelper;
import uk.louistarvin.module.detection.WinnowingMatch;

public class WinnowingPostProcessor implements IPostProcessor<WinnowingRawResult> {

    private boolean isLinked(WinnowingMatch first, WinnowingMatch second) {
        // Check each file combination. If the files and block are the same, return true
        if (first.getLength() == second.getLength()) {
            if (first.getFirstFileID() == second.getFirstFileID() && first.getFirstIndex() == second.getFirstIndex()) {
                return true;
            } else if (first.getSecondFileID() == second.getFirstFileID() && first.getSecondIndex() == second.getFirstIndex()) {
                return true;
            } else if (first.getFirstFileID() == second.getSecondFileID() && first.getFirstIndex() == second.getSecondIndex()) {
                return true;
            } else if (first.getSecondFileID() == second.getSecondFileID() && first.getSecondIndex() == second.getSecondIndex()) {
                return true;
            }
        }
        // No match was found
        return false;
    }

    /**
     * Checks each group in matches to see if the match is linked. If so, it adds it to the group
     * @param match the match to add
     * @param matches The list of grouped matches
     */
    private void addToMatches(WinnowingMatch match, ArrayList<ArrayList<WinnowingMatch>> matches) {
        // Check each group in matches to see if match is linked
        for (ArrayList<WinnowingMatch> group : matches) {
            for (WinnowingMatch other : group) {
                if (isLinked(match, other)) {
                    group.add(match);
                    return;
                }
            }
        }
        // If no link is found: make a new group
        matches.add(new ArrayList<>());
        matches.get(matches.size()-1).add(match);
    }

    /**
     * Processes the raw results, grouping matches that share code blocks
     * @param rawResults The raw match data produced by the detector
     * @return The grouped matches, with each group stored as a sub list
     */
    private ArrayList<ArrayList<WinnowingMatch>> GroupMatches(List<WinnowingRawResult> rawResults) {
        ArrayList<ArrayList<WinnowingMatch>> matches = new ArrayList<>();

        for (WinnowingRawResult<WinnowingMatch> result : rawResults) {
            for (WinnowingMatch match : result.getObjects()) {
                if (matches.size() == 0) {
                    matches.add(new ArrayList<>());
                    matches.get(0).add(match);
                } else {
                    addToMatches(match, matches);
                }
            }
        }

        return matches;
    }
    @Override
    public ModelTaskProcessedResults processResults(List<ISourceFile> files,
            List<WinnowingRawResult> rawResults) {
        ModelTaskProcessedResults results = new	ModelTaskProcessedResults();

        // A list of all match block groups
        // Each group is stored as a sub list and has the same common code block
        ArrayList<ArrayList<WinnowingMatch>> matches = GroupMatches(rawResults);

        // Create a group for each group of matches
        for (ArrayList<WinnowingMatch> group : matches) {
            ICodeBlockGroup newGroup = results.addGroup();
            HashSet<Long> addedFileIDs = new HashSet<>();
            for (WinnowingMatch match : group) {
                if (!addedFileIDs.contains(match.getFirstFileID())) {
                    newGroup.addCodeBlock(SherlockHelper.getSourceFile(match.getFirstFileID()), 
                        ((float) match.getLength()) / ((float) match.getFirstLinesHashes()), 
                        match.getFirstLines());
                    addedFileIDs.add(match.getFirstFileID());
                }
                if (!addedFileIDs.contains(match.getSecondFileID())) {
                    newGroup.addCodeBlock(SherlockHelper.getSourceFile(match.getSecondFileID()), 
                        ((float) match.getLength()) / ((float) match.getSecondLinesHashes()), 
                        match.getSecondLines());
                    addedFileIDs.add(match.getSecondFileID());
                }
            }
            try {
                newGroup.setDetectionType("GST_MATCH");
            } catch (UnknownDetectionTypeException e) {
                e.printStackTrace();
            }
        }

        return results;
    }
    
}
