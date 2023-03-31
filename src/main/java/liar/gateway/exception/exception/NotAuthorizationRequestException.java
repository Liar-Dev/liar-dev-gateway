package liar.gateway.exception.exception;


import liar.gateway.exception.type.ExceptionCode;
import liar.gateway.exception.type.ExceptionMessage;

public class NotAuthorizationRequestException extends CommonException {

    public NotAuthorizationRequestException() {super(ExceptionCode.UNAUTHORIZED, ExceptionMessage.UNAUTHORIZED);}
}
