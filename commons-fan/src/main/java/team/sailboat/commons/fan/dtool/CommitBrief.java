package team.sailboat.commons.fan.dtool;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommitBrief
{
	long startSeq ;
	long endSeq ;
	
	Object[] lastRow ;
}
