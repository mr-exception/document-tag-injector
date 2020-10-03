package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			Directory memoryIndex = new RAMDirectory();
			StandardAnalyzer analyzer = new StandardAnalyzer();
			IndexWriter writter = new IndexWriter(memoryIndex, analyzer, true);
			
			Document doc0 = createDocument("nuture", "trees and forests moonlights and rain", "nuture");
			writter.addDocument(doc0);

			Document doc1 = createDocument("city", "lights traffic people house and streets", "streets");
			writter.addDocument(doc1);
			
			writter.close();
			System.out.println("finished indexing");

			Query query = new QueryParser("content", analyzer).parse("trees trees and streets");
			IndexReader indexReader = IndexReader.open(memoryIndex);
			IndexSearcher searcher = new IndexSearcher(indexReader);
			Hits hits = searcher.search(query);
			for (int i = 0; i < hits.length(); i++) {
				Document doc = hits.doc(i);
				System.out.println(doc.get("tags"));
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Document createDocument(String title, String content, String tags) {
		Document document = new Document();

		document.add(new Field("title", title, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field("content", content, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field("tags", tags, Field.Store.YES, Field.Index.TOKENIZED));
		return document;
	}
	
}
