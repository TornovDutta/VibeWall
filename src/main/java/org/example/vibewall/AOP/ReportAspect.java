package org.example.vibewall.AOP;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class ReportAspect {
    @After("")
    public void newReport(){
        log.info("new Report add");
    }

    @After("")
    public  void reportSolve(){
        log.info("report solve");
    }

}
