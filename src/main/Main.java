package main;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Main {

	private static Directory memoryIndex;
	private static StandardAnalyzer analyzer;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			memoryIndex = new RAMDirectory();
			analyzer = new StandardAnalyzer();

			System.out.println("starting the index progress...");
			runIndexStep();
			System.out.println("finished indexing!");
			runTestStep();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static ArrayList<String> getTags(String term) throws ParseException, IOException {
		ArrayList<String> result = new ArrayList<String>();
		Query query = new QueryParser("content", analyzer).parse(term);
		IndexReader indexReader = IndexReader.open(memoryIndex);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		Hits hits = searcher.search(query);
		for (int i = 0; i < hits.length(); i++) {
			Document doc = hits.doc(i);
			String tag = doc.get("tag");
			result.add(tag);
		}
		return result;
	}

	private static Document createDocument(String title, String content, String tag) {
		Document document = new Document();

		document.add(new Field("title", title, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field("content", content, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field("tag", tag, Field.Store.YES, Field.Index.TOKENIZED));
		return document;
	}

	private static void runIndexStep() throws IOException, JSONException {
		IndexWriter writter = new IndexWriter(memoryIndex, analyzer, true);

		String content = Files.readString(Paths.get("./data.json"), StandardCharsets.UTF_8);
		JSONArray docs = new JSONArray(content);
		for (int i = 0; i < docs.length(); i++) {
			JSONObject jsonDoc = docs.getJSONObject(i);
			Document doc = createDocument(jsonDoc.getString("title"), jsonDoc.getString("content"),
					jsonDoc.getString("tag"));
			writter.addDocument(doc);
		}

		writter.close();
	}

	private static String getBestTag(ArrayList<String> suggested) {
		HashMap<String, Integer> result = new HashMap<>();
		for (int i = 0; i < suggested.size(); i++) {
			String key = suggested.get(i);
			if (result.containsKey(key))
				result.put(key, result.get(key) + 1);
			else
				result.put(key, 1);
		}
		String tag = "none";
		int bestScore = -1;
		for (String key: suggested) {
			if (result.get(key) > bestScore) {
				tag = key;
				bestScore = result.get(key);
			}
		}
		return tag;
	}

	private static void runTestStep() throws ParseException, IOException, JSONException {
		String content = Files.readString(Paths.get("./input.json"), StandardCharsets.UTF_8);
		JSONArray docs = new JSONArray(content);
		for (int i = 0; i < docs.length(); i++) {
			JSONObject jsonDoc = docs.getJSONObject(i);
			System.out.println("=========================================");
			String term = jsonDoc.getString("content");
			System.out.println(term);
			String expected_tag = jsonDoc.getString("tag");
			String tag = getBestTag(getTags(term));
			System.out.println("expected tag: " + expected_tag);
			System.out.println("suggested tag: " +tag);
		}
	}

}
