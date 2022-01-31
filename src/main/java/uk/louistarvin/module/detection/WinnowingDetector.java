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
		 * @param s The string to hash. Should be of length k
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
			
			// Generate hashes using Rabin fingerprint rolling hash
			for (int i = 0; i < lines.size(); i++) {
				String chars = lines.get(i).getValue();
				for (int j = 0; j < chars.length(); j++) {
					if (charQueue.size() < k) {
						charQueue.add(chars.charAt(j));
						lineQueue.add(lines.get(i).getKey());
					} else {
						if (hs == -1) {
							// generate initial hash value
							System.out.println(charQueue.toArray(new Character[k]));
							hs = hash(charQueue.toArray(new Character[k]));
							System.out.println("initial hash: " + hs);
						} else {
							System.out.println("oldChar: " + oldChar + "; newChar: " + charQueue.getLast());
							hs = rehash(hs, oldChar, charQueue.getLast(), base_offset);
						}
						// move the hashing window to the right
						System.out.println(charQueue + " - " + hs);
						oldChar = charQueue.remove();
						charQueue.add(chars.charAt(j));
						kgrams.add(new KGram(charQueue.toArray(new Character[k]), hs, lineQueue.remove(), lines.get(i).getKey()));
						lineQueue.add(lines.get(i).getKey());
					}
				}
			}
			// generate final hash
			if (charQueue.size() >= k) {
				if (hs == -1) {
					// generate initial hash value
					hs = hash(charQueue.toArray(new Character[k]));
					System.out.println("initial hash: " + hs);
				} else {
					hs = rehash(hs, oldChar, charQueue.getLast(), base_offset);
				}
				// move the hashing window to the right
				System.out.println(charQueue);
				kgrams.add(new KGram(charQueue.toArray(new Character[k]), hs, lineQueue.remove(), lines.get(lines.size()-1).getKey()));
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
				window[i] = new KGram(new Character[0], Integer.MAX_VALUE, 0, 0);
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
					System.out.println(window[min].getHash());
				} else {
					// The previous minimum is still in the window.
					// Compare against the new value and update min if necessary
					if (window[r].getHash() <= window[min].getHash()) {
						min = r;
						fingerprint.add(window[min]);
						System.out.println(window[min].getHash());
					}
				}
			}

			return fingerprint.toArray(new KGram[fingerprint.size()]);
		}

        @Override
        public void execute() {
            // Get the pre-processed lines
			List<IndexedString> linesF1 = this.file1.getPreProcessedLines("lines");
			List<IndexedString> linesF2 = this.file2.getPreProcessedLines("lines");

            for (IndexedString line: linesF1) {
                System.out.println(line);
            }

            // for (IndexedString line: linesF2) {
            //     System.out.println(line);
            // }

			KGram[] kgramsF1 = generateHashes(linesF1);
			KGram[] kgramsF2 = generateHashes(linesF2);

			KGram[] fingerprintF1 = winnow(kgramsF1);
			KGram[] fingerprintF2 = winnow(kgramsF2);

			// Arrays to keep track of the number of hashes for each line
			int[] hashesPerLineF1 = new int[this.file1.getFile().getTotalLineCount()+1];
			int[] hashesPerLineF2 = new int[this.file2.getFile().getTotalLineCount()+1];

			for (int i = 0; i < fingerprintF1.length; i++) {
				hashesPerLineF1[fingerprintF1[i].getStartLine()]++;
			}
			for (int i = 0; i < fingerprintF2.length; i++) {
				hashesPerLineF2[fingerprintF2[i].getStartLine()]++;
			}

			// Figure out which of the files is smaller
			KGram[] larger, smaller;
			ISourceFile largerFile, smallerFile;
			int[] largerHashesPerLine, smallerHashesPerLine;
			if (fingerprintF1.length > fingerprintF2.length) {
				larger = fingerprintF1;
				smaller = fingerprintF2;
				largerFile = this.file1.getFile();
				smallerFile = this.file2.getFile();
				largerHashesPerLine = hashesPerLineF1;
				smallerHashesPerLine = hashesPerLineF2;
			} else {
				larger = fingerprintF2;
				smaller = fingerprintF1;
				largerFile = this.file2.getFile();
				smallerFile = this.file1.getFile();
				largerHashesPerLine = hashesPerLineF2;
				smallerHashesPerLine = hashesPerLineF1;
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
								int file1LinesHashes = 0;
								for (int i = file1Lines.getKey(); i <= file1Lines.getValue(); i++) {
									file1LinesHashes += smallerHashesPerLine[i];
								}
								int file2LinesHashes = 0;
								for (int i = file2Lines.getKey(); i <= file2Lines.getValue(); i++) {
									file2LinesHashes += largerHashesPerLine[i];
								}
								WinnowingMatch match = new WinnowingMatch(smallerFile.getPersistentId(), largerFile.getPersistentId(), a, b, j, file1Lines, file1LinesHashes, file2Lines, file2LinesHashes);
								newMatches.add(match);
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
				System.out.println("test");
				res.addObject(match);
			}
	
			this.result = res;
        }

		/**
		 * A hashed K-gram from a submission
		 */
		public class KGram {
			private final Character[] charArray;
			private final int hash;
			private final int startLine;
			private final int endLine;
			private boolean marked;

			public KGram(Character[] charArray, int hash, int startLine, int endLine) {
				this.charArray = charArray;
				this.hash = hash;
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

			public int getStartLine() {
				return startLine;
			}

			public int getEndLine() {
				return endLine;
			}
		}

    }
}
