package com.example

import io.micronaut.runtime.Micronaut
import groovy.transform.CompileStatic

@CompileStatic
class Application {
    static void main(String[] args) {
        Micronaut.build(args)
                // need to do this currently so that the RedisListener gets registered,
                // probably a problem with my implementation...
                .eagerInitSingletons(true)
                .mainClass(Application.class)
                .start()
//        Micronaut.run(Application, args)
    }
}
