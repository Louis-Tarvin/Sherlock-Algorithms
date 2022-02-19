package uk.louistarvin.module.postprocessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import uk.ac.warwick.dcs.sherlock.api.component.ICodeBlockGroup;
import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile;
import uk.ac.warwick.dcs.sherlock.api.exception.UnknownDetectionTypeException;
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.IPostProcessor;
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.ModelTaskProcessedResults;
import uk.ac.warwick.dcs.sherlock.api.util.ITuple;
import uk.ac.warwick.dcs.sherlock.api.util.SherlockHelper;
import uk.louistarvin.module.detection.WinnowingMatch;

public class WinnowingPostProcessor implements IPostProcessor<WinnowingRawResult> {

    private boolean isLinked(WinnowingMatch first, WinnowingMatch second) {
        // Check each file combination. If the files and block are the same, return true
        ITuple<Integer, Integer> firstLines, secondLines;
        if (first.getFirstFileID() == second.getFirstFileID()) {
            firstLines = first.getFirstLines();
            secondLines = second.getFirstLines();
        } else if (first.getSecondFileID() == second.getFirstFileID()) {
            firstLines = first.getSecondLines();
            secondLines = second.getFirstLines();
        } else if (first.getFirstFileID() == second.getSecondFileID()) {
            firstLines = first.getFirstLines();
            secondLines = second.getSecondLines();
        } else if (first.getSecondFileID() == second.getSecondFileID()) {
            firstLines = first.getSecondLines();
            secondLines = second.getSecondLines();
        } else {
            // No common file
            return false;
        }
        if (firstLines.getKey() == secondLines.getKey() 
            && firstLines.getValue() == secondLines.getValue()
            && firstLines.getKey() != firstLines.getValue()) {
            // Code blocks are the same -> files are linked
            return true;
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
                        (float) Math.min((float) (match.getFirstSegmentScore()) / (float) (match.getFirstLinesChars()), 1.0f), 
                        match.getFirstLines());
                    addedFileIDs.add(match.getFirstFileID());
                }
                if (!addedFileIDs.contains(match.getSecondFileID())) {
                    newGroup.addCodeBlock(SherlockHelper.getSourceFile(match.getSecondFileID()), 
                        (float) Math.min((float) (match.getSecondSegmentScore()) / (float) (match.getSecondLinesChars()), 1.0f), 
                        match.getSecondLines());
                    addedFileIDs.add(match.getSecondFileID());
                }
            }
            try {
                newGroup.setDetectionType("WINNOWING_MATCH");
            } catch (UnknownDetectionTypeException e) {
                e.printStackTrace();
            }
        }

        return results;
    }
    
}
