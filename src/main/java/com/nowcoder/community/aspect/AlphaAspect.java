package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//这是一个方面组件（切面）
//@Aspect
public class AlphaAspect {

    //第一个星号代表返回的是任何类型，后面的代码串表示你要织入代码的地方
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){

    }

    //在这些切点执行之前做动作
    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }

    //在这些切点执行之后做动作
    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }

    //在这些切点返回（返回值）之后做动作
    @AfterReturning("pointcut()")
    public void AfterReturning(){
        System.out.println("AfterReturning");
    }

    //在这些切点抛出异常之后做动作
    @AfterThrowing("pointcut()")
    public void AfterThrowing(){
        System.out.println("AfterThrowing");
    }

    //既可以在这些切点执行前做动作，也可在执行后做动作
    @Around("pointcut()")
    public Object Around(ProceedingJoinPoint joinPoint) throws Throwable{
        System.out.println("Around before");
        //joinPoint.proceed()调用目标组件的方法,这样可以实现在这个方法前可以选择编写你要织入的逻辑，同时这个方法后也可以选择编写你要织入的逻辑
        Object obj = joinPoint.proceed();
        System.out.println("Around after");
        return obj;

    }

}
