package com.anshuman.statemachinedemo.exception;

public class StateMachinePersistenceException extends StateMachineException {

    public StateMachinePersistenceException() {
        super();
    }

    public StateMachinePersistenceException(String message) {
        super(message);
    }

    public StateMachinePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateMachinePersistenceException(Throwable cause) {
        super(cause);
    }

}
