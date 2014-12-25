<?php

header("Content-Type: text/html; charset=UTF-8");

class DataBase
{
	private $db;

	public function __construct($host, $username, $password)
	{
		// connect to MySQL.
		$this->db = mysql_connect($host, $username, $password);
	}

	public function __destruct()
	{
		// close connect of MySQL.
		if (!empty($this->db))
  		{
  			mysql_close($this->db);
  		}
	}

	public function createDataBase($database_name)
	{
		$sql = "CREATE DATABASE IF NOT EXISTS ".$database_name." CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'";
        if (!mysql_query($sql, $this->db))
  		{
  			echo "Error creating database: ".mysql_error();
  		}
    }

    public function createTable($database_name, $table_name, $column_name)
    {
		mysql_select_db($database_name, $this->db);
        $sql = "CREATE TABLE IF NOT EXISTS ".$table_name."(".$column_name.") ENGINE=InnoDB DEFAULT CHARSET = UTF8";
		mysql_query($sql, $this->db);
    }
	
	public function createView($database_name, $view_name, $column_name, $data)
	{
		mysql_select_db($database_name, $this->db);
        $sql = "CREATE OR REPLACE VIEW ".$view_name."(".$column_name.")"." AS ".$data;
		mysql_query($sql, $this->db);
	}
	
	public function dropView($database_name, $view_name)
	{
		mysql_select_db($database_name, $this->db);
        $sql = "DROP VIEW ".$view_name;
		mysql_query($sql, $this->db);
	}

    public function insertEntry($database_name, $table_name, $column_name, $values)
    {
    	mysql_select_db($database_name, $this->db);
    	$sql = "INSERT INTO ".$table_name."(".$column_name.")"." VALUES "."(".$values.")";
    	mysql_query($sql, $this->db);
    }

    public function selectEntry($database_name, $table_name, $column_name)
    {
        mysql_select_db($database_name, $this->db);
    	$sql = "SELECT ".$column_name." FROM ".$table_name;
    	$result = mysql_query($sql, $this->db);

    	return $result;
    }
	
	public function selectEntryMore($database_name, $table_name, $column_name, $con)
    {
        mysql_select_db($database_name, $this->db);
    	$sql = "SELECT ".$column_name." FROM ".$table_name." ".$con;
    	$result = mysql_query($sql, $this->db);

    	return $result;
    }

    public function selectEntryOrder($database_name, $table_name, $column_name, $order)
    {
        mysql_select_db($database_name, $this->db);
    	$sql = "SELECT ".$column_name." FROM ".$table_name." ORDER BY ".$order;
    	$result = mysql_query($sql, $this->db);

    	return $result;
    }

    public function selectEntryWhere($database_name, $table_name, $column_name, $con)
    {
        mysql_select_db($database_name, $this->db);
    	$sql = "SELECT ".$column_name." FROM ".$table_name." WHERE ".$con;
    	$result = mysql_query($sql, $this->db);

    	return $result;
    }
    
    public function selectEntryWhereOrder($database_name, $table_name, $column_name, $con, $order)
    {
        mysql_select_db($database_name, $this->db);
    	$sql = "SELECT ".$column_name." FROM ".$table_name." WHERE ".$con." ORDER BY ".$order;
    	$result = mysql_query($sql, $this->db);

    	return $result;
    }

    public function updateEntry($database_name, $table_name, $column_name, $values, $con)
    {
    	mysql_select_db($database_name, $this->db);
    	$sql = "UPDATE ".$table_name." SET ".$column_name." = ".$values." WHERE ".$con;
    	mysql_query($sql, $this->db);
    }

    public function deleteAllEntry($database_name, $table_name)
    {
    	mysql_select_db($database_name, $this->db);
    	$sql = "DELETE FROM ".$table_name;
    	mysql_query($sql, $this->db);
    }
    
    public function deleteEntry($database_name, $table_name, $con)
    {
    	mysql_select_db($database_name, $this->db);
    	$sql = "DELETE FROM ".$table_name." WHERE ".$con;
    	mysql_query($sql, $this->db);
    }
    
    public function copyEntry($database_name, $table_name, $column_name, $target_table_name, $target_column_name, $con)
    {
        mysql_select_db($database_name, $this->db);
        $sql = "INSERT INTO ".$target_table_name." (".$target_column_name.") "."SELECT (".$column_name.") FROM ".$con;
        mysql_query($sql, $this->db);
    }

}

?>
