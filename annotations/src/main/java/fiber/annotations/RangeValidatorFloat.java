package fiber.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.CLASS)
public @interface RangeValidatorFloat {
    float min() default Float.MIN_VALUE;
    float max() default Float.MAX_VALUE;
    int[] typeIndex() default {0};
}
