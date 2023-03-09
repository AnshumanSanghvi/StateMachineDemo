package com.anshuman.workflow.annotation;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on methods to log the time taken for execution.
 */
@Target({METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Profile {

}
