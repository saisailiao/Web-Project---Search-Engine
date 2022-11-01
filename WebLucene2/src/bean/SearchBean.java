package bean;
//从数据库中按照列提取出信息
public class SearchBean {
	private String id;
	private String title;
	private String content;
	private String link;
//链接
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
//id
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
//标题
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}
//新闻内容
	public void setContent(String content) {
		this.content = content;
	}
}