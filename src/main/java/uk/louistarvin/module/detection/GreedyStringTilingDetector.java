
package uk.louistarvin.module.detection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import uk.louistarvin.module.postprocessing.GreedyStringTilingRawResult;
import uk.louistarvin.module.detection.GreedyStringTilingDetector.GreedyStringTilingDetectorWorker;
import uk.louistarvin.module.preprocessing.Tokenizer;

import uk.ac.warwick.dcs.sherlock.api.annotation.AdjustableParameter;
import uk.ac.warwick.dcs.sherlock.api.component.ISourceFile;
import uk.ac.warwick.dcs.sherlock.api.model.detection.IDetector;
import uk.ac.warwick.dcs.sherlock.api.model.detection.ModelDataItem;
import uk.ac.warwick.dcs.sherlock.api.model.detection.PairwiseDetectorWorker;
import uk.ac.warwick.dcs.sherlock.api.util.IndexedString;
import uk.ac.warwick.dcs.sherlock.api.util.Tuple;
import uk.ac.warwick.dcs.sherlock.api.model.detection.PairwiseDetector;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.PreProcessingStrategy;

public class GreedyStringTilingDetector extends PairwiseDetector<GreedyStringTilingDetectorWorker> {

	@AdjustableParameter (name = "Minimum Match Length", defaultValue = 16, minimumBound = 1, maxumumBound = 40, step = 1, description = "The lower bound for the length of a match. Smaller is more sensitive")
	public int mml;

	public GreedyStringTilingDetector() {
		super("GST", "Tokenizes the files then uses Greedy String Tiling to find matching segments", GreedyStringTilingDetectorWorker.class, PreProcessingStrategy.of("tokens", true, Tokenizer.class));
	}

	public class GreedyStringTilingDetectorWorker extends PairwiseDetectorWorker<GreedyStringTilingRawResult<GSTMatch>> {
	
		// Mapping from token strings to integers. Used for hashing
		private HashMap<String, Integer> tokToIntMap;
	
		public GreedyStringTilingDetectorWorker(IDetector parent, ModelDataItem file1Data, ModelDataItem file2Data) {
			super(parent, file1Data, file2Data);
			FillTokToIntMap();
		}
	
	
		/**
		 * Generate the initial hash used in the Rabin fingerprint rolling hash function
		 * @param tokens The array of MarkedTokens to hash. Should be of length mml
		 * @return The hash value as an int
		 */
		private int hash(MarkedToken[] tokens) {
			if (tokens.length > 2) {
				int c = tokens[tokens.length - 1].getValue();
				return (((hash(Arrays.copyOfRange(tokens, 0, tokens.length-1)) * 256 ) % 101) + c) %101;
			} else if (tokens.length == 2){
				int c1 = tokens[0].getValue();
				int c2 = tokens[1].getValue();
				return (((c1 * 256)%101)+c2)%101;
			} else {
				int c = tokens[0].getValue();
				return c % 101;
			}
		}
	
		/**
		 * Generate the next iteration of the Rabin fingerprint rolling hash
		 * @param oldhash Hash value from the previous iteration
		 * @param oldtok The value of the token that has just left the hashing window
		 * @param newtok The value of the token that has just entered the hashing window
		 * @param offset The pattern length base offset
		 * @return The new hash value
		 */
		private int rehash(int oldhash, int oldtok, int newtok, int offset) {
			int unmodded = ((oldhash + 101 - oldtok * offset)*256)+newtok;
			return ((unmodded%101)+101)%101;
		}
	
		private Integer[] generateHashes(MarkedToken[] tokens) {
			int base_offset = 1;
			for (int i=0; i<mml-1; i++) {
				base_offset = (base_offset * 256) % 101;
			}
			int hs = 0;
			List<Integer> hashes = new ArrayList<>();
			for (int i = 0; i < tokens.length - mml; i++) {
				// Generate hashes using Rabin fingerprint rolling hash
				if (i == 0) {
					hs = hash(Arrays.copyOfRange(tokens, i, i + mml));
				} else {
					hs = rehash(hs, tokens[i-1].getValue(), tokens[i+mml-1].getValue(), base_offset);
				}
				hashes.add(hs);
			}
			return hashes.toArray(new Integer[hashes.size()]);
		}
	
		@Override
		public void execute() {
			// Get the tokenised files
			List<IndexedString> tokensF1 = this.file1.getPreProcessedLines("tokens");
			List<IndexedString> tokensF2 = this.file2.getPreProcessedLines("tokens");

			// Arrays to keep track of the number of tokens for each line
			int[] tokensPerLineF1 = new int[this.file1.getFile().getTotalLineCount()+1];
			int[] tokensPerLineF2 = new int[this.file2.getFile().getTotalLineCount()+1];
	
			// Convert IndexedStrings into MarkedTokens
			List<MarkedToken> markedTokensF1 = new ArrayList<>();
			for (IndexedString s : tokensF1) {
				String[] tokens = s.getValue().split(" ");
				for (String t : tokens) {
					MarkedToken mt = new MarkedToken(s.getKey(), t, tokToIntMap.get(t));
					markedTokensF1.add(mt);
					tokensPerLineF1[s.getKey()] += 1;
				}
			}
			List<MarkedToken> markedTokensF2 = new ArrayList<>();
			for (IndexedString s : tokensF2) {
				String[] tokens = s.getValue().split(" ");
				for (String t : tokens) {
					MarkedToken mt = new MarkedToken(s.getKey(), t, tokToIntMap.get(t));
					markedTokensF2.add(mt);
					tokensPerLineF2[s.getKey()] += 1;
				}
			}
	
			// Figure out which of the files is smaller
			List<MarkedToken> larger, smaller;
			ISourceFile largerFile, smallerFile;
			int[] largerTokensPerLine, smallerTokensPerLine;
			if (markedTokensF1.size() > markedTokensF2.size()) {
				larger = markedTokensF1;
				smaller = markedTokensF2;
				largerFile = this.file1.getFile();
				smallerFile = this.file2.getFile();
				largerTokensPerLine = tokensPerLineF1;
				smallerTokensPerLine = tokensPerLineF2;
			} else {
				larger = markedTokensF2;
				smaller = markedTokensF1;
				largerFile = this.file2.getFile();
				smallerFile = this.file1.getFile();
				largerTokensPerLine = tokensPerLineF2;
				smallerTokensPerLine = tokensPerLineF1;
			}
	
			// Generate hashes
			Integer[] smallerHashes = generateHashes(smaller.toArray(new MarkedToken[smaller.size()]));
			Integer[] largerHashes = generateHashes(larger.toArray(new MarkedToken[larger.size()]));
			// Hashes from larger submission are placed into a hash table
			HashMap<Integer, List<Integer>> largerHashMap = new HashMap<>();
			for (int i = 0; i < largerHashes.length; i++) {
				if (largerHashMap.containsKey(largerHashes[i])) {
					largerHashMap.get(largerHashes[i]).add(i);
				} else {
					List<Integer> temp = new ArrayList<>();
					temp.add(i);
					largerHashMap.put(largerHashes[i], temp);
				}
			}
	
			// Main algorithm
			int maxMatch;
			List<GSTMatch> tiles = new ArrayList<>();
			do {
				maxMatch = mml;
				List<GSTMatch> newMatches = new ArrayList<>();
				for (int a = 0; a < smaller.size() - maxMatch; a++) {
					if (smaller.get(a).isMarked()) {
						continue; // skip marked tokens
					}
					List<Integer> possibleMatches = largerHashMap.get(smallerHashes[a]);
					if (possibleMatches != null) {
						for (int b : possibleMatches) {
							if (larger.get(b).isMarked()) {
								continue; // skip marked tokens
							}
							int j = 0;
							while (a+j < smaller.size() && b+j < larger.size() && smaller.get(a+j).getValue() == larger.get(b+j).getValue() 
								&& !smaller.get(a+j).isMarked() && !larger.get(b+j).isMarked()) {
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
								Tuple<Integer, Integer> file1Lines = new Tuple<>(smaller.get(a).getLineNo(), smaller.get(a+j-1).getLineNo());
								Tuple<Integer, Integer> file2Lines = new Tuple<>(larger.get(b).getLineNo(), larger.get(b+j-1).getLineNo());
								int file1LinesTokens = 0;
								for (int i = file1Lines.getKey(); i <= file1Lines.getValue(); i++) {
									file1LinesTokens += smallerTokensPerLine[i];
								}
								int file2LinesTokens = 0;
								for (int i = file2Lines.getKey(); i <= file2Lines.getValue(); i++) {
									file2LinesTokens += largerTokensPerLine[i];
								}
								GSTMatch match = new GSTMatch(smallerFile.getPersistentId(), largerFile.getPersistentId(), a, b, j, file1Lines, file1LinesTokens, file2Lines, file2LinesTokens);
								addMatchIfNotOverlapping(newMatches, match);
							}
						}
					}
				}
	
				for (GSTMatch match : newMatches) {
					for (int i = 0; i < match.getLength(); i++) {
						smaller.get(match.getFirstIndex()+i).mark();
						larger.get(match.getSecondIndex()+i).mark();
					}
					tiles.add(match);
				}
	
			} while (maxMatch > mml);
	
			// Return the result
			GreedyStringTilingRawResult<GSTMatch> res;
			res = new GreedyStringTilingRawResult<GSTMatch>(smallerFile, largerFile);
	
			for (GSTMatch match : tiles) {
				res.addObject(match);
			}
	
			this.result = res;
		}

		private void addMatchIfNotOverlapping(List<GSTMatch> matches, GSTMatch newMatch) {
			for (GSTMatch match : matches) {
				if (match.overlaps(newMatch)) {
					return;
				}
			}
			matches.add(newMatch);
		}
	
		private void FillTokToIntMap() {
			this.tokToIntMap = new HashMap<>();
			tokToIntMap.put("ABSTRACT", 1);
			tokToIntMap.put("ASSERT", 2);
			tokToIntMap.put("BOOLEAN", 3);
			tokToIntMap.put("BREAK", 4);
			tokToIntMap.put("BYTE", 5);
			tokToIntMap.put("CASE", 6);
			tokToIntMap.put("CATCH", 7);
			tokToIntMap.put("CHAR", 8);
			tokToIntMap.put("CLASS", 9);
			tokToIntMap.put("CONST", 10);
			tokToIntMap.put("CONTINUE", 11);
			tokToIntMap.put("DEFAULT", 12);
			tokToIntMap.put("DO", 13);
			tokToIntMap.put("DOUBLE", 14);
			tokToIntMap.put("ELSE", 15);
			tokToIntMap.put("ENUM", 16);
			tokToIntMap.put("EXTENDS", 17);
			tokToIntMap.put("FINAL", 18);
			tokToIntMap.put("FINALLY", 19);
			tokToIntMap.put("FLOAT", 20);
			tokToIntMap.put("FOR", 21);
			tokToIntMap.put("IF", 22);
			tokToIntMap.put("GOTO", 23);
			tokToIntMap.put("IMPLEMENTS", 24);
			tokToIntMap.put("IMPORT", 25);
			tokToIntMap.put("INSTANCEOF", 26);
			tokToIntMap.put("INT", 27);
			tokToIntMap.put("INTERFACE", 28);
			tokToIntMap.put("LONG", 29);
			tokToIntMap.put("NATIVE", 30);
			tokToIntMap.put("NEW", 31);
			tokToIntMap.put("PACKAGE", 32);
			tokToIntMap.put("PRIVATE", 33);
			tokToIntMap.put("PROTECTED", 34);
			tokToIntMap.put("PUBLIC", 35);
			tokToIntMap.put("RETURN", 36);
			tokToIntMap.put("SHORT", 37);
			tokToIntMap.put("STATIC", 38);
			tokToIntMap.put("STRICTFP", 39);
			tokToIntMap.put("SUPER", 40);
			tokToIntMap.put("SWITCH", 41);
			tokToIntMap.put("SYNCHRONIZED", 42);
			tokToIntMap.put("THIS", 43);
			tokToIntMap.put("THROW", 44);
			tokToIntMap.put("THROWS", 45);
			tokToIntMap.put("TRANSIENT", 46);
			tokToIntMap.put("TRY", 47);
			tokToIntMap.put("VOID", 48);
			tokToIntMap.put("VOLATILE", 49);
			tokToIntMap.put("WHILE", 50);
			tokToIntMap.put("DECIMAL_LITERAL", 51);
			tokToIntMap.put("HEX_LITERAL", 52);
			tokToIntMap.put("OCT_LITERAL", 53);
			tokToIntMap.put("BINARY_LITERAL", 54);
			tokToIntMap.put("FLOAT_LITERAL", 55);
			tokToIntMap.put("HEX_FLOAT_LITERAL", 56);
			tokToIntMap.put("BOOL_LITERAL", 57);
			tokToIntMap.put("CHAR_LITERAL", 58);
			tokToIntMap.put("STRING_LITERAL", 59);
			tokToIntMap.put("NULL_LITERAL", 60);
			tokToIntMap.put("LPAREN", 61);
			tokToIntMap.put("RPAREN", 62);
			tokToIntMap.put("LBRACE", 63);
			tokToIntMap.put("RBRACE", 64);
			tokToIntMap.put("LBRACK", 65);
			tokToIntMap.put("RBRACK", 66);
			tokToIntMap.put("SEMI", 67);
			tokToIntMap.put("COMMA", 68);
			tokToIntMap.put("DOT", 69);
			tokToIntMap.put("ASSIGN", 70);
			tokToIntMap.put("GT", 71);
			tokToIntMap.put("LT", 72);
			tokToIntMap.put("BANG", 73);
			tokToIntMap.put("TILDE", 74);
			tokToIntMap.put("QUESTION", 75);
			tokToIntMap.put("COLON", 76);
			tokToIntMap.put("EQUAL", 77);
			tokToIntMap.put("LE", 78);
			tokToIntMap.put("GE", 79);
			tokToIntMap.put("NOTEQUAL", 80);
			tokToIntMap.put("AND", 81);
			tokToIntMap.put("OR", 82);
			tokToIntMap.put("INC", 83);
			tokToIntMap.put("DEC", 84);
			tokToIntMap.put("ADD", 85);
			tokToIntMap.put("SUB", 86);
			tokToIntMap.put("MUL", 87);
			tokToIntMap.put("DIV", 88);
			tokToIntMap.put("BITAND", 89);
			tokToIntMap.put("BITOR", 90);
			tokToIntMap.put("CARET", 91);
			tokToIntMap.put("MOD", 92);
			tokToIntMap.put("ADD_ASSIGN", 93);
			tokToIntMap.put("SUB_ASSIGN", 94);
			tokToIntMap.put("MUL_ASSIGN", 95);
			tokToIntMap.put("DIV_ASSIGN", 96);
			tokToIntMap.put("AND_ASSIGN", 97);
			tokToIntMap.put("OR_ASSIGN", 98);
			tokToIntMap.put("XOR_ASSIGN", 99);
			tokToIntMap.put("MOD_ASSIGN", 100);
			tokToIntMap.put("LSHIFT_ASSIGN", 101);
			tokToIntMap.put("RSHIFT_ASSIGN", 102);
			tokToIntMap.put("URSHIFT_ASSIGN", 103);
			tokToIntMap.put("ARROW", 104);
			tokToIntMap.put("COLONCOLON", 105);
			tokToIntMap.put("AT", 106);
			tokToIntMap.put("ELLIPSIS", 107);
			tokToIntMap.put("WS", 108);
			tokToIntMap.put("MWS", 109);
			tokToIntMap.put("TAB", 110);
			tokToIntMap.put("NEWLINE", 111);
			tokToIntMap.put("BLOCK_COMMENT", 112);
			tokToIntMap.put("LINE_COMMENT", 113);
			tokToIntMap.put("IDENTIFIER", 114);
		}
	
		public class MarkedToken {
			private int lineNo;
			private String name;
			private int value;
			private boolean marked = false;
			
			public MarkedToken(int lineNo, String name, Integer value) {
				this.lineNo = lineNo;
				this.name = name;
				this.value = value;
			}
	
			public int getLineNo() {
				return lineNo;
			}

			public String getName() {
				return name;
			}
	
			public int getValue() {
				return value;
			}
	
			public boolean isMarked() {
				return marked;
			}
	
			public void mark() {
				this.marked = true;
			}
		}
		
	}
	
}