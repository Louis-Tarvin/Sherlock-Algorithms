package uk.louistarvin.module.preprocessing;

import org.antlr.v4.runtime.*;
import uk.ac.warwick.dcs.sherlock.api.util.IndexedString;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.IAdvancedPreProcessor;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaLexer;

import java.util.*;

public class TokenCountsJava implements IAdvancedPreProcessor<JavaLexer> {

	@Override
	public List<IndexedString> process(JavaLexer lexer) {
		List<IndexedString> fields = new LinkedList<>();

		lexer.reset();
		CommonTokenStream tokStream = new CommonTokenStream(lexer);
		tokStream.fill();

		int totalOperatorCount = 0;
		int uniqueOperatorCount = 0;
		int totalOperandCount = 0;
		int uniqueOperandCount = 0;
		int totalControlStatements = 0;
		HashSet<Integer> seenOperators = new HashSet<>();
		HashSet<String> seenOperands = new HashSet<>();

		for (Token t : tokStream.getTokens()) {
			int type = t.getType();
			if(type >= 70 && type <= 103) {
				// Token is an operator
				totalOperatorCount++;
				if (!seenOperators.contains(type)) {
					seenOperators.add(type);
					uniqueOperatorCount++;
				}
			} else if (
					(type >= 51 && type <= 60) //literal
					|| type == 114 //identifier
			) {
				// Token is an operand
				totalOperandCount++;
				if (!seenOperands.contains(t.getText())) {
					seenOperands.add(t.getText());
					uniqueOperandCount++;
				}

			} else if (type == 4 //break
					|| type == 11 //continue
					|| type == 13 //do
					|| type == 15 //else
					|| type == 19 //finally
					|| type == 21 //for
					|| type == 22 //if
					|| type == 23 //goto
					|| type == 36 //return
					|| type == 41 //switch
					|| type == 47 //try
					|| type == 50 //while
			) {
				// Token is a control statement
				totalControlStatements++;
			}
		}
		// Index 0 -> total operators
		fields.add(new IndexedString(0, String.valueOf(totalOperatorCount)));
		// Index 1 -> unique operators
		fields.add(new IndexedString(1, String.valueOf(uniqueOperatorCount)));
		// Index 2 -> total operands
		fields.add(new IndexedString(2, String.valueOf(totalOperandCount)));
		// Index 3 -> unique operands
		fields.add(new IndexedString(3, String.valueOf(uniqueOperandCount)));
		// Index 4 -> control statements 
		fields.add(new IndexedString(4, String.valueOf(totalControlStatements)));

		return fields;
	}
}
