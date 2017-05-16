package example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;

public class PipeCharacterAnalyser extends Analyzer {
    public PipeCharacterAnalyser() {
        super();
    }

    @Override
    protected TokenStreamComponents createComponents(String arg0) {
        Tokenizer tokenizer = new PipeCharacterTokenizer();
        return new TokenStreamComponents(tokenizer, new TrimFilter(tokenizer));
    }
}
