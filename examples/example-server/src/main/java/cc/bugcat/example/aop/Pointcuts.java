package cc.bugcat.example.aop;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@Aspect
@Component
public class Pointcuts {

    @Pointcut("execution(* cc.bugcat.example.catserver.*.*.*(..))")
    public void validOpenapiPoint() {};


    @Around(value = "validOpenapiPoint()")
    public Object validOpenapiArround(ProceedingJoinPoint pjp) throws Throwable {
        return pjp.proceed();
    }
    
}
