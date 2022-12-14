package com.assignment.lucene.lucenmavenprojectnikhil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.assignment.lucene.lucenmavenprojectnikhil.constants.CommonConstants;
import com.assignment.lucene.lucenmavenprojectnikhil.model.QueryModel;
import com.assignment.lucene.lucenmavenprojectnikhil.parser.Parser;

public class Searcher {

	private static Logger log = LoggerFactory.getLogger(Searcher.class);

	private static int NUM_RESULTS = 1;

	public static void runQueries(String queryPath, int numResults, String selectedAnalyser,
			String selectedSimilarity) {
		try {
			Directory directory = FSDirectory.open(Paths.get(CommonConstants.INDEX_PATH));
			DirectoryReader directoryReader = DirectoryReader.open(directory);

			IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
			indexSearcher.setSimilarity(Indexer.getSelectedSimilarityObject(selectedSimilarity));
			Analyzer analyzer = Indexer.getSelectedAnalyzerObject(selectedAnalyser);

			File resultFile = new File(CommonConstants.OUTPUT_FILE_PATH);
			resultFile.getParentFile().mkdirs();
			PrintWriter writer = new PrintWriter(resultFile, StandardCharsets.UTF_8.name());

			ArrayList<QueryModel> queries = Parser.parseQuery(queryPath);

			MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
					new String[] { CommonConstants.TITLE, CommonConstants.AUTHOR, CommonConstants.WORDS }, analyzer);

			NUM_RESULTS = numResults;
			log.debug("Executing the fetched queries, max_hits set to: " + NUM_RESULTS);

			writer.println("Query ID" + "\t" + "Document ID" + "\t" + "Hits Score" + "\t" + "Analyser Used" + "\t"
					+ "Similarity Used");

			for (QueryModel element : queries) {
				String queryString = QueryParser.escape(element.getQuery().trim());
				Query query = queryParser.parse(queryString);
				search(indexSearcher, query, writer, queries.indexOf(element) + 1, selectedAnalyser,
						selectedSimilarity);
			}

			directoryReader.close();
			writer.close();
			directory.close();
			log.info("Searching complete output written to " + CommonConstants.OUTPUT_FILE_PATH);

		} catch (IOException | ParseException e) {
			log.error("Exception occured while running queries", e);
		}
	}

	public static void search(IndexSearcher is, Query query, PrintWriter writer, int queryID, String selectedAnalyser,
			String selectedSimilarity) throws IOException {
		ScoreDoc[] hits = is.search(query, NUM_RESULTS).scoreDocs;
		for (int i = 0; i < hits.length; i++) {
			Document hitDocument = is.doc(hits[i].doc);
			writer.println(queryID + " 0 " + hitDocument.get("docid") + " 0 " + hits[i].score + " "+selectedAnalyser + " "+selectedSimilarity);
		}
	}

}
