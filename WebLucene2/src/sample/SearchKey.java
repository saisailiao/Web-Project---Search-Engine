package sample;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import bean.JdbcUtil;
import bean.SearchLogic;

@SuppressWarnings("serial")
public class SearchKey extends HttpServlet {

	public SearchKey() {
		super();
	}

	@Override
	public void destroy() {
		super.destroy(); 
	}

	private static Connection conn = null;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		String action=request.getParameter("action");
		String sString = "";
		if (action.equals("SearchKey")) {
			sString=SearchKey(request,response);
		}else if (action.equals("Searchword")) {
			sString=Searchword(request,response);
		}
		
		out.print(sString);
	}

	private String SearchKey(HttpServletRequest request,
			HttpServletResponse response) throws UnsupportedEncodingException {
		String keyword = request.getParameter("keyword");
		keyword = URLDecoder.decode(keyword, "UTF-8");

		if (conn == null) {
			return "";
		}
		JdbcUtil db = new JdbcUtil();
		ResultSet rs=db.queryExectue("select  * from tb_Filter ");
		try {
			while (rs.next()) {
				keyword=keyword.replace(rs.getString("KeyWord"), "");
				System.out.println(rs.getString("KeyWord"));
				
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("keyword:"+keyword);
		String[] strings = keyword.split(" ");

		for (int i = 0; i < strings.length; i++) {
			if (strings[i].length() > 0) {
				db.updateExecute("insert into search_his(KeyWord,SearchTime)values('"+ strings[i] + "',now())");
			}

		}
		SearchLogic dal = new SearchLogic();
		String sString = "";
		try {
			sString = dal.getJSON(keyword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sString;
	}
	private String Searchword(HttpServletRequest request,
			HttpServletResponse response) throws UnsupportedEncodingException {

//连接数据库，用于调出搜索历史
		conn = JdbcUtil.getConnection();
		if (conn == null) {
			return "";
		}

		JdbcUtil db = new JdbcUtil();
		ResultSet rs=db.queryExectue("select * from search_his order by searchTime desc limit 3");
		String sString = "";
		List list = new ArrayList();
		//将搜索历史以json的形式传给前端
		try {
			while (rs.next()) {
				HashMap dynBean = new HashMap();
				dynBean.put("KeyWord", rs.getString("KeyWord"));
				list.add(dynBean);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		JSONArray json = JSONArray.fromObject(list);

		return json.toString();
	}
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out
				.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");
		out.print(this.getClass());
		out.println(", using the POST method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	@Override
	public void init() throws ServletException {
		// Put your code here
	}

}
