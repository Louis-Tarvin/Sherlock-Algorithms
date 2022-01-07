package uk.louistarvin.module.preprocessing;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import uk.ac.warwick.dcs.sherlock.api.util.IndexedString;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.IAdvancedPreProcessor;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaLexer;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaParser;
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaParserBaseListener;

import java.util.*;

// NOTE: this class may not be needed
public class TokenizerJava implements IAdvancedPreProcessor<JavaLexer> {

	@Override
	public List<IndexedString> process(JavaLexer lexer) {
		List<IndexedString> fields = new LinkedList<>();

		lexer.reset();
		CommonTokenStream tokStream = new CommonTokenStream(lexer);
		tokStream.fill();
		System.out.println(tokStream.getText());
		for (Token t : tokStream.getTokens()) {
			System.out.println(t.getType());
		}
		JavaParser parser = new JavaParser(tokStream);

		ParseTreeWalker.DEFAULT.walk(new JavaParserBaseListener() {
			@Override
			public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
				fields.add(new IndexedString(ctx.start.getLine(), ctx.getText().split("=")[0]));
			}

			//locals
			@Override
			public void enterLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
				fields.add(new IndexedString(ctx.start.getLine(), ctx.getText().split("=")[0]));
			}
		}, parser.compilationUnit());

		System.out.println("field -> " + fields.toString());
		return fields;
	}
}
