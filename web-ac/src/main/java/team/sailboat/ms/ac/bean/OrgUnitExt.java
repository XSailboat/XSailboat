package team.sailboat.ms.ac.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import team.sailboat.ms.ac.dbean.OrgUnit;

/**
 * 
 * 在BOrgUnit基础上扩展一些属性
 *
 * @author yyl
 * @since 2024年11月29日
 */
@Schema(description = "在BOrgUnit基础上扩展一些属性")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrgUnitExt extends OrgUnit.BOrgUnit
{
	boolean hasChildren ;
}
