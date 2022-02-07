# Sherlock-Algorithms
Module for the Sherlock plagiarism detector containing additional algorithms. Created as part of my third-year university project.

## About
This module adds three new plagiarism detection algorithms to the [Sherlock plagiarism detector](https://github.com/DCS-Sherlock/Sherlock). Below is a brief description of each of the algorithms.

### Greedy String Tiling
Converts each submission into a stream of tokens, then performs Greedy String Tiling to find maximal sets of matches. The minimum match length is an adjustable parameter

### Winnowing
Converts the submission into K-grams, hashing each one, then chooses a subset of these hashes to act as the "Fingerprint" of the submission. Submission fingerprints are then compared to find areas of overlap.

The paper detailing the algorithm is available [here](https://theory.stanford.edu/~aiken/publications/papers/sigmod03.pdf).

The guarantee threshold and noise threshold are adjustable parameters.

### Attribute Counting
Compares submissions based on the counts of certain attributes. The following attributes are compared:
- Number of unique operators
- Number of unique operands
- Total number of operators
- Total number of operands
- Code lines (ignoring blank and comment lines)
- Variables declared (and used)
- Total number of control statements

> Note: due to Sherlock not being designed for attribute counting methods, a workaround was used to display the correct pairwise scores. A side effect of this is that individual file scores are always 100, which can be ignored.

## Usage

To build and run the module, both the executable and dev JAR files, which are typically named `Sherlock-x.x.x.jar` and `Sherlock-x.x.x-dev.jar` respectively, need to be placed in the libs/ directory.

You can then run `./gradlew(.bat) run` to launch Sherlock with the module.
