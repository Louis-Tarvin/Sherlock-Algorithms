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
import uk.louistarvin.module.detection.GSTMatch;

public class GreedyStringTilingPostProcessor implements IPostProcessor<GreedyStringTilingRawResult> {

    private boolean isLinked(GSTMatch first, GSTMatch second) {
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
    private void addToMatches(GSTMatch match, ArrayList<ArrayList<GSTMatch>> matches) {
        // Check each group in matches to see if match is linked
        for (ArrayList<GSTMatch> group : matches) {
            for (GSTMatch other : group) {
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
    private ArrayList<ArrayList<GSTMatch>> GroupMatches(List<GreedyStringTilingRawResult> rawResults) {
        ArrayList<ArrayList<GSTMatch>> matches = new ArrayList<>();

        for (GreedyStringTilingRawResult<GSTMatch> result : rawResults) {
            for (GSTMatch match : result.getObjects()) {
                System.out.println(match.getLength());
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
            List<GreedyStringTilingRawResult> rawResults) {
            ModelTaskProcessedResults results = new	ModelTaskProcessedResults();

            // A list of all match block groups
            // Each group is stored as a sub list and has the same common code block
            ArrayList<ArrayList<GSTMatch>> matches = GroupMatches(rawResults);

            System.out.println(matches.size());

            // Create a group for each group of matches
            for (ArrayList<GSTMatch> group : matches) {
                ICodeBlockGroup newGroup = results.addGroup();
                HashSet<Long> addedFileIDs = new HashSet<>();
                for (GSTMatch match : group) {
                    if (!addedFileIDs.contains(match.getFirstFileID())) {
                        newGroup.addCodeBlock(SherlockHelper.getSourceFile(match.getFirstFileID()), 1.0f, match.getFirstLines());
                        addedFileIDs.add(match.getFirstFileID());
                    }
                    if (!addedFileIDs.contains(match.getSecondFileID())) {
                        newGroup.addCodeBlock(SherlockHelper.getSourceFile(match.getSecondFileID()), 1.0f, match.getSecondLines());
                        addedFileIDs.add(match.getSecondFileID());
                    }
                }
                try {
                    //TODO: create new detection type
                    newGroup.setDetectionType("BASE_COPIED_BLOCK");
                } catch (UnknownDetectionTypeException e) {
                    e.printStackTrace();
                }
            }

            return results;
    }
    
}
