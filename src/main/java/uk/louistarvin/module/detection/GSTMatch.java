package uk.louistarvin.module.detection;

import java.io.Serializable;

/**
 * Represents sections of two submissions that match.
 */
public class GSTMatch implements Serializable {
    /**
     * The line positions of both blocks.
     */
    private final int firstIndex; // The index of the token for the first submission
    private final int secondIndex; // The index of the token for the second submission
    private final int length; // The length of the match

    /**
     * Constructor, stores all inputted data in the container object.
     * @param firstIndex The index of the token for the first submission.
     * @param secondIndex The index of the token for the second submission.
     * @param length The length of the match.
     */
    GSTMatch(int firstIndex, int secondIndex, int length) {
        this.firstIndex = firstIndex;
        this.secondIndex = secondIndex;
        this.length = length;
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