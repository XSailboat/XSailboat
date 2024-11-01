package org.sailboat.base.sql.test;

import team.sailboat.base.sql.PgBloodEngine;

public class PgBloodEngineTest
{

	public static void main(String[] args)
	{
		String sql = """
		WITH latest AS (
				SELECT b1, t1, d20, d21, d22, d23,
				    RANK() OVER (PARTITION BY b1 ORDER BY t1 DESC) AS rank
				FROM c600.ods_zhrtj
				WHERE b1 IN (${site_id::esites})
				AND t1 > ${start_time} 
				AND t1 < ${end_time}
				)
				SELECT 
				  round(SUM(d20)::NUMERIC ,2) AS year_generating_apacity,
				  round(SUM(d21)::NUMERIC ,2) AS year_pumped_capacity,
				  round(SUM(d22)::NUMERIC ,2) AS year_on_grid_energy,
				  round(SUM(d23)::NUMERIC ,2) AS year_off_grid_energy
				FROM latest
				WHERE rank = 1
		""" ;
		new PgBloodEngine("test").parse(sql) ;
		
	}

}
