package team.sailboat.base.schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数组、JSONArray类型
 *
 * @author yyl
 * @since 2024年1月18日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArrayType implements Type
{
	/**
	 * 数组的元素类型
	 */
	Type itemType ;
}
