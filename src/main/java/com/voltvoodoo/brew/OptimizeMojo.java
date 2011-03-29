package com.voltvoodoo.brew;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.mozilla.javascript.ErrorReporter;

/**
 * 
 * @goal optimize
 * @phase process-resources
 * 
 */
public class OptimizeMojo extends AbstractMojo {

    /**
     * Javascript source directory. 
     *
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File optimizeSourceDir;
    
    /**
     * Build modules are put here.
     *
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File optimizeOutputDir;
    
    /**
     * Used to inline i18n resources into the built file. If no locale
     * is specified, i18n resources will not be inlined. Only one locale
     * can be inlined for a build. Root bundles referenced by a build layer
     * will be included in a build layer regardless of locale being set.
     *
     * @parameter expression="${requirejs.locale}" default="en-us"
     */
    private String locale = "en-us";
    
    /**
     * How to optimize all the JS files in the build output directory.
     * Right now only the following values
     * are supported:
     * - "uglify": uses UglifyJS to minify the code.
     * - "closure": (default) uses Google's Closure Compiler in simple optimization
     * mode to minify the code.
     * - "closure.keepLines": Same as closure option, but keeps line returns
     * in the minified files.
     * - "none": no minification will be done.
     * @parameter expression="${requirejs.jsOptimizer}" default="uglify"
     */
    private String jsOptimizer = "uglify";
    
    /**
     * Allow CSS optimizations. Allowed values:
     * - "standard": @import inlining, comment removal and line returns.
     * Removing line returns may have problems in IE, depending on the type
     * of CSS.
     * - "standard.keepLines": like "standard" but keeps line returns.
     * - "none": skip CSS optimizations.
     * @parameter expression="${requirejs.cssOptimizer}" default="standard"
     */
    private String cssOptimizer = "standard";
    
    /**
     * If optimizeCss is in use, a list of of files to ignore for the @import
     * inlining. The value of this option should be a comma separated list
     * of CSS file names to ignore. The file names should match whatever
     * strings are used in the @import calls.
     * @parameter 
     */
    private List<String> cssExcludes;
    

    /**
     * Inlines the text for any text! dependencies, to avoid the separate
     * async XMLHttpRequest calls to load those dependencies.
     * @parameter expression="${requirejs.inlineText}" default=true
     */
    private boolean inlineText = true;
    
    /**
     * Allow "use strict"; be included in the RequireJS files.
     * Default is false because there are not many browsers that can properly
     * process and give errors on code for ES5 strict mode,
     * and there is a lot of legacy code that will not work in strict mode.
     * @parameter expression="${requirejs.useStrictJs}" default=false
     */
    private boolean useStrictJs = false;
    
    /**
     * Specify build pragmas. If the source files contain comments like so:
     * >>excludeStart("fooExclude", pragmas.fooExclude);
     * >>excludeEnd("fooExclude");
     * Then the comments that start with //>> are the build pragmas.
     * excludeStart/excludeEnd and includeStart/includeEnd work, and the
     * the pragmas value to the includeStart or excludeStart lines
     * is evaluated to see if the code between the Start and End pragma
     * lines should be included or excluded.
     * @parameter
     */
    private Map<String, Boolean> pragmas;
    
    /**
     * Skip processing for pragmas.
     * @parameter expression="${requirejs.skipPragmas}" default=false
     */
    private boolean skipPragmas = false;
    
    /**
     * If skipModuleInsertion is false, then files that do not use require.def
     * to define modules will get a require.def() placeholder inserted for them.
     * Also, require.pause/resume calls will be inserted.
     * Set it to true to avoid this. This is useful if you are building code that
     * does not use require() in the built project or in the JS files, but you
     * still want to use the optimization tool from RequireJS to concatenate modules
     * together.
     * @parameter expression="${requirejs.skipModuleInsertion}" default=false
     */
    private boolean skipModuleInsertion = false;
    
    /**
     * Shorthand for specifying a single module to compile.
     * This will be overriden if you use the {@link #modules} parameter.
     * @parameter expression="${requirejs.module}" default="main"
     */
    private String module = "main";
    
    /**
     * List the modules that will be optimized. All their immediate and deep
     * dependencies will be included in the module's file when the build is
     * done. If that module or any of its dependencies includes i18n bundles,
     * only the root bundles will be included unless the locale: section is set above.
     * @parameter
     */
    private List<ModuleDefinition> modules;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
          
            Optimizer builder = new Optimizer();
            ErrorReporter reporter = new DefaultErrorReporter(getLog(), true);
            
            builder.build( optimizeSourceDir, createBuildProfile(), reporter );
            
        } catch (RuntimeException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new MojoExecutionException(exc.getMessage(), exc);
        }
    }
    
    public File getBaseUrl()
    {
        return optimizeSourceDir;
    }

    public File getDir()
    {
        return optimizeOutputDir;
    }

    public String getLocale()
    {
        return locale;
    }

    public String getOptimize()
    {
        return jsOptimizer;
    }

    public String getOptimizeCss()
    {
        return cssOptimizer;
    }

    public boolean isInlineText()
    {
        return inlineText;
    }

    public boolean isUseStrict()
    {
        return useStrictJs;
    }

    public boolean isSkipPragmas()
    {
        return skipPragmas;
    }
    
    public Map<String, Boolean> getPragmas() {
        return pragmas;
    }
    
    public List<String> getCssImportIgnore() {
        return cssExcludes;
    }

    public boolean isSkipModuleInsertion()
    {
        return skipModuleInsertion;
    }
    
    public List<ModuleDefinition> getModules() {
        if(modules == null) {
            modules = new ArrayList<ModuleDefinition>();
            modules.add( new ModuleDefinition(module));
        }
        return modules;
    }

    @JsonIgnore
    public Log getLog() {
        return super.getLog();
    }
    
    @SuppressWarnings( "rawtypes" )
    @JsonIgnore
    public Map getPluginContext() {
        return super.getPluginContext();
    }
    
    private File createBuildProfile() throws IOException {
        File profileFile = File.createTempFile( "profile", "js" );
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue( profileFile, this );
        return profileFile;
    }

    
}
