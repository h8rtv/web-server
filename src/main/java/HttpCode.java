public enum HttpCode {
    NOT_FOUND("404 Not Found"),
    OK("200 OK");

    private String code;

    HttpCode(String code) {
        this.code = code;
    }
    public String toString() {
        return code;
    }
}