package fiber.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.CLASS)
public @interface RangeValidatorLong {
    long min() default Long.MIN_VALUE;
    long max() default Long.MAX_VALUE;
    int[] typeIndex() default {0};
}
