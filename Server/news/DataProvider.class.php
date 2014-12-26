<?set_time_limit(0);?>

<?php

header("Content-Type: text/html; charset=UTF-8");

$RootDir = $_SERVER['DOCUMENT_ROOT'];

require_once($RootDir.'/news/database.class.php');
require_once($RootDir.'/news/Push.class.php');

class DataProvider
{
    // 数据库操作对象。
    private $database;
	
	// 中山学院官网首页地址。
	public $zsc = "http://www.zsc.edu.cn/index.php";
	
	// 中山学院官网地址前缀。
	public $prefix = "http://www.zsc.edu.cn/";
    
	/*
	*
	* 默认构造函数。
	*
	*/
    public function __construct()
	{}

	/*
	*
	* 默认析构函数。
	*
	*/
	public function __destruct()
	{}
    
	/*
	*
	* 初始化数据库连接，并建立数据库。
	*
	*/
    public function database_init()
    {
		/*
		* 数据库实例名称：localhost
		* 数据库实例用户名：root
		* 数据库实例用户口令：root
		*/
        $this->database = new DataBase("localhost", "root", "root");
        $this->database->createDataBase("db_mynews");
    }
	
	/*
	*
	* 去除多余的空白字符。
	* [$str]: 待处理的字符串。
	* return: 处理好的字符串。
	*
	*/
    public function trimall($str)
    {
		// 多余的空白字符包括：半/全角空格，制表符，换行。
        $before = array(" ", "　", "\t", "\n", "\r");
        $after = array("", "" ,"" ,"" ,"");
		
		$str = str_replace($before, $after, $str);
        
        return $str;
    }
	
	/*
	*
	* 去除HTML源码中的注释。
	* [$contents]: 待处理的HTML源码。
	* return: 处理好的HTML源码。
	*
	*/
    public function remove_comments_in_HTML($contents)
    {
		// 使用preg_replace()函数做正则替换操作。
        $contents = preg_replace('#<!--[\s\S]*?-->#', "", $contents);
		
		return $contents;
    }
	
	/*
	*
	* 正则提取单条数据。
	* [$regex]: 正则表达式。
	* [$contents]: 待处理的HTML源码。
	* [$contents]: 提取第几组数据。
	* return: 从HTML源码中提取出来的数据。
	*
	*/
    public function extract_single_unit_by_regex($regex, $contents, $index)
    {
		$matches = array();
		// 使用preg_match_all()函数做正则提取操作。
        preg_match_all($regex, $contents, $matches);
		// 提取到的单条数据，只有一条匹配结果。
        $contents = $matches[$index][0];
		
		return $contents;
    }
	
	/*
	*
	* 正则提取多条数据。
	* [$regex]: 正则表达式。
	* [$contents]: 待处理的HTML源码。
	* [$contents]: 提取第几组数据。
	* return: 从HTML源码中提取出来的数据。
	*
	*/
    public function extract_multi_unit_by_regex($regex, $contents, $index)
    {
		$matches = array();
		// 使用preg_match_all()函数做正则提取操作。
        preg_match_all($regex, $contents, $matches);
		// 提取到的单条数据，只有一条匹配结果。
        $contents = $matches[$index];
		
		return $contents;
    }
	
	/*
	*
	* 将字符串转换为UTF-8编码。
	* [$str]: 待处理的字符串。
	* return: 转换好的字符串。
	*
	*/
	public function encodingConveterToUTF8($str)
	{
		// GBK转换为UTF-8。
		return mb_convert_encoding($str, 'UTF-8', 'GBK');
	}
	
	/*
	*
	* 将字符串转换为GBK编码。
	* [$str]: 待处理的字符串。
	* return: 转换好的字符串。
	*
	*/
	public function encodingConveterToGBK($str)
	{
		// UTF-8转换为GBK。
		return mb_convert_encoding($str, 'GBK', 'UTF-8');
	}
    
	/*
	*
	* 抓取网页源码。
	* [$url]: 需要抓取的页面地址。
	* return: 抓取到的网页完整源码。
	*
	*/
    public function catch_contents_by_URL($url)
    {
		// 使用file_get_contents()函数抓取网页源码。
        $contents = file_get_contents($url);
        //$contents = iconv("gb2312", "utf-8",$contents);
        //echo $contents;

        return $contents;
    }
	
	/*
	*
	* 获取新闻分类信息。
	*
	*/
    public function catch_news_category()
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $category_table_name = "category";
        
		/*
		* 字段解释：
		* category_flag: 分类索引号
		* category_href: 分类页面链接
		* category_description: 分类名称
		* category_type: 分类类型
		* category_items: 分类条目总数
		* category_pages: 分类页面总数
		*/
        $category_column_name = "category_flag int NOT NULL,
                        category_href varchar(255),
                        category_description varchar(255),
                        category_type varchar(64) NOT NULL,
                        category_items int NOT NULL,
                        category_pages int NOT NULL";
        
		// 建立分类信息表。
		$this->database->createTable($database_name, $category_table_name, $category_column_name);

	//------------------------------------------------------------------------------------------------------------------------
		
		// 抓取中山学院官网首页源码。
        $url = $this->zsc;
        $contents = $this->catch_contents_by_URL($url);

		// 储存“学校要闻”段落。
		$hotnews_contents = array();

		// 从$contents提取“学校要闻”段落到$hotnews_contents。
        $regex = '#(?<=<div id="hotnews")[\s\S]*?(?=</div>)#';
		$hotnews_contents = $this->extract_single_unit_by_regex($regex, $contents, 0);

		// 去除“学校要闻”段落中的HTML注释。
		$hotnews_contents = $this->remove_comments_in_HTML($hotnews_contents);

		// 从$hotnews_contents提取“学校要闻”页面链接到$hotnews_page_link。
        $regex = '#<a[\s\S]*?href="(list.*?)"#';
		$hotnews_page_link = $this->extract_single_unit_by_regex($regex, $hotnews_contents, 1);

		//$hotnews_page_link = str_ireplace('http://www.zsc.edu.cn', "", $hotnews_page_link);

        echo $hotnews_page_link."</br>", "</br>", "\n";

    //------------------------------------------------------------------------------

		// 抓取“学校要闻”页面源码。
        $url = $this->prefix.$hotnews_page_link;
        $contents = $this->catch_contents_by_URL($url);

		// 储存分类标签页段落。
        $menu_contents = array();

		// 从$contents提取标签页段落到$menu_contents。 
        $regex = '#(?<=<div id="menu")[\s\S]*?(?=</div>)#';
		$menu_contents = $this->extract_single_unit_by_regex($regex, $contents, 0);
		
		// 去除标签页段落中的HTML注释。
		$menu_contents = $this->remove_comments_in_HTML($menu_contents);

		// 从$menu_contents提取分类标签页段落到$menu_contents。
        $regex = '#(?<=<ul id="downmenu")[\s\S]*?(?=</ul>)#';
		$menu_contents = $this->extract_single_unit_by_regex($regex, $menu_contents, 0);

		// 从$menu_contents提取各分类标签页的链接到$category_link。
        $regex = '#<a[\s\S]*?href="(/list.*?)"#';		
		$category_link = $this->extract_multi_unit_by_regex($regex, $menu_contents, 1);

		// 从$menu_contents提取各分类标签页的名称到$category_link。
        $regex = '#(?<=<span>)[\s\S]*?(?=</span>)#';
        $category_name = $this->extract_multi_unit_by_regex($regex, $menu_contents, 0);

		// 删除数据库中category表的全部数据。
        $this->database->deleteEntry($database_name, $category_table_name, "category_type = 'news_category'");

		// 将提取到的数据写入数据库。
        $id = 1;
        foreach($category_link as $key)
        {
			// 去除链接中的前缀。
			$key = str_ireplace('http://www.zsc.edu.cn/', "", $key);
			// 去除链接中的前缀/号。
			$key = preg_replace('#^/#', "", $key);
			// 转换编码为UTF8。
			$name = $this->encodingConveterToUTF8($category_name[$id - 1]);
				
			// 将每一条数据插入数据库。
            $this->database->insertEntry($database_name, $category_table_name, 
                                    "category_flag, category_href, category_description, category_type, category_items, category_pages",
                                    $id.', "'.$key.'", "'.$name.'", "news_category", 0, 0');
			
			echo $key."</br>".$name."</br>";
			$id++;
        }

        echo "</br>", "\n";
    }
    
	/*
	*
	* 获取各个新闻分类的数量信息。
	* [$category_href]: 各个新闻分类的页面地址。
	* [$id]: 各个新闻分类的索引号。
	* return: 新闻分类的条目总数。
	*
	*/
    public function catch_news_category_capacity_by_id($category_href, $catid)
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $category_table_name = "category";

		// 抓取某个新闻分类的页面源码。
		$url = $this->prefix.$category_href;
		$contents = $this->catch_contents_by_URL($url);

		$matches = array();

		// 从$contents提取分类新闻数量信息段落到$contents。
		$regex = '#(?<=<p id="pages")[\s\S]*?(?=</p>)#';
		$contents = $this->extract_single_unit_by_regex($regex, $contents, 0);
	
		// 去除分类新闻数量信息段落中的HTML注释。
		$contents = $this->remove_comments_in_HTML($contents);

		// 从$contents提取分类新闻条目总数段落到$category_items。
		$regex = $this->encodingConveterToGBK('#(?<=总数：<b>)[0-9]*?(?=</b>)#');
		//$regex = '#(?<=总数：<b>)[0-9]*?(?=</b>)#';
		$category_items = $this->extract_single_unit_by_regex($regex, $contents, 0);

		// 从$contents提取分类新闻页面总数段落到$category_pages。
		$regex = $this->encodingConveterToGBK('#(?<=页次：<b><font color="red">1</font>/)[0-9]*?(?=</b>)#');
		//$regex = '#(?<=页次：<b><font color="red">1</font>/)[0-9]*?(?=</b>)#';
		$category_pages = $this->extract_single_unit_by_regex($regex, $contents, 0);
        
        echo $category_items."  ".$category_pages."</br>";
        
		// 更新数据库中新闻分类的条目总数。
        $this->database->updateEntry($database_name, $category_table_name, "category_items", '"'.(int)$category_items.'"', "category_flag = '".$catid."'");
		// 更新数据库中新闻分类的页面总数。
        $this->database->updateEntry($database_name, $category_table_name, "category_pages", '"'.(int)$category_pages.'"', "category_flag = '".$catid."'");
        
		// 返回新闻分类的条目总数。
        return $category_items;

    }
    
	/*
	*
	* 获取所有新闻分类的数量信息。
	*
	*/
    public function catch_all_news_category_capacity()
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $category_table_name = "category";
        
		// 从数据库中读取新闻分类信息。
        $category_column_name = "*";
        $category = $this->database->selectEntryWhereOrder($database_name, $category_table_name, $category_column_name, "category_type = 'news_category'", "category_flag");
        
		// 遍历数据库读取结果。
        while($row = mysql_fetch_array($category))
        {
            $catid = $row['category_flag'];
            
			// 跳过三个无用的分类。
            if($catid == 10 || $catid == 11 || $catid == 12)
            {
                continue;
            }
            
			// 获取新闻分类的数量信息。
            $this->catch_news_category_capacity_by_id($row['category_href'], $catid);
        }
        
    }
    
	/*
	*
	* 按分类获取新闻列表。
	* [$catid]: 新闻分类索引号。
	* [$pages]: 列表页面总数。
	* [$href]: 新闻分类页面地址。
	*
	*/
    public function catch_news_list_by_id($catid, $pages, $href)
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $list_table_name = "list_".$catid;
        
		/*
		* 字段解释：
		* item_flag: 新闻列表条目索引号
		* item_href: 新闻列表条目链接
		* item_description: 新闻列表条目名称
		* item_date: 新闻列表条目日期
		* item_page: 新闻列表条目所在页
		* item_category: 新闻列表所属分类
		*/
        $list_column_name = "item_flag int NOT NULL,
                    PRIMARY KEY(item_flag),
                    item_href varchar(255),
                    item_description varchar(255),
                    item_date date,
                    item_page int NOT NULL,
					item_category int NOT NULL";
		
		// 建立新闻列表信息表。
        $this->database->createTable($database_name, $list_table_name, $list_column_name);

	//------------------------------------------------------------------------------
        
		// 列表页面总数。
        $count = $pages;
    
		// 删除数据库中list_(catid)表的全部数据。
        $this->database->deleteAllEntry($database_name, $list_table_name);
 
		// 逐页抓取新闻列表内容。
        for($i = 1; $i <= $count; $i++)
        {
			// 抓取某个新闻列表页的页面源码。
            $url = $this->prefix.$href."&page=".$i;
            $contents = $this->catch_contents_by_URL($url);
        
            $matches = array();
        
			// 从$contents提取新闻列表段落到$contents。
            $regex = '#(?<=<ul class="text_list text_list_f14">)[\s\S]*?(?=</ul>)#';
			$contents = $this->extract_single_unit_by_regex($regex, $contents, 0);

			// 去除新闻列表段落中的HTML注释。
			$contents = $this->remove_comments_in_HTML($contents);
			
            // 从$contents提取新闻列表条目到$matches。
            $regex = '#(?<=<li>)[\s\S]*?(?=</li>)#';
			$matches = $this->extract_multi_unit_by_regex($regex, $contents, 0);
    
            // 遍历$matches，提取新闻列表条目信息并写入数据库。
			$regex = '#<a[\s\S]*?>([\s\S]*?)(?=</a>)#';
            $regex2 = '#(?<=<span class="date">)[\s\S]*?(?=</span>)#';
            $regex3 = '#(?<=<a href=")[\s\S]*?(?=")#';
            $regex4 = '#contentid=([0-9]*?)(?=")#';
            foreach($matches as $key)
            {	
				// 从$key中提取新闻列表条目的标题、日期、页面链接、索引号。
				$item_title = $this->extract_single_unit_by_regex($regex, $key, 1);
				$item_date = $this->extract_single_unit_by_regex($regex2, $key, 0);
				$item_href = $this->extract_single_unit_by_regex($regex3, $key, 0);
				$item_flag = $this->extract_single_unit_by_regex($regex4, $key, 1);
                
                // 对数据库进行操作之前，要先对字符串进行转义。
                if(!get_magic_quotes_gpc())
                {
					// 使用addslashes()函数对字符串进行转义，使其合适SQL语句。
                    $item_title = addslashes($item_title);
                }
				
				// 转换编码为UTF8。
				$item_title = $this->encodingConveterToUTF8($item_title);
                
				// 将每一条数据插入数据库。
                $this->database->insertEntry($database_name, $list_table_name,
                                            "item_flag, item_href, item_description, item_date, item_page, item_category",
                                            $item_flag.', "'.$item_href.'", "'.$item_title.'", "'.$item_date.'", '.$i.', '.$catid);
                
                //echo $item_flag[1][0]."</br>";
                //echo $item_title[1][0]."</br>";
                //echo $item_date[0][0]."</br>";
                //echo $item_href[0][0]."</br>";
                //echo $key."</br>";
                //echo "ok"."</br>";
            }
			// 延时1秒。
            sleep(1);
        }
    }
    
	/*
	*
	* 获取某个分类的新闻列表。
	* [$catid]: 新闻分类索引号。
	*
	*/
    public function catch_a_news_list($catid)
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $category_table_name = "category";
        
		// 从数据库中读取新闻分类信息。
        $category_column_name = "*";
        $category = $this->database->selectEntryWhere($database_name, $category_table_name, $category_column_name, "category_flag = '".$catid."'");
        
		// 遍历数据库读取结果。
        while($row = mysql_fetch_array($category))
        {
			// 获取该分类的新闻列表。
            $this->catch_news_list_by_id($row['category_flag'], $row['category_pages'], $row['category_href']);
        }
        
        echo "catch news list ".$catid." finish!"."</br>";
    }
    
	/*
	*
	* 获取所有分类的新闻列表。
	*
	*/
    public function catch_all_news_list()
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $category_table_name = "category";
        
		// 从数据库中读取新闻分类信息。
        $category_column_name = "*";
        $category = $this->database->selectEntryWhereOrder($database_name, $category_table_name, $category_column_name, "category_type = 'news_category'", "category_flag");
        
		// 遍历数据库读取结果。
        while($row = mysql_fetch_array($category))
        {
			// 新闻分类索引号。
            $catid = $row['category_flag'];
        
			// 跳过三个无用的分类。
            if($catid == 10 || $catid == 11 || $catid == 12)
            {
                continue;
            }
            
			// 获取该分类的新闻列表。
            $this->catch_news_list_by_id($catid, $row['category_pages'], $row['category_href']);
        }
        
        echo "catch all news list finish!"."</br>";
    }

	/*
	*
	* 按分类和链接获取新闻正文。
	* [$catid]: 新闻分类索引号。
	* [$data]: 新闻正文相关数据。
	*
	*/
    public function catch_news_body_by_link($catid, $data)
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $content_table_name = "content_".$catid;
    
		// 抓取某个新闻正文页的页面源码。
        $url = $this->prefix.$data['item_href'];
        $contents = $this->catch_contents_by_URL($url);
        
        $matches = array();
        $title_contents = array();
        $text_contents = array();
    
		// 从$contents提取新闻标题段落到$title_contents。
        $regex = '#(?<=<div id="content">)[\s\S]*?</div>#';
		$title_contents = $this->extract_single_unit_by_regex($regex, $contents, 0);
    
		// 去除新闻标题段落HTML源码中的注释。
		$title_contents = $this->remove_comments_in_HTML($title_contents);
    
		// 从$contents提取新闻正文段落到$text_contents。
        $regex = '#<div id="endtext">[\s\S]*?(?=<div id="pages">)#';
		$text_contents = $this->extract_single_unit_by_regex($regex, $contents, 0);
    
		// 去除新闻正文段落HTML源码中的注释。
		$text_contents = $this->remove_comments_in_HTML($text_contents);
    
        //echo $title_contents."</br>".$text_contents;
        
		// 去除多余的内容。
        $regex = $this->encodingConveterToGBK('#浏览次数：[\s\S]*?</span>#');
		//$regex = '#浏览次数：[\s\S]*?</span>#';
        $title_contents = preg_replace($regex, "", $title_contents);
        
		// 去除多余的内容。
        $regex = '#<span>|</span>#';
        $title_contents = preg_replace($regex, "", $title_contents);
		
		// 转换编码为UTF8。
		$title_contents = $this->encodingConveterToUTF8($title_contents);
		$text_contents = $this->encodingConveterToUTF8($text_contents);
		
		// 从$title_contents提取新闻主标题到$item_title。
        $regex = '#<h1>([\s\S]*?)</h1>#';
		$item_title = $this->extract_single_unit_by_regex($regex, $title_contents, 1);
        
		// 从$title_contents提取新闻副标题到$item_title。
        $regex = '#<h2>([\s\S]*?)</h2>#';
		$item_subtitle = $this->extract_single_unit_by_regex($regex, $title_contents, 1);
        
        //echo $item_title."</br>".$item_subtitle;
        
		// 去除新闻正文段落HTML源码中的注释。
		$text_contents = $this->remove_comments_in_HTML($text_contents);
        
		// 为新闻正文图片添加“width=100%”，适应移动设备屏幕缩放。
        $regex = '#<img#';
        $text_contents = preg_replace($regex, "<img width=100%", $text_contents);
        
		// 为新闻正文图片添加“text-align: left; text-indent: 0em; padding-top: 1em; padding-bottom: 1em”，适应移动设备屏幕排版。
        $regex = '#style="text-align:[ ]*?center[\s\S]*?"#';
        $text_contents = preg_replace($regex, "style=\"text-align: left; text-indent: 0em; padding-top: 1em; padding-bottom: 1em\"", $text_contents);
        
		// 去除HTML源码中的标签，将新闻正文转化为纯文字新闻摘要。
        $regex = '#<div[\s\S]*?>|</div>|<img[\s\S]*?/>|<br />|&.*?;#';
        $item_summary = preg_replace($regex, "", $text_contents);
		
		// 新闻最长140个字符，使用mb_substr()函数截取前140个字符，使用strip_tags()函数去掉多余的HTML标签，使用trimall()去掉多余的空白字符。
        $len = 140;
        $item_summary = mb_substr($item_summary, 0, $len, 'utf-8');
        $item_summary = strip_tags($item_summary);
        $item_summary = $this->trimall($item_summary);
        
		// 为新闻正文添加“width=100% style=\"text-indent: 2em”，适应移动设备屏幕排版；加上base信息，使<img>标签内的图片可以正确加载。
        $text_contents = "<div width=100% style=\"text-indent: 2em\">"."\n".$text_contents."\n"."</div>";
        $text_contents = "<base href=\"http://www.zsc.edu.cn/\" />"."\n".$text_contents;

        // 对数据库进行操作之前，要先对字符串进行转义。
        if(!get_magic_quotes_gpc())
        {
			// 使用addslashes()函数对字符串进行转义，使其合适SQL语句。
            $item_title = addslashes($item_title);
            $item_subtitle = addslashes($item_subtitle);
			$item_summary = addslashes($item_summary);
            $text_contents = addslashes($text_contents);
            //$text_contents = mysqli_real_escape_string($text_contents);
        }
    
		// 删除数据库中已存在的同一条新闻正文条目。
        $this->database->deleteEntry($database_name, $content_table_name, "itemID = '".$data['item_flag']."'");
    
		// 将新闻正文条目插入数据库。
        $this->database->insertEntry($database_name, $content_table_name, "itemID, item_date, item_summary, item_title, item_subtitle, item_text, item_category",
                                    '"'.$data['item_flag'].'"'.', "'.$data['item_date'].'", "'
                                    .$item_summary.'", "'.$item_title.'", "'.$item_subtitle.'", "'.$text_contents.'", "'.$catid.'"');
    
		/*
		// 从数据库中读取新闻正文条目。
        $content_column_name = "*";
        $data = $this->database->selectEntryWhere($database_name, $table_name, $content_column_name, "itemID = '".$data['item_flag']."'");
    
        $res = mysql_num_rows($data);
        
        while($row = mysql_fetch_array($data))
        {
            $pages = $row['item_text'];
            echo $pages;
        }
        
		// 返回新闻正文条目。
        return $res;*/
    }
    
	/*
	*
	* 获取某一篇新闻正文。
	* [$catid]: 新闻分类索引号。
	* [$contentid]: 新闻正文相关数据。
	*
	*/
    public function catch_a_news_body($catid, $contentid)
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $list_table_name = "list_".$catid;
        $content_table_name = "content_".$catid;
        
		/*
		* 字段解释：
		* itemID: 新闻正文条目索引号
		* item_date: 新闻正文条目日期
		* item_summary: 新闻正文条目摘要
		* item_title: 新闻正文条目标题
		* item_subtitle: 新闻正文条目副标题
		* item_text: 新闻正文条目内容
		* item_category: 新闻正文条目所属分类
		*/
        $content_column_name = "itemID int NOT NULL, 
							PRIMARY KEY(itemID),
							item_date date,
							item_summary text,
							item_title text,
							item_subtitle text,
							item_text text,
							item_category int NOT NULL";
							
		// 建立新闻正文表。
        $this->database->createTable($database_name, $content_table_name, $content_column_name);
        
	//------------------------------------------------------------------------------
        
		// 从数据库中读取新闻列表信息。
        $list_column_name = "*";
        $data = $this->database->selectEntryWhere($database_name, $list_table_name, $list_column_name, "item_flag = '".$contentid."'");
        
		// 遍历数据库读取结果。
        while($row = mysql_fetch_array($data))
        {
			// 按结果获取新闻正文。
            $this->catch_news_body_by_link($catid, $row);
        }
        
    }
    
	/*
	*
	* 获取所有新闻正文。（空置）
	*
	*/
    public function catch_all_news_body()
    {
        $database_name = "db_mynews";
        $table_name = "list_1";
        $new_table_name = "content_1";

        $column_name = "itemID int NOT NULL,
                        PRIMARY KEY(itemID),
                        item_date date,
                        item_summary text,
                        item_title text,
                        item_subtitle text,
                        item_text text,
                        item_category int NOT NULL";
        $this->database->createTable($database_name, $new_table_name, $column_name);
        
        //------------------------------------------------------------------------------
        
        $column_name = "*";
        $data = $this->database->selectEntryWhere($database_name, $table_name, $column_name, "item_flag = '16762'");
        
        while($row = mysql_fetch_array($data))
        {
            $this->catch_news_body_by_link(1, $row);
        }
    }
    
	/*
	*
	* 获取一周热点列表。
	*
	*/
    public function catch_hotlist()
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $list_table_name = "list_14";
        $content_table_name = "content_14";
        
		/*
		* 字段解释：
		* item_flag: 新闻列表条目索引号
		* item_date: 新闻列表条目日期
		* item_href: 新闻列表条目链接
		* item_title: 新闻列表条目标题
		*/
        $list_column_name = "item_flag int NOT NULL,
                        item_date date,
                        item_href varchar(255),
                        item_title varchar(255)";
						
		// 建立一周热点表。
        $this->database->createTable($database_name, $list_table_name, $list_column_name);
    
    //------------------------------------------------------------------------------
		
        // 抓取中山学院官网首页源码。
        $url = $this->zsc;
        $contents = $this->catch_contents_by_URL($url);

		// 储存“学校要闻”段落。
		$hotnews_contents = array();

		// 从$contents提取“学校要闻”段落到$hotnews_contents。
        $regex = '#(?<=<div id="hotnews")[\s\S]*?(?=</div>)#';
		$hotnews_contents = $this->extract_single_unit_by_regex($regex, $contents, 0);

		// 去除“学校要闻”段落中的HTML注释。
		$hotnews_contents = $this->remove_comments_in_HTML($hotnews_contents);

		// 从$hotnews_contents提取“学校要闻”页面链接到$hotnews_page_link。
        $regex = '#<a[\s\S]*?href="(list.*?)"#';
		$hotnews_page_link = $this->extract_single_unit_by_regex($regex, $hotnews_contents, 1);

		//$hotnews_page_link = str_ireplace('http://www.zsc.edu.cn', "", $hotnews_page_link);

        //echo $hotnews_page_link."</br>", "</br>", "\n";

    //------------------------------------------------------------------------------

		// 抓取“学校要闻”页面源码。
        $url = $this->prefix.$hotnews_page_link;
        $contents = $this->catch_contents_by_URL($url);

        $matches = array();

		// 从$contents提取“一周热点”段落到$list_contents。
        $regex = '#(?<=<ul id="sidehotlist")[\s\S]*?(?=</ul>)#';
		$list_contents = $this->extract_single_unit_by_regex($regex, $contents, 0);

		// 去除HTML源码中的注释。
		$list_contents = $this->remove_comments_in_HTML($list_contents);

		// 从$list_contents提取“一周热点”列表条目到$matches。
        $regex = '#<li>([\s\S]*?)</li>#';
		$list_contents = $this->extract_multi_unit_by_regex($regex, $list_contents, 1);
        
		// 删除数据库中“一周热点”的数据。
        $this->database->deleteAllEntry($database_name, $list_table_name);
        $this->database->deleteAllEntry($database_name, $content_table_name);
        
		// 遍历$list_contents，提取“一周热点”列表条目信息并写入数据库。
        $regex = '#contentid=([0-9]*?)"#';
        $regex2 = '#<a[\s\S]*?href="([\s\S]*?)"#';
        $regex3 = '#<a[\s\S]*?>([\s\S]*?)</a>#';
        foreach($list_contents as $key)
        {
			// 从$key中提取“一周热点”列表条目的索引号、链接、标题。
			$id = $this->extract_single_unit_by_regex($regex, $key, 1);
			$href = $this->extract_single_unit_by_regex($regex2, $key, 1);
			$title = $this->extract_single_unit_by_regex($regex3, $key, 1);
			
			// 转换编码为UTF8。
			$title = $this->encodingConveterToUTF8($title);
            
			// 将每一条数据插入数据库。
            $this->database->insertEntry($database_name, $list_table_name, 
                                    "item_flag, item_date, item_href, item_title",
                                    $id.', "0000-00-00"'.', "'.$href.'", "'.$title.'"');
									
			//echo $id."</br>", $href."</br>", $title."</br>";
        }
        
    }
    
	/*
	*
	* 检查某一分类新闻条目数量。
	* [$category_href]: 新闻分类页面地址。
	* [$catid]: 新闻分类索引号。
	* [$category_items]: 新闻分类条目总数。
	* return: 新闻分类新增的条目数。
	*
	*/
    public function check_news_category_capacity_by_id($category_href, $catid, $category_items)
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $list_table_name = "list_".$catid;
		
		$count = $category_items;
        
		// 获取该分类最新的新闻条目总数。
        $items = $this->catch_news_category_capacity_by_id($category_href, $catid);
        
        echo "news_add:".$items." ".$count."</br>";
        
		// 返回新闻分类新增的条目数。
        return ($items - $count);
    }
    
	/*
	*
	* 检查所有分类新闻条目数量。
	*
	*/
    public function check_all_news_category_capacity()
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $category_table_name = "category";
        
		// 从数据库中读取新闻分类信息。
        $category_column_name = "*";
        $category = $this->database->selectEntryWhereOrder($database_name, $category_table_name, $category_column_name, "category_type = 'news_category'", "category_flag");
        
		// 所有新闻分类新增的条目数。
        $items_new = 0;
        
		// 遍历数据库读取结果。
        while($row = mysql_fetch_array($category))
        {
			// 新闻分类索引号。
            $catid = $row['category_flag'];
            
			// 跳过三个无用的分类。
            if($catid == 10 || $catid == 11 || $catid == 12)
            {
                continue;
            }
            
			// 检查分类新闻条目数量。
            $result = $this->check_news_category_capacity_by_id($row['category_href'], $catid, $row['category_items']);
			
			// 如果结果大于0，就表示该分类有新增内容，反之没有。
            if($result > 0)
            {
                echo "true"."</br>";
				// 将新增内容的数量累计起来。
                $items_new = $items_new + $result;
				// 更新相应的分类新闻列表。
                //$this->update_a_news_list($id);
            }
            else
            {
                echo "false"."</br>";
            }
        }
        
		// 推送更新消息到设备。
        $this->prepare_news_item_to_push($items_new);
        echo "update finish!"."</br>";
    }
    
	/*
	*
	* 更新指定分类的新闻列表。
	* [$catid]: 新闻分类索引号。
	*
	*/
    public function update_a_news_list($catid)
    {
		// 获取并更改该分类的新闻列表信息。
        $this->catch_a_news_list($catid);
    }
    
	/*
	*
	* 准备更新消息的推送。
	* [$items]: 新闻分类索引号。
	*
	*/
    public function prepare_news_item_to_push($items)
    {
		// 准备推送字段，标题和描述。
        $title = "发现 ".$items." 篇新闻更新";
        $description = "点击打开中山学院新闻客户端";
    
		// 推送消息到设备。
        $this->push_news_item($title, $description);
    }
    
	/*
	*
	* 推送消息到设备。
	* [$title]: 推送消息的标题。
	* [$description]: 推送消息的描述。
	*
	*/
    public function push_news_item($title, $description)
    {
		// 推送消息。
        $message_push = new Push();
        $message_push->push_message_to_android($title, $description);
    }
    
	/*
	*
	* 提供新闻分类信息。
	* return: 新闻分类信息。
	*
	*/
    public function get_category()
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $category_table_name = "category";
        
		// 从数据库中读取新闻分类信息。
        $category_column_name = "*";
        $data = $this->database->selectEntryOrder($database_name, $category_table_name, $category_column_name, "category_flag");
        
        // 储存读取到的结果。
		$result = array();
        
		// 遍历数据库读取结果。
        while($row = mysql_fetch_array($data))
        {
			// 将结果转换为数组。
            $result[$row['category_flag']] = $row;
        }
        
		// 返回新闻分类信息。
        return $result;
    }
    
	/*
	*
	* 提供指定的新闻分类信息。
	* [$catid]: 新闻分类索引号。
	* return: 指定的新闻分类信息。
	*
	*/
    public function get_category_by_id($catid)
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $category_table_name = "category";
        
		// 从数据库中读取新闻分类信息。
        $category_column_name = "*";
        $data = $this->database->selectEntryWhere($database_name, $category_table_name, $category_column_name, "category_flag = '".$catid."'");
        
		// 储存读取到的结果。
        $result = array();
        
		// 遍历数据库读取结果。
        while($row = mysql_fetch_array($data))
        {
			// 将结果转换为数组。
            $result[$row['category_flag']] = $row;
        }
        
		// 返回指定的新闻分类信息。
        return $result;
    }
    
	/*
	*
	* 提供指定分类的新闻列表。
	* [$catid]: 新闻分类索引号。
	* [$pages]: 新闻列表的页码。
	* return: 指定分类的新闻列表。
	*
	*/
    public function get_page_list_by_id($catid, $pages)
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $category_table_name = "list_".$catid;
        
		// 从数据库中读取新闻列表信息。
        $category_column_name = "*";
        $data = $this->database->selectEntryWhere($database_name, $category_table_name, $category_column_name, "item_page = '".$pages."'");
        
		// 储存读取到的结果。
        $result = array();
        
		// 遍历数据库读取结果。
        while($row = mysql_fetch_array($data))
        {
			// 将结果转换为数组。
            $result[$row['item_flag']] = $row;
        }
        
		// 返回指定分类的新闻列表。
        return $result;
    }
    
	/*
	*
	* 提供指定分类的新闻条目。
	* [$catid]: 新闻分类索引号。
	* [$contentid]: 新闻条目索引号。
	* return: 指定分类的新闻条目。
	*
	*/
    public function get_content_by_id($catid, $contentid)
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $content_table_name = "content_".$catid;
        
		// 从数据库中读取新闻条目。
        $content_column_name = "*";
        $data = $this->database->selectEntryWhere($database_name, $content_table_name, $content_column_name, "itemID = '".$contentid."'");
        
		// 储存读取到的结果。
        $result = array();
        
        //var_dump(mysql_num_rows($data));
        
		// 如果读取失败或结果为空，则重新获取。
        if($data == false || mysql_num_rows($data) == 0)
        {
			// 获取新闻条目正文。
            $this->catch_a_news_body($catid, $contentid);
			// 从数据库中读取新闻条目。
            $column_name = "*";
            $data = $this->database->selectEntryWhere($database_name, $content_table_name, $content_column_name, "itemID = '".$contentid."'");
        }
        
		// 遍历数据库读取结果。
        while($row = mysql_fetch_array($data))
        {
			// 将结果转换为数组。
            $result[$row['itemID']] = $row;
        }
        
		// 返回指定分类的新闻条目。
        return $result;
    }
    
	/*
	*
	* 提供“一周热点”列表。
	* return: “一周热点”列表。
	*
	*/
    public function get_hotlist()
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $list_table_name = "list_14";
        
		// 从数据库中读取“一周热点”列表条目。
        $list_column_name = "*";
        $data = $this->database->selectEntry($database_name, $list_table_name, $list_column_name);
        
        $result = array();
		
		// 如果读取失败或结果为空，则重新获取。
        if($data == false || mysql_num_rows($data) == 0)
        {
			// 获取新闻条目正文。
            $this->catch_hotlist();
            // 从数据库中读取“一周热点”列表条目。
			$list_column_name = "*";
			$data = $this->database->selectEntry($database_name, $list_table_name, $list_column_name);
        }
        
		// 遍历数据库读取结果。
        while($row = mysql_fetch_array($data))
        {
			// 将结果转换为数组。
            $result[$row['item_flag']] = $row;
        }
        
		// 返回“一周热点”列表。
        return $result;
    }
	
	/*
	*
	* 提供搜索结果。
	* [$userID]: 用户标识号。
	* [$pages]: 搜索结果页码。
	* return: 搜索结果。
	*
	*/
    public function get_search_result($userID, $pages)
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $result_view_name = "search_result_".$userID;
        
		// 从数据库中读取搜索结果列表条目。
        $list_column_name = "*";
		$pages = ($pages - 1) * 10;
		$limit = "LIMIT ".$pages.", 10";
        $data = $this->database->selectEntryMore($database_name, $result_view_name, $list_column_name, $limit);
        
        $result = array();
		
		// 如果读取失败或结果为空，则重新获取。
        if($data == false || mysql_num_rows($data) == 0)
        {
			return 0;
        }
        
		// 遍历数据库读取结果。
        while($row = mysql_fetch_array($data))
        {
			// 将结果转换为数组。
            $result[$row['item_flag']] = $row;
        }
        
		// 返回搜索结果。
        return $result;
    }
    
	/*
	*
	* 搜索指定新闻分类。
	* [$userID]: 用户标识号。
	* [$catid]: 新闻分类索引号。
	* [$key]: 搜索关键字。
	* return: 搜索结果总数。
	*
	*/
    public function search_news_list_by_id($userID, $catid, $key)
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $list_table_name = "list_".$catid;
        $result_view_name = "search_result_".$userID;
        
		/*
		* 字段解释：
		* item_flag: 搜索结果条目索引号
		* item_href: 搜索结果条目日期
		* item_description: 搜索结果条目标题
		* item_date: 搜索结果条目日期
		*/
        $result_column_name = "item_flag, item_href, item_description, item_date, item_category";
		
    //------------------------------------------------------------------------------
		
		// 视图来源。
		$select = "SELECT item_flag, item_href, item_description, item_date, item_category FROM ".$list_table_name." WHERE item_description LIKE '%".$key."%'";
		// 创建搜索结果视图。
		$this->database->createView($database_name, $result_view_name, $result_column_name, $select);
		
		// 统计搜索结果总数
		$data = $this->database->selectEntry($database_name, $result_view_name, 'count(*)');
		
		while($row = mysql_fetch_array($data))
        {
			$count = $row['count(*)'];
		}
		
		// 返回搜索结果总数。
        return $count;
    }
    
	/*
	*
	* 搜索所有新闻分类。
	* [$userID]: 用户标识号。
	* [$catid]: 新闻分类索引号。
	* [$key]: 搜索关键字。
	* return: 搜索结果总数。
	*
	*/
    public function search_news_list_by_all($userID, $key)
    {
		// 数据库信息。
        $database_name = "db_mynews";
        $result_view_name = "search_result_".$userID;
        
		/*
		* 字段解释：
		* item_flag: 搜索结果条目索引号
		* item_href: 搜索结果条目日期
		* item_description: 搜索结果条目标题
		* item_date: 搜索结果条目日期
		*/
        $result_column_name = "item_flag, item_href, item_description, item_date, item_category";
        
    //------------------------------------------------------------------------------
        
		$list_table_name = "list_1";
		$select = "SELECT item_flag, item_href, item_description, item_date, item_category FROM ".$list_table_name." WHERE item_description LIKE '%".$key."%'";
        // 视图来源。
        for($i = 2; $i <= 13; $i++)
        {
			// 跳过三个无用的分类。
            if($i == 10 || $i == 11 || $i == 12)
            {
                continue;
            }
			
			// 构建SELECT语句。
			$list_table_name = "list_".$i;
			$select = $select." UNION ALL ";
			$select = $select.("SELECT item_flag, item_href, item_description, item_date, item_category FROM ".$list_table_name." WHERE item_description LIKE '%".$key."%'");
        }
		
        // 创建搜索结果视图。
		$this->database->createView($database_name, $result_view_name, $result_column_name, $select);
		
		// 统计搜索结果总数
		$data = $this->database->selectEntry($database_name, $result_view_name, 'count(*)');
		
		while($row = mysql_fetch_array($data))
        {
			$count = $row['count(*)'];
		}
		
		// 返回搜索结果总数。
        return $count;
    }
	
    /*
	*
	* 搜索结束处理。
	* [$userID]: 用户标识号。
	*
	*/
	public function search_finish($userID)
	{
		// 数据库信息。
        $database_name = "db_mynews";
        $result_view_name = "search_result_".$userID;
		
		// 删除搜索结果视图。
		$this->database->dropView($database_name, $result_view_name);
	}
	
}

?>
