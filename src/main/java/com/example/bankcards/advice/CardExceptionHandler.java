package com.example.bankcards.advice;

import com.example.bankcards.advice.response.*;
import com.example.bankcards.exception.card.*;
import com.example.bankcards.exception.card.encryptor.*;
import com.example.bankcards.exception.customer.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;

@RestControllerAdvice
public class CardExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CardWithNumberAlreadyExistsException.class)
    private RuntimeExceptionResponse cardWithNumberAlreadyExists(CardWithNumberAlreadyExistsException e){
        return getExceptionResponse(e);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CardWithNumberNoExistsException.class)
    private RuntimeExceptionResponse cardWithNumberNoExists(CardWithNumberNoExistsException e){
        return getExceptionResponse(e);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CardByIdNotExistException.class)
    private RuntimeExceptionResponse cardByIdNotExist(CardByIdNotExistException e){
        return getExceptionResponse(e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NoAccessToOtherDataException.class)
    private RuntimeExceptionResponse insufficientFunds(NoAccessToOtherDataException e){
        return getExceptionResponse(e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InsufficientFundsException.class)
    private RuntimeExceptionResponse insufficientFunds(InsufficientFundsException e){
        return getExceptionResponse(e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CardBlockedException.class)
    private RuntimeExceptionResponse cardBlocked(CardBlockedException e){
        return getExceptionResponse(e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(LimitExhaustedException.class)
    private RuntimeExceptionResponse limitExhausted(LimitExhaustedException e){
        return getExceptionResponse(e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(EncryptorException.class)
    private RuntimeExceptionResponse cardEncrypted(EncryptorException e){
        return getExceptionResponse(e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DecryptException.class)
    private RuntimeExceptionResponse cardDecrypted(DecryptException e){
        return getExceptionResponse(e);
    }

    private RuntimeExceptionResponse getExceptionResponse(Exception e) {
        return new RuntimeExceptionResponse(e.getMessage(), LocalDateTime.now());
    }
}
