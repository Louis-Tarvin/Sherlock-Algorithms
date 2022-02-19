package uk.louistarvin.module.detection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import uk.ac.warwick.dcs.sherlock.api.annotation.AdjustableParameter;
import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile;
import uk.ac.warwick.dcs.sherlock.api.model.detection.IDetector;
import uk.ac.warwick.dcs.sherlock.api.model.detection.ModelDataItem;
import uk.ac.warwick.dcs.sherlock.api.model.detection.PairwiseDetector;
import uk.ac.warwick.dcs.sherlock.api.model.detection.PairwiseDetectorWorker;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.PreProcessingStrategy;
import uk.ac.warwick.dcs.sherlock.api.util.IndexedString;
import uk.ac.warwick.dcs.sherlock.api.util.Tuple;
import uk.louistarvin.module.preprocessing.WinnowingPreProcessor;
import uk.louistarvin.module.detection.WinnowingDetector.WinnowingDetectorWorker;
import uk.louistarvin.module.postprocessing.WinnowingRawResult;

public class WinnowingDetector extends PairwiseDetector<WinnowingDetectorWorker> {
    
    @AdjustableParameter (name = "Guarantee threshold", 
		defaultValue = 16, 
		minimumBound = 1, 
		maxumumBound = 40, 
		step = 1, 
		description = "Matches longer than this threshold are guaranteed to be detected. Must be larger than the noise threshold")
	public int t;

    @AdjustableParameter (name = "Noise threshold", 
		defaultValue = 16, 
		minimumBound = 1, 
		maxumumBound = 40, 
		step = 1, 
		description = "Matches shorter than this value are ignored. Must be less than the guarantee threshold")
	public int k;

    public WinnowingDetector() {
        super("Winnowing",
			"Generates a fingerprint for each submission, which is then used as the basis for comparison", 
			WinnowingDetectorWorker.class, 
			PreProcessingStrategy.of("lines", false, WinnowingPreProcessor.class));
    }

    public class WinnowingDetectorWorker extends PairwiseDetectorWorker<WinnowingRawResult<WinnowingMatch>> {

        public WinnowingDetectorWorker(IDetector parent, ModelDataItem file1Data, ModelDataItem file2Data) {
            super(parent, file1Data, file2Data);
        }

        /**
		 * Generate the initial hash used in the Rabin fingerprint rolling hash function
		 * @param s The character array to hash. Should be of length k
		 * @return The hash value as an int
		 */
		private int hash(Character[] s) {
			if (s.length > 2) {
				int c = (int) s[s.length - 1];
				return (((hash(Arrays.copyOfRange(s, 0, s.length-1)) * 256 ) % 101) + c) %101;
			} else if (s.length == 2){
				int c1 = (int) s[0];
				int c2 = (int) s[1];
				return (((c1 * 256)%101)+c2)%101;
			} else {
				int c = (int) s[0];
				return c % 101;
			}
		}
	
		/**
		 * Generate the next iteration of the Rabin fingerprint rolling hash
		 * @param oldhash Hash value from the previous iteration
		 * @param oldChar The value of the character that has just left the hashing window
		 * @param newChar The value of the character that has just entered the hashing window
		 * @param offset The pattern length base offset
		 * @return The new hash value
		 */
		private int rehash(int oldhash, int oldChar, int newChar, int offset) {
			int unmodded = ((oldhash + 101 - oldChar * offset)*256)+newChar;
			return ((unmodded%101)+101)%101;
		}

		/**
		 * Split the submission into K-grams and hash each one using the rolling
		 * hash function
		 * @param lines The preprocessed lines of the submission
		 * @return An array of K-grams that have been hashed
		 */
		private KGram[] generateHashes(List<IndexedString> lines) {
			List<KGram> kgrams = new ArrayList<>();

			int base_offset = 1;
			for (int i=0; i<k-1; i++) {
				base_offset = (base_offset * 256) % 101;
			}

			int hs = -1;
			char oldChar = 0;
			LinkedList<Character> charQueue = new LinkedList<>();
			LinkedList<Integer> lineQueue = new LinkedList<>();
			LinkedList<Integer> indexQueue = new LinkedList<>();
			
			// Generate hashes using Rabin fingerprint rolling hash
			for (int i = 0; i < lines.size(); i++) {
				String chars = lines.get(i).getValue();
				for (int j = 0; j < chars.length(); j++) {
					if (charQueue.size() < k) {
						charQueue.add(chars.charAt(j));
						lineQueue.add(lines.get(i).getKey());
						indexQueue.add(j);
					} else {
						if (hs == -1) {
							// generate initial hash value
							hs = hash(charQueue.toArray(new Character[k]));
						} else {
							hs = rehash(hs, oldChar, charQueue.getLast(), base_offset);
						}
						// move the hashing window to the right
						oldChar = charQueue.remove();
						charQueue.add(chars.charAt(j));
						kgrams.add(new KGram(charQueue.toArray(new Character[k]), hs, indexQueue.remove(), lineQueue.remove(), lines.get(i).getKey()));
						lineQueue.add(lines.get(i).getKey());
						indexQueue.add(j);
					}
				}
			}
			// generate final hash
			if (charQueue.size() >= k) {
				if (hs == -1) {
					// generate initial hash value
					hs = hash(charQueue.toArray(new Character[k]));
				} else {
					hs = rehash(hs, oldChar, charQueue.getLast(), base_offset);
				}
				// move the hashing window to the right
				kgrams.add(new KGram(charQueue.toArray(new Character[k]), hs, indexQueue.remove(), lineQueue.remove(), lines.get(lines.size()-1).getKey()));
			}
			return kgrams.toArray(new KGram[kgrams.size()]);
		}

		/**
		 * Performs the Winnowing algorithm to select a subset of k-grams to use
		 * as the 'fingerprint' of the submission
		 * @param kgrams The hashed k-grams from the submission
		 * @return a subset of k-grams representing the fingerprint
		 */
		KGram[] winnow(KGram[] kgrams) {
			List<KGram> fingerprint = new ArrayList<>();

			int w = t - k + 1; // window size
			KGram[] window = new KGram[w];

			for (int i = 0; i < w; i++) {
				// placeholder K-grams
				window[i] = new KGram(new Character[0], Integer.MAX_VALUE, 0, 0, 0);
			}

			int r = 0; // window right end
			int min = 0; // index of minimum hash value

			for (int i = 0; i < kgrams.length; i++) {
				r = Math.floorMod(r+1, w);
				window[r] = kgrams[i];
				if (min == r) {
					// The previous minimum is no longer in the window.
					// Scan the window to find the rightmost minimal hash
					for (int j = Math.floorMod(r-1, w); j != r; j=Math.floorMod(j-1+w, w)) {
						if (window[j].getHash() < window[min].getHash())
							min = j;
					}
					fingerprint.add(window[min]);
				} else {
					// The previous minimum is still in the window.
					// Compare against the new value and update min if necessary
					if (window[r].getHash() <= window[min].getHash()) {
						min = r;
						fingerprint.add(window[min]);
					}
				}
			}

			return fingerprint.toArray(new KGram[fingerprint.size()]);
		}

		/**
		 * Calculate the number of characters within a match
		 * @param start The first K-gram in the match
		 * @param end The last K-gram in the match
		 * @param lines The preprocessed lines
		 * @return The number of characters
		 */
		private int segmentScore(KGram start, KGram end, int[] charsPerLine) {
			if (start.startLine == end.endLine) {
				// when a match is on a single line it's simply end index - start index
				return (end.startIndex + k) - start.startIndex;
			} else {
				// number of characters in start line
				int score = charsPerLine[start.getStartLine()] - start.getStartIndex();
				// number of characters in between lines
				for (int i = start.getStartLine() + 1; i < end.getEndLine(); i++) {
					score += charsPerLine[i];
				}
				// number of characters in end line
				if (end.startLine == end.endLine) {
					score += end.startIndex + k;
				} else {
					score += k - (charsPerLine[end.startLine] - end.startIndex);
				}
				return score;
			}
		}

        @Override
        public void execute() {
			// Early return if adjustable parameters are invalid
			if (k > t) {
				System.err.println("Error during postprocessing: Noise threshold cannot be greater than guarantee threshold");
				return;
			}
            // Get the pre-processed lines
			List<IndexedString> linesF1 = this.file1.getPreProcessedLines("lines");
			List<IndexedString> linesF2 = this.file2.getPreProcessedLines("lines");

			KGram[] kgramsF1 = generateHashes(linesF1);
			KGram[] kgramsF2 = generateHashes(linesF2);

			KGram[] fingerprintF1 = winnow(kgramsF1);
			KGram[] fingerprintF2 = winnow(kgramsF2);

			// Arrays to keep track of the number of hashes for each line
			int[] charsPerLineF1 = new int[this.file1.getFile().getTotalLineCount()+1];
			int[] charsPerLineF2 = new int[this.file2.getFile().getTotalLineCount()+1];

			for (IndexedString l : linesF1) {
				charsPerLineF1[l.getKey()] += l.getValue().length();
			}
			for (IndexedString l : linesF2) {
				charsPerLineF2[l.getKey()] += l.getValue().length();
			}

			// Figure out which of the files is smaller
			KGram[] larger, smaller;
			ISourceFile largerFile, smallerFile;
			int[] largerCharsPerLine, smallerCharsPerLine;
			if (fingerprintF1.length > fingerprintF2.length) {
				larger = fingerprintF1;
				smaller = fingerprintF2;
				largerFile = this.file1.getFile();
				smallerFile = this.file2.getFile();
				largerCharsPerLine = charsPerLineF1;
				smallerCharsPerLine = charsPerLineF2;
			} else {
				larger = fingerprintF2;
				smaller = fingerprintF1;
				largerFile = this.file2.getFile();
				smallerFile = this.file1.getFile();
				largerCharsPerLine = charsPerLineF2;
				smallerCharsPerLine = charsPerLineF1;
			}
	
			// Hashes from larger submission are placed into a hash table
			HashMap<Integer, List<Integer>> largerHashMap = new HashMap<>();
			for (int i = 0; i < larger.length; i++) {
				if (largerHashMap.containsKey(larger[i].getHash())) {
					largerHashMap.get(larger[i].getHash()).add(i);
				} else {
					List<Integer> temp = new ArrayList<>();
					temp.add(i);
					largerHashMap.put(larger[i].getHash(), temp);
				}
			}
	
            // Find matches using greedy string tiling
			int maxMatch;
			List<WinnowingMatch> tiles = new ArrayList<>();
			do {
				maxMatch = 1;
				List<WinnowingMatch> newMatches = new ArrayList<>();
				for (int a = 0; a < smaller.length - maxMatch; a++) {
					if (smaller[a].isMarked()) {
						continue; // skip marked tokens
					}
					List<Integer> possibleMatches = largerHashMap.get(smaller[a].getHash());
					if (possibleMatches != null) {
						for (int b : possibleMatches) {
							if (larger[b].isMarked()) {
								continue; // skip marked tokens
							}
							int j = 0;
							while (a+j < smaller.length && b+j < larger.length && Arrays.equals(smaller[a+j].getCharArray(), larger[b+j].getCharArray()) 
								&& !smaller[a+j].isMarked() && !larger[b+j].isMarked()) {
								// Extend the match as far as possible
								j++;
							}
							if (j >= maxMatch) {
								if (j > maxMatch) {
									// New maximum match length
									newMatches.clear();
									maxMatch = j;
								}
								// Create and add the match
								Tuple<Integer, Integer> file1Lines = new Tuple<>(smaller[a].getStartLine(), smaller[a+j-1].getEndLine());
								Tuple<Integer, Integer> file2Lines = new Tuple<>(larger[b].getStartLine(), larger[b+j-1].getEndLine());
								int file1LinesChars = 0;
								for (int i = file1Lines.getKey(); i <= file1Lines.getValue(); i++) {
									file1LinesChars += smallerCharsPerLine[i];
								}
								int file2LinesChars = 0;
								for (int i = file2Lines.getKey(); i <= file2Lines.getValue(); i++) {
									file2LinesChars += largerCharsPerLine[i];
								}
								int file1SegmentScore = segmentScore(smaller[a], smaller[a+j-1], smallerCharsPerLine);
								int file2SegmentScore = segmentScore(larger[b], larger[b+j-1], largerCharsPerLine);
								WinnowingMatch match = new WinnowingMatch(smallerFile.getPersistentId(),
									largerFile.getPersistentId(),
									a,
									b,
									j,
									file1Lines,
									file2Lines,
									file1LinesChars,
									file2LinesChars,
									file1SegmentScore,
									file2SegmentScore);
								addMatchIfNotOverlapping(newMatches, match);
							}
						}
					}
				}
	
				for (WinnowingMatch match : newMatches) {
					for (int i = 0; i < match.getLength(); i++) {
						smaller[match.getFirstIndex()+i].mark();
						larger[match.getSecondIndex()+i].mark();
					}
					tiles.add(match);
				}
	
			} while (maxMatch > 1);

			// Return the result
			WinnowingRawResult<WinnowingMatch> res;
			res = new WinnowingRawResult<WinnowingMatch>(smallerFile, largerFile);
	
			for (WinnowingMatch match : tiles) {
				res.addObject(match);
			}
	
			this.result = res;
        }

		private void addMatchIfNotOverlapping(List<WinnowingMatch> matches, WinnowingMatch newMatch) {
			for (WinnowingMatch match : matches) {
				if (match.overlaps(newMatch)) {
					return;
				}
			}
			matches.add(newMatch);
		}

		/**
		 * A hashed K-gram from a submission
		 */
		public class KGram {
			private final Character[] charArray;
			private final int hash;
			private final int startIndex;
			private final int startLine;
			private final int endLine;
			private boolean marked;

			public KGram(Character[] charArray, int hash, int startIndex, int startLine, int endLine) {
				this.charArray = charArray;
				this.hash = hash;
				this.startIndex = startIndex;
				this.startLine = startLine;
				this.endLine = endLine;
				this.marked = false;
			}

			public Character[] getCharArray() {
				return charArray;
			}

			public boolean isMarked() {
				return marked;
			}

			public void mark() {
				this.marked = true;
			}

			public int getHash() {
				return hash;
			}

			public int getStartIndex() {
				return startIndex;
			}

			public int getStartLine() {
				return startLine;
			}

			public int getEndLine() {
				return endLine;
			}
		}

    }
}
