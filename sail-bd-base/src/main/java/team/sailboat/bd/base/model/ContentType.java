package team.sailboat.bd.base.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 内容的类型
 *
 * @author yyl
 * @since 2021年6月16日
 */
@Schema(description = "内容的类型")
public enum ContentType
{
	object,
	hdfs,
	hbase
	;
}
