package team.sailboat.commons.fan.res;

import java.util.function.Supplier;

public interface ResourceSupplier<T> extends AutoCloseable, Supplier<T>
{

}
