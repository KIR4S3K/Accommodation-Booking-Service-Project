package app.accommodationbookingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AlreadyCanceledException extends RuntimeException {
    public AlreadyCanceledException(String message) {
        super(message);
    }
}
