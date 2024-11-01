package team.sailboat.aviator.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DateDuration
{
	public static char sUM_y = 'y' ;
	
	int value ;
	
	char unitMark ;
}
