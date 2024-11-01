package team.sailboat.commons.fan.infc;

@FunctionalInterface
public interface EIntFunction<R , X extends Throwable> {

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */
    R apply(int value) throws X ;
}