package uk.louistarvin.module.preprocessing;

import java.util.List;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.IGeneralPreProcessor;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.ILexerSpecification;
import uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing.StandardLexerSpecification;

public class Tokenizer implements IGeneralPreProcessor {

    @Override
    public ILexerSpecification getLexerSpecification() {
        return new StandardLexerSpecification();
    }

    @Override
    public List<? extends Token> process(List<? extends Token> tokens, Vocabulary vocab, String lang) {
        System.out.println("tokens" + tokens);
        return tokens;
    }

}
