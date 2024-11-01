package team.sailboat.base.schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Field
{
	String name ;
	
	String description ;
	
	Type dataType ;
}
