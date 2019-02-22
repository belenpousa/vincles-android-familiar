package cat.bcn.vincles.mobile.Client.Errors;


public class VinclesError {
    public static final String ERROR_DEFAULT = "0";
    public static final String ERROR_CONNECTION = "1";
    public static final String ERROR_CANCEL = "2";
    public static final String ERROR_LOGIN = "invalid_grant";
    public static final String ERROR_LOGIN_ATTEMPT = "invalid_grant";
    public static final String ERROR_CODE = "1301";
    public static final String ERROR_FILE_NOT_FOUND = "file_not_found";

    private String code = "0";
    private String message = "";

    public VinclesError() {
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
