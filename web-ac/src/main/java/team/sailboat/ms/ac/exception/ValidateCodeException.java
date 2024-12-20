package team.sailboat.ms.ac.exception;

import org.springframework.security.core.AuthenticationException;

public class ValidateCodeException extends AuthenticationException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6444460135316030660L;

	public ValidateCodeException(String message) {
        super(message);
    }
}