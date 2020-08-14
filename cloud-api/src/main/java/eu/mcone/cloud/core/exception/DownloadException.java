package eu.mcone.cloud.core.exception;

import eu.mcone.cloud.core.exception.CloudException;

public class DownloadException extends CloudException {

    public DownloadException() {
        super();
    }

    public DownloadException(String s) {
        super(s);
    }

    public DownloadException(Throwable t) {
        super(t);
    }

    public DownloadException(String s, Throwable t) {
        super(s, t);
    }

}
