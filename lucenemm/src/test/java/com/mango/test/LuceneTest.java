package com.mango.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import com.hankcs.lucene.HanLPAnalyzer;

public class LuceneTest {
	@Test
	public void indexWrite() throws Exception {
		// 打开索引库
		Directory directory = FSDirectory.open(Paths.get("D:\\index"));
		// 标准分词器
		// IndexWriterConfig indexWriterConfig = new IndexWriterConfig(new
		// StandardAnalyzer());
		// IndexWriterConfig indexWriterConfig = new IndexWriterConfig(new
		// SmartChineseAnalyzer());
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(new HanLPAnalyzer());
		// 写入索引库对象
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);

		// 数据源
		File sourceFile = new File("D:\\source");
		File[] listFiles = sourceFile.listFiles();
		for (File file : listFiles) {
			// 获取文件相关数据
			String name = file.getName();
			String content = FileUtils.readFileToString(file);
			String path = file.getPath();
			long size = FileUtils.sizeOf(file);
			Field nameField = new TextField("name", name, Store.YES);
			Field contentField = new TextField("content", content, Store.YES);
			Field pathField = new StoredField("path", path);
			// Field sizeField = new StoredField("size", size);
			// 不分词,不索引,不存储
			Field sizeField = new LongPoint("size", size);

			Document document = new Document();
			document.add(nameField);
			document.add(contentField);
			document.add(pathField);
			document.add(sizeField);
			indexWriter.addDocument(document);
		}
		indexWriter.close();
	}

	@Test
	public void deleteindexWrite() throws Exception {
		// 打开索引库
		Directory directory = FSDirectory.open(Paths.get("D:\\index"));
		// 标准分词器
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig();
		// 写入索引库对象
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		indexWriter.deleteAll();
		indexWriter.close();
	}

	@Test
	public void queryIndex() throws Exception {
		Directory directory = FSDirectory.open(Paths.get("D:\\\\index"));
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		// 查询所有
		Query query1 = new MatchAllDocsQuery();
		// 不分词单域查询
		Query query2 = new TermQuery(new Term("name", "中国人"));
		// 范围查询
		Query query3 = LongPoint.newRangeQuery("size", 10, 500);
		// 组合查询条件
		BooleanClause bc1 = new BooleanClause(query1, Occur.MUST);
		BooleanClause bc2 = new BooleanClause(query2, Occur.MUST_NOT);
		// 不分词组合查询
		Query query4 = new BooleanQuery.Builder().add(bc1).add(bc2).build();
		// 分词单域查询
		QueryParser queryParser = new QueryParser("name", new HanLPAnalyzer());
		Query query5 = queryParser.parse("spring是一个很强大");
		// 分词多域
		MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(new String[] { "name", "content" },
				new HanLPAnalyzer());
		Query query6 = multiFieldQueryParser.parse("spring是一个很强大");
		TopDocs topDocs = indexSearcher.search(query6, 10);
		int total = topDocs.totalHits;
		System.out.println(total);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			int i = scoreDoc.doc;
			System.out.println(i);
			Document doc = indexSearcher.doc(i);
			System.out.println(doc.get("name"));
			System.out.println(doc.get("content"));
			System.out.println(doc.get("path"));
			System.out.println(doc.get("size"));
		}
		indexReader.close();
	}
}
