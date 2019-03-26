package fiber.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.CLASS)
public @interface RangeValidatorInt {
    int min() default Integer.MIN_VALUE;
    int max() default Integer.MAX_VALUE;
    int[] typeIndex() default {0};
}
