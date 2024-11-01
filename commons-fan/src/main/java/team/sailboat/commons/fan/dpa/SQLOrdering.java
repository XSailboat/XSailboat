package team.sailboat.commons.fan.dpa;

public enum SQLOrdering
{
	 ASC("ASC"),
	 DESC("DESC") ;
	    
	 public final String name;
	 public final String name_lcase;

	 private SQLOrdering(String name)
	 {
		 this.name = name;
		 this.name_lcase = name.toLowerCase();
	 }
}
