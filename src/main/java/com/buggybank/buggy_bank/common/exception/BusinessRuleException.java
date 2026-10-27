package com.buggybank.buggy_bank.common.exception;

/** A valid request that breaks a rule, e.g. withdrawing more than the piggy holds. */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}