package com.anshuman.statemachinedemo.exception;

public class StateMachineException extends WorkflowException {

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
