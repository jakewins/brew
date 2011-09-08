package com.voltvoodoo.brew;

/**
 * Settings for the wrap properties.
 */
public class Wrap {
    /**
     * Wrap any build layer in a start and end text specified by wrap.
     * Use this to encapsulate the module code so that define/require are
     * not globals. The end text can expose some globals from your file,
     * making it easy to create stand-alone libraries that do not mandate
     * the end user use requirejs.
       E.g. start: "(function() {",
            end: "}());"
     * @parameter
     */
    private String start;
    /**
     * @parameter
     */
    private String end;
    /**
     * Another way to use wrap, but uses file paths. This makes it easier
     * to have the start text contain license information and the end text
     * to contain the global variable exports, like
     * window.myGlobal = requirejs('myModule');
     * File paths are relative to the build file, or if running a commmand
     * line build, the current directory.
     * @parameter
     */
    private String startFile;
    /**
     * @parameter
     */
    private String endFile;

}
