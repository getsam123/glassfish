package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.interceptor.*;
import java.lang.reflect.*;


public class BaseBean {

    // InterceptorA
    boolean ac = false;

    // InterceptorB
    boolean ac1 = false;

    // InterceptorC
    boolean ac2 = false;

    Method method = null;

    void verifyMethod(String name) {
        if (method == null) {
            if (name != null) throw new RuntimeException("In " + getClass().getName() + " expected method name: " + name + " got null");
        } else {
            if (!method.getName().equals(name)) 
                throw new RuntimeException("In " + getClass().getName() + " expected method name: " + name + " got: " + method.getName());
        }
    }

    void verify(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (!ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was not called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorC.AroundConstruct was called");
    }

    void verifyA(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorC.AroundConstruct was called");
    }

    void verifyA_AC(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
    }

    void verifyB_AC(String name) {
        if (ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was called");
        if (!ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was not called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
    }

    void verifyAB_AC(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (!ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was not called");
        if (ac2) throw new RuntimeException("[" + name + "] InterceptorC.AroundConstruct was called");
    }

    void verifyAC_AC(String name) {
        if (!ac) throw new RuntimeException("[" + name + "] InterceptorA.AroundConstruct was not called");
        if (ac1) throw new RuntimeException("[" + name + "] InterceptorB.AroundConstruct was called");
        if (!ac2) throw new RuntimeException("[" + name + "] InterceptorC.AroundConstruct was not called");
    }
}
