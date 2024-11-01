package team.sailboat.base.msg;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class Msg
{
	String id ;
	
	String partition ;
	
	MsgType type ;
	
	int level ;
	
	String content ;
	
	String data ;
	
	String eventTime ;
	
	Timestamp processTime ;
	
	String source ;
	
	String relatedMsgId ;
	
	String userId ;
	
	String userName ;
	
	String digest ;
	
	String clientId ;
	
	String firstTopicId ;
	
	String location ;
	
	String destination ;
	
	public Msg()
	{
	}
	
	public Msg(String[] aCells , Timestamp aProcessTime)
	{
		id = aCells[0] ;
		partition = aCells[1] ;
		type = MsgType.valueOf(aCells[2]) ;
		level = Integer.parseInt(aCells[3]) ;
		content = aCells[4] ;
		data = aCells[5] ;
		eventTime = aCells[6] ;
		
		processTime = aProcessTime ;
		
		source = aCells[7] ;
		relatedMsgId = aCells[8] ;
		userId = aCells[9] ;
		userName = aCells[10] ;
		digest = aCells[11] ;
		clientId = aCells[12] ;
		firstTopicId = aCells[13] ;
		location = aCells[14] ;
		destination = aCells[15] ;
	}
}
