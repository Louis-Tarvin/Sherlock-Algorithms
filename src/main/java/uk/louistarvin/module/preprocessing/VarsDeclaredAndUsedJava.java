package uk.louistarvin.sherlock.module.model.extra.preprocessing;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import uk.ac.warwick.dcs.sherlock.api.util.IndexedString;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.IAdvancedPreProcessor;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaLexer;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaParser;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaParserBaseListener;

import java.util.*;

public class VarsDeclaredAndUsedJava implements IAdvancedPreProcessor<JavaLexer> {

	@Override
	public List<IndexedString> process(JavaLexer lexer) {

		List<IndexedString> fields = new LinkedList<>();
		HashSet<String> variablesDeclared = new HashSet<>();
		HashSet<String> variablesUsed = new HashSet<>();
		List<Integer> declaredLines = new ArrayList<>();

		// Get declared variables
		lexer.reset();
		JavaParser parser = new JavaParser(new CommonTokenStream(lexer));

		ParseTreeWalker.DEFAULT.walk(new JavaParserBaseListener() {
			//globals
			@Override
			public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
				variablesDeclared.add(ctx.getChild(1).getText().split("=")[0]);
				declaredLines.add(ctx.start.getLine());
			}

			//locals
			@Override
			public void enterLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
				variablesDeclared.add(ctx.getChild(1).getText().split("=")[0]);
				declaredLines.add(ctx.start.getLine());
			}
		}, parser.compilationUnit());

		// Get used variables
		lexer.reset();
		CommonTokenStream tokStream = new CommonTokenStream(lexer);
		tokStream.fill();

		for (Token t : tokStream.getTokens()) {
			// Check if token is an identifier
			if (t.getType() == 114) {
				// Check that this is not a declaration
				if (!declaredLines.contains(t.getLine())) {
					variablesUsed.add(t.getText());
				}
				
			}
		}

		// Work out the count of variables declared and used
		int count = 0;
		for (String v : variablesDeclared) {
			if (variablesUsed.contains(v)) {
				count += 1;
			}
		}
		fields.add(new IndexedString(0, String.valueOf(count)));

		return fields;
	}

}
