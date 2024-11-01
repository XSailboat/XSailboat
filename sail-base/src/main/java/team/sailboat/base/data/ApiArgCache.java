package team.sailboat.base.data;

import com.googlecode.aviator.Expression;

import lombok.Data;
import team.sailboat.base.dataset.ApiArg;

@Data
public class ApiArgCache
{
	ApiArg apiArg ;
	
	Object value ;
	
	String exprStr ;
	
	Expression expr ;
}
