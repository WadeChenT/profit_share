package com.example.profit_share.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class PsException extends RuntimeException {

    private static final long serialVersionUID = 500515802716689108L;
    private LocalDateTime errTime;
    private String errMsg;
    private String errCode;

    private PsException() {
        super();
        this.errTime = LocalDateTime.now(ZoneId.systemDefault());
    }

    public static PsException occur() {
        return new PsException();
    }

    public static PsException occur(String errCode, String errMsg) {
        return occur().setErrCode(errCode)
                      .setErrMsg(errMsg);
    }

    public static PsException occur(PsErrorEnum error) {
        return occur(error.getErrCode(), error.getErrMsg());
    }

    @Getter
    public enum PsErrorEnum {
        COMMON_ERROR("9900", "Server Error."),
        WRONG_EMAIL_OR_PASSWORD("8787", "Wrong email address or password");

        private String errMsg;
        private String errCode;

        PsErrorEnum(String errCode, String errMsg) {
            this.errCode = errCode;
            this.errMsg = errMsg;
        }
    }

}
