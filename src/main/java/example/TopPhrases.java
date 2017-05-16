package example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeSet;

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

		String indexPath = "target/index";
		Path docPath = Paths.get("keywords.txt");
		Directory directory = FSDirectory.open(Paths.get(indexPath));

		Analyzer analyzer = new PipeCharacterAnalyser();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);

		IndexWriter writer = new IndexWriter(directory, iwc);
		System.out.println("Started Indexing...");
		indexDoc(writer, docPath);
		System.out.println("Done Indexing...");
		writer.close();

		IndexReader reader = DirectoryReader.open(directory);
		findTopPhrases(reader);
		analyzer.close();
		reader.close();
		directory.close();

	}

	private static void indexDoc(IndexWriter writer, Path file) throws IOException {
		try (InputStream stream = Files.newInputStream(file)) {
			Document doc = new Document();
			doc.add(new TextField("contents",
					new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));

			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
				// New index, so we just add the document (no old document can
				// be there):
				System.out.println("adding " + file);
				writer.addDocument(doc);
			} else {
				// Existing index (an old copy of this document may have been
				// indexed) so
				// we use updateDocument instead to replace the old one matching
				// the exact
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
			highFreqTerms = HighFreqTerms.getHighFreqTerms(reader, 100000, "contents", cmp);
			TreeSet<PhraseCount> set = new TreeSet<PhraseCount>();
			for (TermStats ts : highFreqTerms) {
				set.add(new PhraseCount(ts.termtext.utf8ToString(), ts.totalTermFreq));
			}

			for (PhraseCount phrase : set) {
				System.out.println(phrase);
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

class PhraseCount implements Comparable<PhraseCount> {
	private final String phrase;
	private final long count;

	public PhraseCount(String word, long count) {
		this.phrase = word;
		this.count = count;
	}

	public String getPhrase() {
		return phrase;
	}

	public long getCount() {
		return count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((phrase == null) ? 0 : phrase.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PhraseCount other = (PhraseCount) obj;
		if (phrase == null) {
			if (other.phrase != null)
				return false;
		} else if (!phrase.equals(other.phrase))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PhraseCount [phrase=" + phrase + ", count=" + count + "]";
	}

	@Override
	public int compareTo(PhraseCount o) {
		long c = o.count - this.count;
		if (c != 0) {
			return (int) c;
		} else {
			return this.phrase.compareTo(o.phrase);
		}
	}
}
