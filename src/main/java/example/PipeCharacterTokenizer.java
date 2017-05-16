package example;

import org.apache.lucene.analysis.util.CharTokenizer;

public class PipeCharacterTokenizer extends CharTokenizer {
    public PipeCharacterTokenizer() {
        super();
    }

    @Override
    protected boolean isTokenChar(final int character) {
        return '|' != character;
    }

}