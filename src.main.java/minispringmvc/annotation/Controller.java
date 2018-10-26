package annotation;

import java.lang.annotation.*;

@Documented //JAVADOC
@Target(ElementType.TYPE) //作用于类上
@Retention(RetentionPolicy.RUNTIME) //限制Annotation的生命周期，这里运行时保留
public @interface Controller {
    /**
     * 作用于该类上的注释有一个VALUE属性，直白的说就是controller的名称
     * @return
     */
    public String value();
}
