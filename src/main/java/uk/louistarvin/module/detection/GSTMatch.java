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
    private final int firstLinesTokens; // The number of tokens between start and end lines in the first file
    private final ITuple<Integer, Integer> secondLines; // The start and end lines in the first file
    private final int secondLinesTokens; // The number of tokens between start and end lines in the second file
    
    /**
     * Constructor, stores all inputted data in the container object.
     * @param firstFileID The ID of the first submission
     * @param secondFileID The ID of the second submission
     * @param firstIndex The index of the token for the first submission.
     * @param secondIndex The index of the token for the second submission.
     * @param length The length of the match.
     * @param firstLines The start and end line numbers for the code block in the first file
     * @param firstLinesTokens // The number of tokens between start and end lines in the first file
     * @param secondLines The start and end line numbers for the code block in the second file
     * @param secondLinesTokens // The number of tokens between start and end lines in the second file
     */
    public GSTMatch(long firstFileID, long secondFileID, int firstIndex, int secondIndex, int length, ITuple<Integer, Integer> firstLines,
            int firstLinesTokens, ITuple<Integer, Integer> secondLines, int secondLinesTokens) {
        this.firstFileID = firstFileID;
        this.secondFileID = secondFileID;
        this.firstIndex = firstIndex;
        this.secondIndex = secondIndex;
        this.length = length;
        this.firstLines = firstLines;
        this.firstLinesTokens = firstLinesTokens;
        this.secondLines = secondLines;
        this.secondLinesTokens = secondLinesTokens;
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

    public int getFirstLinesTokens() {
        return firstLinesTokens;
    }

    public int getSecondLinesTokens() {
        return secondLinesTokens;
    }

    /**
     * Checks if this match overlaps with the other
     * @param other The match to check against
     * @return true if matches overlap, false otherwise
     */
    public boolean overlaps(GSTMatch other) {
        if (firstIndex < other.getFirstIndex()) {
            if ((other.getFirstIndex() - firstIndex) < length) {
                return true;
            }
        } else {
            if ((firstIndex - other.getFirstIndex()) < other.getLength()) {
                return true;
            }
        }

        if (secondIndex < other.getSecondIndex()) {
            return (other.getSecondIndex() - secondIndex) < length;
        } else {
            return (secondIndex - other.getSecondIndex()) < other.getLength();
        }
    }
}