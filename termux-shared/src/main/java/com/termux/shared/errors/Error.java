package com.termux.shared.errors;

import android.content.Context;

import androidx.annotation.NonNull;

import com.termux.shared.logger.Logger;
import com.termux.shared.markdown.MarkdownUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Error implements Serializable {

    /** The optional error label. */
    private String label;
    /** The error type. */
    private String type;
    /** The error code. */
    private int code;
    /** The error message. */
    private String message;
    /** The error exceptions. */
    private List<Throwable> throwableList = new ArrayList<>();

    private static final String LOG_TAG = "Error";

    public Error(String type, Integer code, String message, List<Throwable> throwableList) {
        this.type = type;
        this.code = code;
        this.message = message;
        if (throwableList != null) {
            this.throwableList = throwableList;
        }
    }

    /** The {@link Class} that defines a builder for {@link Error} class. */
    public static class ErrorBuilder {
        private String type = null;
        private Integer code = null;
        private String message = null;
        private List<Throwable> throwableList = new ArrayList<>();

        /**
         * Set type for {@link ErrorBuilder}.
         *
         * @param type The error type.
         */
        public ErrorBuilder setType(String type) {
            if (type != null && !type.isEmpty())
                this.type = type;
            else
                this.type = Errno.TYPE;
            return this;
        }

        /**
         * Set code for {@link ErrorBuilder}.
         *
         * @param code The error code.
         */
        public ErrorBuilder setCode(Integer code) {
            if (code != null && code > Errno.ERRNO_SUCCESS.getCode())
                this.code = code;
            else
                this.code = Errno.ERRNO_SUCCESS.getCode();
            return this;
        }

        /**
         * Set message for {@link ErrorBuilder}.
         *
         * @param message The error message.
         */
        public ErrorBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Set throwableList for {@link ErrorBuilder}.
         *
         * @param throwable The error exception (just a single throwable).
         */
        public ErrorBuilder setThrowableList(Throwable throwable) {
            return this.setThrowableList(Collections.singletonList(throwable));
        }

        /**
         * Set throwableList for {@link ErrorBuilder}.
         *
         * @param throwableList The error exceptions.
         */
        public ErrorBuilder setThrowableList(List<Throwable> throwableList) {
            if (throwableList != null)
                this.throwableList = throwableList;
            return this;
        }

        /**
         * Returns {@link Error} object.
         *
         */
        public Error build() {
            return new Error(type, code, message, throwableList);
        }
    }

    public Error setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getLabel() {
        return label;
    }


    public String getType() {
        return type;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void prependMessage(String message) {
        if (message != null && isStateFailed())
            this.message = message + this.message;
    }

    public void appendMessage(String message) {
        if (message != null && isStateFailed())
            this.message = this.message + message;
    }

    public List<Throwable> getThrowableList() {
        return Collections.unmodifiableList(throwableList);
    }


    public synchronized boolean setStateFailed(@NonNull Error error) {
        return setStateFailed(error.getType(), error.getCode(), error.getMessage(), null);
    }

    public synchronized boolean setStateFailed(@NonNull Error error, Throwable throwable) {
        return setStateFailed(error.getType(), error.getCode(), error.getMessage(), Collections.singletonList(throwable));
    }
    public synchronized boolean setStateFailed(@NonNull Error error, List<Throwable> throwablesList) {
        return setStateFailed(error.getType(), error.getCode(), error.getMessage(), throwablesList);
    }

    public synchronized boolean setStateFailed(int code, String message) {
        return setStateFailed(this.type, code, message, null);
    }

    public synchronized boolean setStateFailed(int code, String message, Throwable throwable) {
        return setStateFailed(this.type, code, message, Collections.singletonList(throwable));
    }

    public synchronized boolean setStateFailed(int code, String message, List<Throwable> throwablesList) {
        return setStateFailed(this.type, code, message, throwablesList);
    }

    public synchronized boolean setStateFailed(String type, int code, String message, List<Throwable> throwablesList) {
        this.message = message;
        this.throwableList = throwablesList;

        if (type != null && !type.isEmpty())
            this.type = type;

        if (code > Errno.ERRNO_SUCCESS.getCode()) {
            this.code = code;
            return true;
        } else {
            Logger.logWarn(LOG_TAG, "Ignoring invalid error code value \"" + code + "\". Force setting it to RESULT_CODE_FAILED \"" + Errno.ERRNO_FAILED.getCode() + "\"");
            this.code = Errno.ERRNO_FAILED.getCode();
            return false;
        }
    }

    public boolean isStateFailed() {
        return code > Errno.ERRNO_SUCCESS.getCode();
    }


    @NonNull
    @Override
    public String toString() {
        return getErrorLogString(this);
    }



    /**
     * Log the {@link Error} and show a toast for the minimal {@link String} for the {@link Error}.
     *
     * @param context The {@link Context} for operations.
     * @param logTag The log tag to use for logging.
     * @param error The {@link Error} to convert.
     */
    public static void logErrorAndShowToast(Context context, String logTag, Error error) {
        if (error == null) return;
        error.logErrorAndShowToast(context, logTag);
    }

    public void logErrorAndShowToast(Context context, String logTag) {
        Logger.logErrorExtended(logTag, getErrorLogString());
        Logger.showToast(context, getMinimalErrorLogString(), true);
    }


    /**
     * Get a log friendly {@link String} for {@link Error} error parameters.
     *
     * @param error The {@link Error} to convert.
     * @return Returns the log friendly {@link String}.
     */
    public static String getErrorLogString(final Error error) {
        if (error == null) return "null";
        return error.getErrorLogString();
    }

    public String getErrorLogString() {
        StringBuilder logString = new StringBuilder();

        logString.append(getCodeString());
        logString.append("\n").append(getTypeAndMessageLogString());
        if (throwableList != null && throwableList.size() > 0)
            logString.append("\n").append(geStackTracesLogString());

        return logString.toString();
    }

    /**
     * Get a minimal log friendly {@link String} for {@link Error} error parameters.
     *
     * @param error The {@link Error} to convert.
     * @return Returns the log friendly {@link String}.
     */
    public static String getMinimalErrorLogString(final Error error) {
        if (error == null) return "null";
        return error.getMinimalErrorLogString();
    }

    public String getMinimalErrorLogString() {
        StringBuilder logString = new StringBuilder();

        logString.append(getCodeString());
        logString.append(getTypeAndMessageLogString());

        return logString.toString();
    }

    /**
     * Get a minimal {@link String} for {@link Error} error parameters.
     *
     * @param error The {@link Error} to convert.
     * @return Returns the {@link String}.
     */
    public static String getMinimalErrorString(final Error error) {
        if (error == null) return "null";
        return error.getMinimalErrorString();
    }

    public String getMinimalErrorString() {
        StringBuilder logString = new StringBuilder();

        logString.append("(").append(getCode()).append(") ");
        logString.append(getType()).append(": ").append(getMessage());

        return logString.toString();
    }

    /**
     * Get a markdown {@link String} for {@link Error}.
     *
     * @param error The {@link Error} to convert.
     * @return Returns the markdown {@link String}.
     */
    public static String getErrorMarkdownString(final Error error) {
        if (error == null) return "null";
        return error.getErrorMarkdownString();
    }

    public String getErrorMarkdownString() {
        StringBuilder markdownString = new StringBuilder();

        markdownString.append(MarkdownUtils.getSingleLineMarkdownStringEntry("Error Code", getCode(), "-"));
        markdownString.append("\n").append(MarkdownUtils.getMultiLineMarkdownStringEntry(
                (Errno.TYPE.equals(getType()) ? "Error Message" : "Error Message (" + getType() + ")"), message, "-"));
        if (throwableList != null && throwableList.size() > 0)
            markdownString.append("\n\n").append(geStackTracesMarkdownString());

        return markdownString.toString();
    }


    public String getCodeString() {
        return Logger.getSingleLineLogStringEntry("Error Code", code, "-");
    }

    public String getTypeAndMessageLogString() {
        return Logger.getMultiLineLogStringEntry(Errno.TYPE.equals(type) ? "Error Message" : "Error Message (" + type + ")", message, "-");
    }

    public String geStackTracesLogString() {
        return Logger.getStackTracesString("StackTraces:", Logger.getStackTracesStringArray(throwableList));
    }

    public String geStackTracesMarkdownString() {
        return Logger.getStackTracesMarkdownString("StackTraces", Logger.getStackTracesStringArray(throwableList));
    }

}
