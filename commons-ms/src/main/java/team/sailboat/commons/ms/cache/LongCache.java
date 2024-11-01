package team.sailboat.commons.ms.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.cache.annotation.Cacheable;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Cacheable(cacheNames = "longCache" , keyGenerator = "com.cimstech.ms.common.cache.MethodKeyGen")
public @interface LongCache {
}
