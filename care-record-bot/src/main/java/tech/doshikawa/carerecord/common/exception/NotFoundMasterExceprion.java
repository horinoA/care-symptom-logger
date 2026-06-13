package tech.doshikawa.carerecord.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundMasterExceprion extends CustomAppException {

    public NotFoundMasterExceprion(String message) {
        super(message);
    }

    public NotFoundMasterExceprion(String message,Object[] args){
        super(message, args);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
