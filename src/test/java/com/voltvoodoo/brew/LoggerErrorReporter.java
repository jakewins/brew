package com.voltvoodoo.brew;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import java.util.logging.Logger;

/**
 * An error reporter using java.util.Logger.
 */
public class LoggerErrorReporter implements ErrorReporter {
    private String defaultFilename;
    private boolean acceptWarn;
    private int warningCount;
    private int errorCount;
    private Logger log;

    public LoggerErrorReporter(Logger log, boolean acceptWarnings) {
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

    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        String fullMessage = newMessage(message, sourceName, line, lineSource, lineOffset);
        log.severe(fullMessage);
        errorCount++;
    }

    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        error(message, sourceName, line, lineSource, lineOffset);
        throw new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
    }

    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
        if (acceptWarn) {
            String fullMessage = newMessage(message, sourceName, line, lineSource, lineOffset);
            log.warning(fullMessage);
            warningCount++;
        }
    }

}
