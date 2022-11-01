package bean;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * SearchLogic.java
 * 
 * @version 1.0
 * @createTime Lucene���ݿ����
 */
//lucene 数据库检索
public class SearchLogic {
	private static Connection conn = null;// 数据库链接对象
	private static Statement stmt = null;
	private static ResultSet rs = null;//数据库查询返回结果
	private final String searchDir = "C:\\index";//索引文件所在路径
	private static File indexFile = null;// 索引文件
	private static Searcher searcher = null;//搜索
	private static Analyzer analyzer = null;// 分词
	//索引页面缓冲时长初始化
	private final int maxBufferedDocs = 500;


	//获取数据库数据
	public List<SearchBean> getResult(String queryStr) throws Exception {
		List<SearchBean> result = null;
		conn = JdbcUtil.getConnection();
		if (conn == null) {
			throw new Exception("无法连接数据库");
		}
		String sql = "select *   from content";
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);//查询数据库数据
			this.createIndex(rs); // 给数据库创建索引,此处执行一次，不要每次运行都创建索引，以后数据有更新可以后台调用更新索引
			TopDocs topDocs = this.search(queryStr);//搜索
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			result = this.addHits2List(scoreDocs);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("查询 sql : " + sql);
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
		}
		return result;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getJSON(String keyword) throws Exception {
		SearchLogic logic = new SearchLogic();
		//获取查询结果
		List<SearchBean> result = logic.getResult(keyword.toLowerCase());
		
		List list = new ArrayList();
		for (int i = 0; i < result.size(); i++) {
			//循环把搜索结果加入到list中，在searchBean内
			HashMap dynBean = new HashMap();
			dynBean.put("id", result.get(i).getId());
			dynBean.put("content", result.get(i).getContent());
			dynBean.put("title", result.get(i).getTitle());
			dynBean.put("link", result.get(i).getLink());
			list.add(dynBean);
		}
		System.out.println("长度：" + list.size());
		if (list.size() > 0) {
			//格式化成json格式并输出到前台，前台数据为json格式
			JSONArray json = JSONArray.fromObject(list);
			return json.toString();
		} else {
			return "[]";
		}

	}

	//为数据库检索数据创建索引
	private void createIndex(ResultSet rs) throws Exception {
		Directory directory = null;
		IndexWriter indexWriter = null;

		try {
			// 索引文件对象
			indexFile = new File(searchDir);
			// 如果不存在则创建
			if (!indexFile.exists()) {
				indexFile.mkdir();
			}
			// 打开索引文件
			directory = FSDirectory.open(indexFile);
			analyzer = new IKAnalyzer();// �ִ�

			indexWriter = new IndexWriter(directory, analyzer, true,
					IndexWriter.MaxFieldLength.UNLIMITED);
			indexWriter.setMaxBufferedDocs(maxBufferedDocs);
			Document doc = null;
			while (rs.next()) {
				// 创建文档
				doc = new Document();
				// 链接
				Field link = new Field("link", String.valueOf(rs
						.getString("link")), Field.Store.YES,
						Field.Index.NOT_ANALYZED, TermVector.NO);
				// 标题
				Field title = new Field("title",
						rs.getString("title") == null ? "" : rs
								.getString("title"), Field.Store.YES,
						Field.Index.ANALYZED, TermVector.NO);
				// 内容
				Field content = new Field("content",
						rs.getString("content") == null ? "" : rs
								.getString("content"), Field.Store.YES,
						Field.Index.ANALYZED, TermVector.NO);
				//将三者加入到doc中
				doc.add(link);
				doc.add(title);
				doc.add(content);
				//将添加好数据的doc加入到索引文件中
				indexWriter.addDocument(doc);
			}

			indexWriter.optimize();
			indexWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//搜索索引
	private TopDocs search(String queryStr) throws Exception {
		if (searcher == null) {
			indexFile = new File(searchDir);
			// 打开索引文件
			searcher = new IndexSearcher(FSDirectory.open(indexFile));
		}
		QueryParser parser = new QueryParser(Version.LUCENE_29, "content",
				new IKAnalyzer());
		Query query = new TermQuery(new Term("content", queryStr));
		TopDocs topDocs = searcher.search(query, null, 100);// ����ƥ�䵽��ǰ100����¼
		return topDocs;
	}

	//将搜索索引的结果返回，并添加到list中
	private List<SearchBean> addHits2List(ScoreDoc[] scoreDocs)
			throws Exception {
		List<SearchBean> listBean = new ArrayList<SearchBean>();
		SearchBean bean = null;
		//返回结果并添加到List中
		for (int i = 0; i < scoreDocs.length; i++) {
			int docId = scoreDocs[i].doc;
			Document doc = searcher.doc(docId);
			bean = new SearchBean();
			bean.setId(doc.get("id"));
			bean.setLink(doc.get("link"));
			bean.setTitle(doc.get("title"));
			bean.setContent(doc.get("content"));
			listBean.add(bean);
		}
		return listBean;
	}

}