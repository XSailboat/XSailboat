package team.sailboat.base.schema;

import java.util.List;

import lombok.Data;

/**
 * 对象、JSONArray类型
 *
 * @author yyl
 * @since 2024年1月18日
 */
@Data
public class ObjectType implements Type
{
	/**
	 * 字段
	 */
	List<Field> fields ;
}
