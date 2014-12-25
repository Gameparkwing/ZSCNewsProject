<?set_time_limit(0);?>

<?php

header("Content-Type: text/html; charset=UTF-8");

$RootDir = $_SERVER['DOCUMENT_ROOT'];

require_once($RootDir.'/news/DataProvider.class.php');

    $data_provider = new DataProvider();
    $data_provider->database_init();
    
	
    $data_provider->catch_news_category();
    
    $data_provider->catch_all_news_category_capacity();
    
    //$data_provider->catch_all_news_list();
    
    //$data_provider->catch_all_news_body();
    
    //$data_provider->check_all_news_category_capacity();
    
    //$data_provider->catch_news_list_by_id(5, 3, "/list.php?catid=38");
    
    //$data_provider->catch_a_news_body(9,16397);
    
    //$data_provider->search_news_list_by_all("2013", "2013");
	
    //$data_provider->get_search_result("2013", 1);
	//$data_provider->get_search_result("2013", 2);
	
    //$data_provider->catch_hotlist();
    
    //$data_provider->catch_a_news_list(13);
?>
