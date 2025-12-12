package com.data.anac_api.exception;

import com.data.anac_api.utils.DataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<DataResponse<Void>> handleRuntimeException(RuntimeException ex) {
    DataResponse<Void> response = new DataResponse<>(new Date(), true, ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<DataResponse<Void>> handleGenericException(Exception ex) {
    DataResponse<Void> response = new DataResponse<>(new Date(), true, "Erreur interne du serveur : " + ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(RessourceNotFoundException.class)
  public final ResponseEntity<DataResponse<Void>> handleResourceNotFound(Exception ex)  {
    DataResponse<Void> response = new DataResponse<>(new Date(),true, "Ressource inexistante : " +ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(BadRequestException.class)
  public final ResponseEntity<DataResponse<Void>> handleBadRequestException(Exception ex)  {
    DataResponse<Void> response = new DataResponse<>(new Date(),true, "Erreur de demande : " +ex.getMessage(), null);
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }
}
