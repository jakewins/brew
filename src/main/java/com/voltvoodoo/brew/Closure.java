package com.voltvoodoo.brew;

import java.util.Map;

/**
 * Settings for closure compiler
 */
public class Closure {
    /**
     * @parameter
     */
    private Map<String, String> CompilerOptions;
    /**
     * @parameter default-value="SIMPLE_OPTIMIZATIONS"
     */
    private String CompilationLevel = "SIMPLE_OPTIMIZATIONS";
    /**
     * @parameter default-value="WARNING"
     */
    private String loggingLevel = "WARNING";

    public Map<String, String> getCompilerOptions() {
        return CompilerOptions;
    }

    public String getCompilationLevel() {
        return CompilationLevel;
    }

    public String getLoggingLevel() {
        return loggingLevel;
    }
}
