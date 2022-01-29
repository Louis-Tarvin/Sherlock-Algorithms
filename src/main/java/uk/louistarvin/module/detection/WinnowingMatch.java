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

    public WinnowingMatch(long firstFileID, long secondFileID, int firstIndex, int secondIndex, int length, ITuple<Integer, Integer> firstLines,
            ITuple<Integer, Integer> secondLines) {
        this.firstFileID = firstFileID;
        this.secondFileID = secondFileID;
        this.firstIndex = firstIndex;
        this.secondIndex = secondIndex;
        this.length = length;
        this.firstLines = firstLines;
        this.secondLines = secondLines;
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
}
