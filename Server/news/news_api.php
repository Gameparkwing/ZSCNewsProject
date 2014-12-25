<?set_time_limit(0);?>

<?php

header("Content-Type: text/html; charset=UTF-8");

$RootDir = $_SERVER['DOCUMENT_ROOT'];
$api_key = 4321;

require($RootDir.'/news/DataProvider.class.php');

	// 新建数据提供者对象实例。
    $data_provider = new DataProvider();
	// 初始化数据提供者的数据库。
    $data_provider->database_init();
    
	// 储存输出的内容。
    $output = array();
    
	// 获取HTTP的GET参数值。
	$token = @$_GET['token'] ? $_GET['token'] : 0;
    $operation = @$_GET['operation'] ? $_GET['operation'] : NULL;
    $catid = @$_GET['catid'] ? $_GET['catid'] : 0;
    $pages = @$_GET['pages'] ? $_GET['pages'] : 0;
    $contentid = @$_GET['contentid'] ? $_GET['contentid'] : 0;
    $userID = @$_GET['userID'] ? $_GET['userID'] : NULL;
    $search_key = @$_GET['search_key'] ? $_GET['search_key'] : NULL;
    
	// 验证接口。
	if($token != $api_key)
	{
		exit();
	}
	
	// 操作接口。
    if(empty($operation))
    {
        //$output = array('data'=>NULL, 'info'=>'kda!', 'code'=>-201);
        //exit(json_encode($output));
		exit();
    }
	else if($operation == 'get_list')
    {
        if(empty($catid)||empty($pages))
        {
            
        }
        else
        {
            $output = array();
            $data = $data_provider->get_page_list_by_id($catid, $pages);
            
            $count = 1;
            
            foreach($data as $key)
			{
				$output[$count] = array(
					'item_flag' => $key['item_flag'],
					'item_href' => $key['item_href'],
					'item_description' => $key['item_description'],
					'item_date' => $key['item_date']
				);
				$count++;
			}
            
        
            //exit(urldecode(json_encode($output)));
            exit(json_encode($output));
        }
    }
	else if($operation == 'get_list_summary')
    {
        if(empty($catid)||empty($pages))
        {
            
        }
        else
        {
            $output = array();
            $data = $data_provider->get_page_list_by_id($catid, $pages);
            
            $count = 1;
            foreach($data as $key)
			{
				$data2 = $data_provider->get_content_by_id($catid, $key['item_flag']);
				
				foreach($data2 as $key2)
				{
					$summary = $key2['item_summary'];
					//echo $summary."</br>";
				}
			
				$output[$count] = array(
					'item_flag' => $key['item_flag'],
					'item_href' => $key['item_href'],
					'item_description' => $key['item_description'],
					'item_summary' => $summary,
					'item_date' => $key['item_date'],
					'item_category' => $key['item_category']
				);
				$count++;
			}
            
            //exit(urldecode(json_encode($output)));
            exit(json_encode($output));
        }
    }
    else if($operation == 'get_category_items')
    {
        if(empty($catid))
        {
            
        }
        else
        {
            $output = array();
            $data = $data_provider->get_category_by_id($catid);
        
            foreach($data as $key)
            {   
                $output[$key['category_flag']] = array(
                    'category_items' => $key['category_items']
                );
            }
        
            exit(json_encode($output));
        }
    }
    else if($operation == 'get_category_pages')
    {
        if(empty($catid))
        {
            
        }
        else
        {
            $output = array();
            $data = $data_provider->get_category_by_id($catid);
        
            foreach($data as $key)
            {   
                $output[$key['category_flag']] = array(
                    'category_pages' => $key['category_pages']
                );
            }
        
            exit(json_encode($output));
        }
    }
    else if($operation == 'get_content')
    {
        if(empty($catid)||empty($contentid))
        {
            
        }
        else
        {
            $output = array();
            $data = $data_provider->get_content_by_id($catid, $contentid);
            
            foreach($data as $key)
            {   
                $key['item_text'] = htmlspecialchars($key['item_text']);
                $output[$key['itemID']] = array(
                    'itemID' => $key['itemID'],
                    'item_title' => $key['item_title'],
                    'item_subtitle' => $key['item_subtitle'],
                    'item_text' => $key['item_text'],
                    'item_category' => $key['item_category']
                );
            }
        
            //exit(urldecode(json_encode($output)));
            exit(json_encode($output));
        }
    }
    else if($operation == 'get_hotlist')
    {
        $output = array();
        $data = $data_provider->get_hotlist();
        
        $count = 1;    
        foreach($data as $key)
        {
            $output[$count] = array(
                'item_flag' => $key['item_flag'],
                'item_href' => $key['item_href'],
                'item_title' => $key['item_title']
            );
            $count++;
        }
        
        exit(json_encode($output));
    }
    else if($operation == 'search')
    {
        if(empty($userID)||empty($catid)||empty($search_key))
        {
            
        }
        else
        {
            //$search_key = mb_convert_encoding($search_key, 'gb2312', 'utf-8');
			$search_key = urldecode($search_key);
        
            $data = $data_provider->search_news_list_by_id($userID, $catid, $search_key);
            $output['count'] = $data;
            
            exit(json_encode($output));
        }
    }
    else if($operation == 'search_all')
    {
        if(empty($userID)||empty($search_key))
        {
            
        }
        else
        {
            //$search_key = mb_convert_encoding($search_key, 'gb2312', 'utf-8');
			$search_key = urldecode($search_key);
        
            $data = $data_provider->search_news_list_by_all($userID, $search_key);
            $output['count'] = $data;
            
            exit(json_encode($output));
        }
    }
	else if($operation == 'get_search_list')
    {
        if(empty($userID)||empty($pages))
        {
            
        }
        else
        {
			$output = array();
            $data = $data_provider->get_search_result($userID, $pages);
            
            $count = 1;
            
            foreach($data as $key)
            {
                $output[$count] = array(
                    'item_flag' => $key['item_flag'],
                    'item_href' => $key['item_href'],
                    'item_description' => $key['item_description'],
                    'item_date' => $key['item_date'],
                    'item_category' => $key['item_category']
                );
                $count++;
            }
        
            //exit(urldecode(json_encode($output)));
            exit(json_encode($output));
        }
    }
	else if($operation == 'get_search_list_summary')
    {
        if(empty($userID)||empty($pages))
        {
            
        }
        else
        {
			$output = array();
            $data = $data_provider->get_search_result($userID, $pages);
            
            $count = 1;
			
			foreach($data as $key)
			{
				$data2 = $data_provider->get_content_by_id($key['item_category'], $key['item_flag']);
				
				foreach($data2 as $key2)
				{
					$summary = $key2['item_summary'];
					//echo $summary."</br>";
				}
			
				$output[$count] = array(
					'item_flag' => $key['item_flag'],
					'item_href' => $key['item_href'],
					'item_description' => $key['item_description'],
					'item_summary' => $summary,
					'item_date' => $key['item_date'],
					'item_category' => $key['item_category']
				);
				$count++;
			}
			
			exit(json_encode($output));
        }
    }
	else if($operation == 'search_finish')
	{
		if(empty($userID))
		{
			
		}
		else
		{
			$data_provider->search_finish($userID);
			exit();
		}
	}
    else if($operation == 'get_category')
    {
        $database_name = "db_mynews";
        $table_name = "category";
        
        $data = $data_provider->get_category();
        
        $output = array();
        
        foreach($data as $key)
        {   
            $output[$key['category_flag']] = array(
                'category_flag' => $key['category_flag'],
                'category_href' => $key['category_href'],
                'category_description' => $key['category_description']
            );
        }
        
        exit(urldecode(json_encode($output)));
    }
    
?>
