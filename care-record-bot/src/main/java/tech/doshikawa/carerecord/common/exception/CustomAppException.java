package tech.doshikawa.carerecord.common.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public abstract class CustomAppException extends RuntimeException {
    private final Object[] args;

    public CustomAppException(String message) {
        this(message, null);
    }

    public CustomAppException(String message, Object[] args) {
        super(message);
        this.args = args;
    }
    
    // 子クラスごとに異なるステータスコードを返せるようにする（デフォルトは400）
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
