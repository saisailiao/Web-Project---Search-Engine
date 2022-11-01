<%@ page contentType="text/html;charset=utf-8" language="java"%>
<html>
	<head>
		<%
			String path = request.getContextPath();
		%>
		<meta http-equiv="content-type" content="text/html; charset=UTF-8">
		<title>数据搜索</title>

<style type="text/css">
.highlight {
	background: yellow;
	color: #CC0000;
}

#div2 {
	width:50%;
	text-align: left;
	margin-top: 5px;
	width: 110px;
	border: 2px solid #FFC0CB;
	text-align: left;
	font-size: 12px;
	
}
#div3{
	width:50%;
	position:absolute;
	left:0;
	top:0;
}
#div1{
	width:50%;
	float: left;
}
.history {
	text-align: left;
	border: 3px solid #CC0;
}

.history span {
	color: #363;
	font-size: 12px;
	margin-left: 10px;
	float: left;
}

.a_link {
	width: 30px;
	line-height: 30px;
	height: 30px;
	border: 1px solid #CCC;
	text-align: center;
	display: block;
	float: left;
	cursor: pointer;
	color: #333;
	margin-left: 10px;
}

.a_link:hover {
	background: #F2F2F2;
}

.a_cur {
	background: #ffa405;
	width: 30px;
	line-height: 30px;
	height: 30px;
	border: 1px solid #fe8101;
	text-align: center;
	display: block;
	float: left;
	cursor: pointer;
	color: #999;
	margin-left: 10px;
	text-decoration: none;
}

 html {
      height: 100%;
    }

    body {
      height: 100%;
    }
    .container {
      height: 40%;
      width: 595px;
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
    }

    .bgDiv {
      box-sizing: border-box;
      width: 595px;
      height: 55px;
      position: relative;
    /* position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%); */
    }

    .search-input-text {
      border: 1px solid #b6b6b6;
      width: 495px;
      background: #fff;
      height: 51px;
      line-height: 33px;
      font-size: 18px;
      padding: 3px 0 0 7px;
    }

    .search-input-button {
      width: 90px;
      height: 50px;
      color: #fff;
      font-size: 16px;
      letter-spacing: 3px;
      background: #AFEEEE;
      border: .5px solid #AFEEEE;
      margin-left: -5px;
      vertical-align: top;
      opacity: .9;
    }

    .search-input-button:hover {
      opacity: 1;
      box-shadow: 0 1px 1px #333;
      cursor: pointer;
    }

    .suggest {
      width: 502px;
      position: absolute;
      top: 38px;
      border: 1px solid #999;
      background: #AFEEEE;
      display: none;
    }

    .suggest ul {
      list-style: none;
      margin: 0;
      padding: 0;
    }

    .suggest ul li {
      padding: 3px;
      font-size: 17px;
      line-height: 25px;
      cursor: pointer;
    }

    .suggest ul li:hover {
      background-color: #e5e5e5
    }
</style>

		<script type="text/javascript" src="<%=path%>/js/jquery.js">
</script>


		<script type="text/javascript">
$(function() {
	getSearchHis();
})
var firstText = "";
var len = 0;
var total = 0;
var curindex = 0;

//获取开始时间
function showTime() {
	d = new Date();
	var s = "";
	s += d.getYear() + "-";
	s += (d.getMonth() + 1) + "-";
	s += d.getDate() + " ";
	s += d.getHours() + ":";
	s += d.getMinutes() + ":";
	s += d.getSeconds() + ":";
	s += d.getMilliseconds();
	return s;
}

function search100(index) {
	if (document.getElementById("checkText").value == "") {
		alert("请输入关键字！");
		return;
	}
	var d1 = showTime();
	$("#div3").html("");
	var ln = "";
	$
			.getJSON(
					"servlet/SearchKey?action=SearchKey&t="
							+ new Date().getTime(),
					{
						keyword : encodeURI(document
								.getElementById("checkText").value)
					},
					function(data) {
						ln = data.length;
						if (data.length > 0) {
							var start = index * 5;
							var end = Math.min(data.length, start + 5);
							for ( var i = start; i < end; i++) {
								var div = '<div class="b_context_box">';
								div += ' <div class="div_title"><a target="_blank" style="color:#00008B;text-decoration:underline;" href="'
										+ data[i].link
										+ '">'
										+ data[i].title
										+ '</a></div>'
								div += ' <div class="div_content">' + data[i].content + '</div>'
								div += '</div>';
								$("#div3").append(div);
								var ar = document.getElementById("checkText").value
										.replace(/\s+/g, ' ').split(" ");
								for ( var jj = 0; jj < ar.length; jj++) {
									$("#div3").highlight(ar[jj]);
								}

							}
							if (data.length > 5) {
								var link = '<div style="padding:10px 0 0 10px;">';
								var m = Math.ceil(data.length / 5);
								for ( var i = 0; i < m; i++) {
									if (index == i) {
										link += '<a href="javascript:void(0);" onclick="javascript:void(0);" class="a_cur">' + (i + 1) + '</a>';
									} else {
										link += '<a href="javascript:void(0);" onclick="javascript:search100('
												+ i
												+ ');" class="a_link">'
												+ (i + 1) + '</a>';
									}
								}
								link += '<div>';
								$("#div3").append(link);
							}
						}
						var sjc = parseInt(d2
								.substring(d2.lastIndexOf(':') + 1))
								- parseInt(d1
										.substring(d1.lastIndexOf(':') + 1));
						$("#div2").html("搜索到:" + ln + " 条记录,用时:" + sjc + " 毫秒");

						getSearchHis();

					});
	var d2 = showTime();

}
function getSearchHis() {
	$.getJSON("servlet/SearchKey?action=Searchword&t=" + new Date().getTime(),
			{}, function(json) {
				var s = "";
				for ( var i = 0; i < json.length; i++) {
					s += "<span>" + json[i].KeyWord + "</span>";
				}
				$("#div_his").html(s);
				//处理搜索历史关键字

		});
}
</script>
		<link rel="stylesheet" href="PublicStyle.css" type="text/css"></link>
		<script type="text/javascript" src="js/jquery.js">
</script>
		<script type="text/javascript" src="js/jquery.highlight.js">
</script>
		<link rel="stylesheet" href="css.css" type="text/css"></link>
	</head>

	<body style="padding: 20px; background-image:url(images/backgroundImage1.jpg);weight:10000px;">
<style> 
  .ss{
    font-weight: bold;
    color: #def;
    text-shadow: 0 0 1px currentColor,-1px -1px 1px #000,0 -1px 1px #000,1px -1px 1px #000,1px 0 1px #000,1px 1px 1px #000,0 1px 1px #000,-1px 1px 1px #000,-1px 0 1px #000;
  }
</style>
<h1 class="ss" style="margin-left:100px; margin-top:100px;width:1200px;text-align: center;font-size:50px;font-family:STXingkai;">搜索一下
<img style="height:5%; weight:5%;" src = "images/label.jpg"/>
</h1>
	<div class="container">
    <div class="bgDiv">
      <input type="text" class="search-input-text" id="checkText" name="checkText" value="" autofocus placeholder="关键词">
      <input type="button" value="搜索" class="search-input-button" id="btn" onClick="search100(0)">
      <div class="suggest">
        <ul id="search-result">
        </ul>
      </div>
    </div>
  </div>
		 
		<fieldset
			style="margin-left: 1100px;margin-top:1px; border: 3px solid #CC0; width: 100px; padding: 4px;">
			<legend>
				搜索历史
			</legend>
			<div id="div_his" class="history"></div>
		</fieldset>
			<fieldset
			style=" margin-left: 1100px;margin-top:1px; border: 3px solid #FFC0CB; width: 100px; padding: 4px;">
			<legend>
				搜索性能
			</legend>
			<div id="div2" class="history"></div>
		</fieldset>
		<div id="div3"
			style="position:absolute; left:450px; top:350px; color: #666; font-size: 14px;"></div>


	</body>
</html>
