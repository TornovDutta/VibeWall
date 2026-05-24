package org.example.vibewall.AOP;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LoggingAspect {
    @After("execution(* org.example.vibewall.service.serviceImpl.RegistrationServiceImpl.login(..))")
    public void newUsers() {
        log.info("new user add");
    }

    @After("execution(* org.example.vibewall.service.serviceImpl.RegistrationServiceImpl.adduser(..))")
    public void newUserRegister() {
        log.info("new user add to database");
    }

    @After("execution(* org.example.vibewall.service.serviceImpl.AdminServiceImpl.addAdmin(..))")
    public void newAdmin() {
        log.info("new admin addd");
    }
}
