package ru.study.errors;

public class UndoError extends RuntimeException{
    public UndoError(String message) {
        super(message);
    }
}
