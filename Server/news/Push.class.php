<?php

require_once('/Baidu-Push-SDK/Channel.class.php');

class Push
{

    private $apiKey = '29YD39qQzQein6IdaMQ57GhO';
    private $secretKey = 'Dizrbts8IY5mo82l6CBwNmQbRRb31z05';

    public function __construct()
	{
	}

	public function __destruct()
	{
	}

    public function error_output($str)
    {
        echo "\033[1;40;31m".$str."\033[0m"."\n";
    }

    public function right_output($str)
    {
        echo "\033[1;40;32m".$str."\033[0m"."\n";
    }


    public function test_queryBindList($userId)
    {
        $channel = new Channel($this->apiKey, $this->secretKey);
        $optional [ Channel::CHANNEL_ID ] = "3915728604212165383";
        $ret = $channel->queryBindList ( $userId, $optional ) ;
        if(false === $ret)
        {
            error_output('WRONG, '. __FUNCTION__ .' ERROR!!!!!' ) ;
            error_output('ERROR NUMBER: ' . $channel->errno ( ) ) ;
            error_output('ERROR MESSAGE: ' . $channel->errmsg ( ) ) ;
            error_output('REQUEST ID: ' . $channel->getRequestId ( ) );
        }
        else
        {
            right_output ( 'SUCC, ' . __FUNCTION__ . ' OK!!!!!' ) ;
            right_output ( 'result: ' . print_r ( $ret, true ) ) ;
        }
    }

    //推送android设备消息
    public function test_pushMessage_android()
    {
        $channel = new Channel ( $this->apiKey, $this->secretKey ) ;
        //推送消息到某个user，设置push_type = 1;
        //推送消息到一个tag中的全部user，设置push_type = 2;
        //推送消息到该app中的全部user，设置push_type = 3;
        //$push_type = 1; //推送单播消息
        //$optional[Channel::USER_ID] = $user_id; //如果推送单播消息，需要指定user
        //$optional[Channel::TAG_NAME] = "xxxx";  //如果推送tag消息，需要指定tag_name

        $push_type = 3;

        //指定发到android设备
        $optional[Channel::DEVICE_TYPE] = 3;
        //指定消息类型为通知
        $optional[Channel::MESSAGE_TYPE] = 1;
        //通知类型的内容必须按指定内容发送，示例如下：
        $message = '{
                "title": "test_push",
                "description": "open url",
                "notification_basic_style":7,
                "open_type":1,
                "url":"http://www.baidu.com"
            }';

        $message_key = "msg_key";
        $ret = $channel->pushMessage ( $push_type, $message, $message_key, $optional ) ;
        if ( false === $ret )
        {
            $this->error_output ( 'WRONG, ' . __FUNCTION__ . ' ERROR!!!!!' ) ;
            $this->error_output ( 'ERROR NUMBER: ' . $channel->errno ( ) ) ;
            $this->error_output ( 'ERROR MESSAGE: ' . $channel->errmsg ( ) ) ;
            $this->error_output ( 'REQUEST ID: ' . $channel->getRequestId ( ) );
        }
        else
        {
            $this->right_output ( 'SUCC, ' . __FUNCTION__ . ' OK!!!!!' ) ;
            $this->right_output ( 'result: ' . print_r ( $ret, true ) ) ;
        }
    }

    public function push_message_to_android($title, $description)
    {
        $channel = new Channel($this->apiKey, $this->secretKey) ;

        $push_type = 3;

        //指定发到android设备
        $optional[Channel::DEVICE_TYPE] = 3;
        //指定消息类型为通知
        $optional[Channel::MESSAGE_TYPE] = 1;
        //通知类型的内容必须按指定内容发送，示例如下：
        /*
                notification_basic_style:只有notification_builder_id为0时才有效，才需要设置；该属性是整型，每一位代表一种基本样式，基本样式用二进制位表示如下
                                响铃：0100B=0x04
                                振动：0010B=0x02
                                可清除：0001B=0x01
                    如果需要同时设置多种基本样式，可以对上述三种基本样式做“或”运算，例如要设置通知为响铃、振动和可清除、则notification_basic_style 值为：
                    notification_basic_style=0100B | 0010B | 0001B= 0111B=0x07
            */
        $message = '{
                "title": "'.$title.'",
                "description": "'.$description.'",
                "notification_builder_id": 0,
                "notification_basic_style": 7,
                "open_type":2
            }';

        $message_key = "msg_key";
        $ret = $channel->pushMessage($push_type, $message, $message_key, $optional);
        if(false === $ret)
        {
            $this->error_output ( 'WRONG, ' . __FUNCTION__ . ' ERROR!!!!!' ) ;
            $this->error_output ( 'ERROR NUMBER: ' . $channel->errno ( ) ) ;
            $this->error_output ( 'ERROR MESSAGE: ' . $channel->errmsg ( ) ) ;
            $this->error_output ( 'REQUEST ID: ' . $channel->getRequestId ( ) );
        }
        else
        {
            $this->right_output ( 'SUCC, ' . __FUNCTION__ . ' OK!!!!!' ) ;
            $this->right_output ( 'result: ' . print_r ( $ret, true ) ) ;
        }
    }
}

?>
