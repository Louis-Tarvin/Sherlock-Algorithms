package uk.louistarvin.module.detection;

import java.io.Serializable;

import uk.ac.warwick.dcs.sherlock.api.util.ITuple;

public class WinnowingMatch implements Serializable {
    private final long firstFileID; // File ID of first submission
    private final long secondFileID; // File ID of second submission
    private final int firstIndex; // The index of the fingerprint for the first submission
    private final int secondIndex; // The index of the fingerprint for the second submission
    private final int length; // The length of the match
    private final ITuple<Integer, Integer> firstLines; // The start and end lines in the first file
    private final ITuple<Integer, Integer> secondLines; // The start and end lines in the second file
    private final int firstLinesChars; // The number of characters between start and end lines in the first file
    private final int secondLinesChars; // The number of characters between start and end lines in the second file
    private final int firstSegmentScore;
    private final int secondSegmentScore;

    public WinnowingMatch(long firstFileID, long secondFileID, int firstIndex, int secondIndex, int length, ITuple<Integer, Integer> firstLines,
            ITuple<Integer, Integer> secondLines, int firstLinesChars, int secondLinesChars, int firstSegmentScore, int secondSegmentScore) {
        this.firstFileID = firstFileID;
        this.secondFileID = secondFileID;
        this.firstIndex = firstIndex;
        this.secondIndex = secondIndex;
        this.length = length;
        this.firstLines = firstLines;
        this.secondLines = secondLines;
        this.firstLinesChars = firstLinesChars;
        this.secondLinesChars = secondLinesChars;
        this.firstSegmentScore = firstSegmentScore;
        this.secondSegmentScore = secondSegmentScore;
    }

    public int getFirstSegmentScore() {
        return firstSegmentScore;
    }

    public int getSecondSegmentScore() {
        return secondSegmentScore;
    }

    public int getFirstLinesChars() {
        return firstLinesChars;
    }

    public int getSecondLinesChars() {
        return secondLinesChars;
    }

    public ITuple<Integer, Integer> getFirstLines() {
        return firstLines;
    }
    
    public ITuple<Integer, Integer> getSecondLines() {
        return secondLines;
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

    /**
     * Checks if this match overlaps with the other
     * @param other The match to check against
     * @return true if matches overlap, false otherwise
     */
    public boolean overlaps(WinnowingMatch other) {
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
