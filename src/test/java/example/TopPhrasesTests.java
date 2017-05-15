package example;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class TopPhrasesTests {

    public void testAnalyser() throws IOException {
        String text = "youtube | facebook | amazon | gmail | google | craigslist | ebay | netflix | yahoo mail | walmart | google maps | yahoo | facebook login | google translate | home depot | weather | maps | hotmail | translate | target | paypal | pornhub | usps tracking | lowes | best buy | google docs | pinterest | pandora | calculator | youtube to mp3 | bank of america | wells fargo | google drive | mapquest | cnn | google classroom | twitter | zillow | linkedin | instagram | speed test | capital one | espn | not at all | pizza hut | you tube | costco | ups tracking | usps | indeed ";
        Analyzer analyzer = new PipeCharacterAnalyser();

        TokenStream stream = analyzer.tokenStream(null, text);
        CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            System.out.println(cattr.toString());
        }
        stream.end();
        stream.close();
        analyzer.close();
    }
}
