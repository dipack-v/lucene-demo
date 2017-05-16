package example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.HighFreqTerms.DocFreqComparator;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class TopPhrases {
    public static void main(String[] args) throws IOException {

        String indexPath = "D:/index";
        Path docPath = Paths.get("D:/workspace/lucene-demo/input/keywords.txt");
        Directory directory = FSDirectory.open(Paths.get(indexPath));
        
        
        Analyzer analyzer = new PipeCharacterAnalyser();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        
        IndexWriter writer = new IndexWriter(directory, iwc);
        indexDoc(writer, docPath);
        System.out.println("Done Indexing...");
        writer.close();
        
        IndexReader reader = DirectoryReader.open(directory);
        findTopPhrases(reader);
        reader.close();
        directory.close();

    }

    private static void indexDoc(IndexWriter writer, Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            Document doc = new Document();
            doc.add(new TextField("contents",
                    new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));

            if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                // New index, so we just add the document (no old document can be there):
                System.out.println("adding " + file);
                writer.addDocument(doc);
            } else {
                // Existing index (an old copy of this document may have been indexed) so
                // we use updateDocument instead to replace the old one matching the exact
                // path, if present:
                System.out.println("updating " + file);
                writer.updateDocument(new Term("path", file.toString()), doc);
            }
        }
    }

    private static void findTopPhrases(IndexReader reader) throws IOException {
        DocFreqComparator cmp = new HighFreqTerms.DocFreqComparator();
        TermStats[] highFreqTerms;
        try {
            highFreqTerms = HighFreqTerms.getHighFreqTerms(reader, 5000, "contents", cmp);
            List<String> terms = new ArrayList<String>(highFreqTerms.length);
            for (TermStats ts : highFreqTerms) {
                System.out.println(">>>>" + ts.termtext.utf8ToString() + " count" + ts.totalTermFreq);
                terms.add(ts.termtext.utf8ToString());
            }

            for (String term : terms) {
                // System.out.println(">>>>" + term);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        reader.close();
    }
}

class PipeCharacterAnalyser extends Analyzer {
    public PipeCharacterAnalyser() {
        super();
    }

    @Override
    protected TokenStreamComponents createComponents(String arg0) {
        Tokenizer tokenizer = new PipeCharacterTokenizer();
        return new TokenStreamComponents(tokenizer, new TrimFilter(tokenizer));
    }
}

class PipeCharacterTokenizer extends CharTokenizer {
    public PipeCharacterTokenizer() {
        super();
    }

    @Override
    protected boolean isTokenChar(final int character) {
        return '|' != character;
    }

}
