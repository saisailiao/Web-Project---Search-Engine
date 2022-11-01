package sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import common.CommonAnalyzer;

public class Lucene {
	private static File indexDir = new File("c:\\index");

	//selectText为所要查的关键字
	//number为所返回数据条数 
	public String lucene(String selectText,String URL ) throws Exception {
		int number=1000;
		String info = "";
		String fieldName = "text";
		String red1 = "<font color=red>";
		String red2 = "</font>";

		//把文件加载读取内容
		FileInputStream fis = new FileInputStream("C:\\luceneFile\\"+URL);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String a;

		//分词算法
		Analyzer analyzer = new CommonAnalyzer();

		// 目录
		Directory directory = FSDirectory.open(indexDir);
		try {
			// IndexWriter 是 Lucene 用来创建索引的一个核心的类，他的作用是把一个个的 Document 对象加到索引中来
			IndexWriter iwriter = new IndexWriter(directory, analyzer, true,
					IndexWriter.MaxFieldLength.LIMITED);
			iwriter.setMaxFieldLength(25000);
			// Lucene是无法对PDF，TXT，DOC等物理文件建立索引的，它只能对Document文件建立索引，Document是一个虚拟的文件
			while ((a = br.readLine()) != null) {
				Document doc = new Document();
				doc.add(new Field(fieldName, a, Field.Store.YES,
						Field.Index.ANALYZED));
				iwriter.addDocument(doc);
			}
			// Field.Store.YES:为该Field值创建索引
			// Field.Index.TOKENIZED:索引Field的值,使它能被查到
			// Field 对象是用来描述一个文档的某个属性的
			iwriter.close();
			//索引对象
			IndexSearcher isearcher = new IndexSearcher(directory, true);
			QueryParser parser = new QueryParser(Version.LUCENE_29, fieldName,
					analyzer);
			long begin = new Date().getTime();
			Query query = parser.parse(selectText);
			// Hits 用来保存搜索得到的结果
			System.out.println("传来的数量是: " + number);
			TopDocs topDocs = isearcher.search(query, number);
			ScoreDoc[] hits = topDocs.scoreDocs;
			Document hitDoc = null;
			if (hits.length == 0) {
			
			} else {
				System.out.println("查到：" + hits.length + "条记录");
				info += hits.length;
			}
			Highlighter highlighter = null;
			String highLightText = "";
			String getValue = "";
			SimpleHTMLFormatter simpleHTMLFormatter;
			for (int i = 0; i < hits.length; i++) {
				hitDoc = isearcher.doc(hits[i].doc);
				getValue = hitDoc.get(fieldName);
				simpleHTMLFormatter = new SimpleHTMLFormatter(red1, red2);
				highlighter = new Highlighter(simpleHTMLFormatter,
						new QueryScorer(query));
				highLightText = highlighter.getBestFragment(analyzer, "field",
						getValue);
				
			}
			System.out.println("查到：" + hits.length + "条记录");
			System.out.println(info);
			long end = new Date().getTime();
			isearcher.close();
			directory.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		br.close();
		fis.close();
		return info;
	}


}
