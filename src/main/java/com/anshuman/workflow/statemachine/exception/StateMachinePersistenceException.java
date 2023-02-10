package com.anshuman.workflow.statemachine.exception;

import com.anshuman.workflow.exception.WorkflowException;

public class StateMachinePersistenceException extends WorkflowException {

    public StateMachinePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

}
