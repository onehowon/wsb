package com.ebiz.wsb.global.api;

import com.ebiz.wsb.domain.guardian.exception.FileUploadException;
import com.ebiz.wsb.domain.location.exception.InvalidLocationDataException;
import com.ebiz.wsb.domain.notice.exception.NoticeNotFoundException;
import com.ebiz.wsb.domain.token.exception.InvalidTokenException;
import com.ebiz.wsb.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;

// 특정 Exception 발생 시 해당 응답을 반환해 에러 메시지를 클라이언트한테 반환

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String WRONG_USER_EXCEPTION_MESSAGE = "이메일 혹은 비밀번호가 일치하지 않습니다";
    private static final String WRONG_REQUEST_EXCEPTION_MESSAGE = "잘못된 요청입니다";
    private static final String MAX_UPLOAD_SIZE_EXCEEDED_MESSAGE = "사진은 최대 10MB까지 업로드 가능합니다";

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity handleBadCredentialsException() {
        return new ResponseEntity(ErrorResponse.builder()
                .message(WRONG_USER_EXCEPTION_MESSAGE)
                .build(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleNullRequestException(MethodArgumentNotValidException e) {
        return new ResponseEntity(ErrorResponse.builder()
                .message(e.getBindingResult().getFieldErrors().get(0).getDefaultMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoticeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoticeNotFoundException(NoticeNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .message("파일 처리 중 오류가 발생했습니다: " + ex.getMessage())
                        .build());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .message(MAX_UPLOAD_SIZE_EXCEEDED_MESSAGE)
                        .build());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder().
                        message("유효하지 않은 토큰입니다.")
                        .build());
    }

    @ExceptionHandler(InvalidLocationDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLocationDataException(InvalidLocationDataException ex){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .message("잘못된 위치 데이터입니다.")
                        .build());
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponse> handleFileUploadException(FileUploadException ex){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .message(ex.getMessage())
                        .build());
    }
}