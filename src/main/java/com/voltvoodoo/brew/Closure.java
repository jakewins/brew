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
}
