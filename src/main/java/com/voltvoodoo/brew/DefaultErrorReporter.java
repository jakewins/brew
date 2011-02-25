package com.voltvoodoo.brew;

import org.apache.maven.plugin.logging.Log;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

public class DefaultErrorReporter implements ErrorReporter
{
    private String defaultFilename;
    private boolean acceptWarn;
    private Log log;
    private int warningCount;
    private int errorCount;

    public DefaultErrorReporter(Log log, boolean acceptWarnings) {
        this.log = log;
        this.acceptWarn = acceptWarnings;
    }

    public void setDefaultFileName(String v) {
        if (v.length() == 0) {
            v = null;
        }
        defaultFilename = v;
    }

    public int getErrorCnt() {
        return errorCount;
    }

    public int getWarningCnt() {
        return warningCount;
    }

    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        String fullMessage = newMessage(message, sourceName, line, lineSource, lineOffset);
        log.error(fullMessage);
        errorCount++;
    }

    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        error(message, sourceName, line, lineSource, lineOffset);
        throw new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
    }

    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
        if (acceptWarn) {
            String fullMessage = newMessage(message, sourceName, line, lineSource, lineOffset);
            log.warn(fullMessage);
            warningCount++;
        }
    }

    private String newMessage(String message, String sourceName, int line, String lineSource, int lineOffset) {
        StringBuilder back = new StringBuilder();
        if ((sourceName == null) || (sourceName.length() == 0)) {
            sourceName = defaultFilename;
        }
        if (sourceName != null) {
            back.append(sourceName)
                .append(":line ")
                .append(line)
                .append(":column ")
                .append(lineOffset)
                .append(':')
                ;
        }
        if ((message != null) && (message.length() != 0)) {
            back.append(message);
        } else {
            back.append("unknown error");
        }
        if ((lineSource != null) && (lineSource.length() != 0)) {
            back.append("\n\t")
                .append(lineSource)
                ;
        }
        return back.toString();
    }
}
