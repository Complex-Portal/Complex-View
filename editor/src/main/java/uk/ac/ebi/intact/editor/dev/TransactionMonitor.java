/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor.dev;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO comment this class header.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
//@Controller
@Aspect
public class TransactionMonitor {

//    @Around("execution(@org.springframework.transaction.annotation.Transactional * *(..))")
//    public Object debugInfoForTransaction(ProceedingJoinPoint pjp) throws Throwable {
//
//        System.out.println("Method: "+pjp.getSignature()+" - Transaction: "+pjp.);
//
//       Object retVal = pjp.proceed();
//
//        return retVal;
//
//    }

    @Pointcut("execution(@org.springframework.transaction.annotation.Transactional * *(..))")
    public void transactionalMethod() {}

    @Before("transactionalMethod()")
    public void beforeTransactionalMethod(JoinPoint joinPoint) {
        System.out.println("BEGIN TRANSACTION: "+joinPoint.getSignature());
        System.out.println("\t"+joinPoint.getSignature().getDeclaringType());
        //joinPoint.getSignature().getDeclaringType().getAnnotation(Transactional.class);
    }

    @AfterReturning("transactionalMethod()")
    public void afterTransactionalMethod(JoinPoint joinPoint) {
        System.out.println("END TRANSACTION: "+joinPoint.getSignature());
    }

    @AfterThrowing(pointcut = "transactionalMethod()", throwing = "e")
    public void afterThrowingFromTransactionalMethod(JoinPoint joinPoint, RuntimeException e) {
        System.out.println("ROLLING BACK TRANSACTION: "+joinPoint.getSignature());
    }


}
