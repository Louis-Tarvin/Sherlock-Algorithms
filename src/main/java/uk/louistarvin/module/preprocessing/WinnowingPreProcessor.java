package uk.louistarvin.module.preprocessing;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.IGeneralPreProcessor;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.ILexerSpecification;
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.StandardLexerSpecification;

public class WinnowingPreProcessor implements IGeneralPreProcessor {

    @Override
    public ILexerSpecification getLexerSpecification() {
        return new StandardLexerSpecification();
    }

    @Override
    public List<? extends Token> process(List<? extends Token> tokens, Vocabulary vocab, String lang) {
        List<Token> result = new ArrayList<>();

		for (Token t : tokens) {

            // Ignore comments and whitespace
			switch (StandardLexerSpecification.channels.values()[t.getChannel()]) {
				case DEFAULT:
                    // Ignore identifiers
                    if (!(t.getType() == 114)) {
                        result.add(t);
                    } else {
                        result.add(new CommonToken(114, "I"));
                    }
					break;
				// case WHITESPACE:
				// 	result.add(t);
				// 	break;
				// case LONG_WHITESPACE:
				// 	result.add(t);
				// 	break;
				default:
					break;
			}
		}

        return result;
    }
    
}
