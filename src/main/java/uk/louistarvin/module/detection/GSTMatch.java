package uk.louistarvin.module.detection;

import java.io.Serializable;

import uk.ac.warwick.dcs.sherlock.api.util.ITuple;

/**
 * Represents sections of two submissions that match.
 */
public class GSTMatch implements Serializable {
    
    private long firstFileID; // File ID of first submission
    private long secondFileID; // File ID of second submission
    private final int firstIndex; // The index of the token for the first submission
    private final int secondIndex; // The index of the token for the second submission
    private final int length; // The length of the match
    private final ITuple<Integer, Integer> firstLines; // The start and end lines in the first file
    private final ITuple<Integer, Integer> secondLines; // The start and end lines in the first file
    
    /**
     * Constructor, stores all inputted data in the container object.
     * @param firstIndex The index of the token for the first submission.
     * @param secondIndex The index of the token for the second submission.
     * @param length The length of the match.
     * @param firstLines The start and end line numbers for the code block in the first file
     * @param secondLines The start and end line numbers for the code block in the second file
     */
    public GSTMatch(long firstFileID, long secondFileID, int firstIndex, int secondIndex, int length, ITuple<Integer, Integer> firstLines,
            ITuple<Integer, Integer> secondLines) {
        this.firstFileID = firstFileID;
        this.secondFileID = secondFileID;
        this.firstIndex = firstIndex;
        this.secondIndex = secondIndex;
        this.length = length;
        this.firstLines = firstLines;
        this.secondLines = secondLines;
    }

    public long getFirstFileID() {
        return firstFileID;
    }

    public long getSecondFileID() {
        return secondFileID;
    }

    public int getLength() {
        return length;
    }

    public int getSecondIndex() {
        return secondIndex;
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public ITuple<Integer, Integer> getFirstLines() {
        return firstLines;
    }

    public ITuple<Integer, Integer> getSecondLines() {
        return secondLines;
    }
}