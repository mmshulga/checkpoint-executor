package my.mmshulga.internal.aspect;

import my.mmshulga.internal.IWithPhaser;
import my.mmshulga.internal.annotation.CheckPoint;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.concurrent.Phaser;

@Aspect
public class CheckPointInvocationAspect {

    @Before("target(my.mmshulga.internal.IWithPhaser) " +
            "&& execution(@my.mmshulga.internal.annotation.CheckPoint * *(..))" +
            "&& within(@my.mmshulga.internal.annotation.CheckPointSyncedJob *)")
    public void checkPointAdvice(JoinPoint jp) {
        IWithPhaser obj = (IWithPhaser) jp.getTarget();
        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();

        try {
            CheckPoint checkPoint = obj.getClass()
                    .getDeclaredMethod(method.getName(), method.getParameterTypes())
                    .getAnnotation(CheckPoint.class);

            if (checkPoint == null) {
                return;
            }

            Phaser phaser = obj.__getPhaser();
            phaser.arrive();
            phaser.awaitAdvance(checkPoint.order());
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
}
