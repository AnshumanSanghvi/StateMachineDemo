package com.anshuman.statemachinedemo.workflow.exception;

public class StateMachineException extends RuntimeException {

    public StateMachineException() {
        super();
    }

    public StateMachineException(String message) {
        super(message);
    }

    public StateMachineException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateMachineException(Throwable cause) {
        super(cause);
    }
}
