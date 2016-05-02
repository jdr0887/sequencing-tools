package org.renci.seqtools.filter.vcf;

public class FilterVCFException extends Exception {

    private static final long serialVersionUID = -8690318178302673502L;

    public FilterVCFException() {
        super();
    }

    public FilterVCFException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public FilterVCFException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterVCFException(String message) {
        super(message);
    }

    public FilterVCFException(Throwable cause) {
        super(cause);
    }

}
