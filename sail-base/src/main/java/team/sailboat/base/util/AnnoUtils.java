package team.sailboat.base.util;

import java.io.IOException;
import java.util.function.BiConsumer;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;


public class AnnoUtils
{
	/**
	 * 获取指定包下指定类型的注解
	 * @param <T>
	 * @param aPackageName
	 * @param aRecusive
	 * @param aAnnoType
	 * @return
	 * @throws IOException 
	 */
	public static void visitAnnotations(String aPackageName , boolean aRecusive , Class<?> aAnnoType
			, BiConsumer<String , MultiValueMap<String, Object>> aAnnoAttrConsumer) throws IOException
	{
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                ClassUtils.convertClassNameToResourcePath(aPackageName) + "/**/*.class" ;
        Resource[] resources = resourcePatternResolver.getResources(pattern);
        //MetadataReader 的工厂类
        MetadataReaderFactory readerfactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        for (Resource resource : resources)
        {
            //用于读取类信息
            MetadataReader reader = readerfactory.getMetadataReader(resource);
            //扫描到的class
            AnnotationMetadata annoMeta = reader.getAnnotationMetadata()  ;
            MultiValueMap<String, Object> mvMap = annoMeta.getAllAnnotationAttributes(aAnnoType.getName()) ;
            if(mvMap != null)
            {
            	aAnnoAttrConsumer.accept(annoMeta.getClassName() , mvMap);
            }
        }
	}
}
