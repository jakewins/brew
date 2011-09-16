package com.voltvoodoo.brew;

import java.util.Map;

/**
 * Settings for uglify.
 */
public class Uglify {
    /**
     * @parameter
     */
    private Map<String, String> gen_codeOptions;
    /**
     * @parameter
     */
    private Map<String, String> strict_semicolons;
    /**
     * @parameter
     */
    private Map<String, String> do_toplevel;
    /**
     * @parameter
     */
    private Map<String, String> ast_squeezeOptions;

    public Map<String, String> getGen_codeOptions() {
        return gen_codeOptions;
    }

    public Map<String, String> getStrict_semicolons() {
        return strict_semicolons;
    }

    public Map<String, String> getDo_toplevel() {
        return do_toplevel;
    }

    public Map<String, String> getAst_squeezeOptions() {
        return ast_squeezeOptions;
    }
}
