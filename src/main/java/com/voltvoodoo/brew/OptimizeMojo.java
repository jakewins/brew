package com.voltvoodoo.brew;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.plexus.util.FileUtils;
import org.mozilla.javascript.ErrorReporter;

/**
 *
 * @goal optimize
 * @phase process-classes
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
     * Source tree is copied here, minified and aggregated.
     *
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File optimizeBuildDir;

    /**
     * Built modules are put here, moved from the build directory after
     * minification and aggregation. By default this is the same as the
     * output directory.
     *
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File optimizeOutputDir;

    /**
     * File name suffix for optimized modules. Set this to "false" to 
     * not use a suffix.
     *
     * @parameter expression="-min"
     */
    private String optimizedFileNameSuffix;

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
     * If includeRequire is specified on a module, then import require.js from
     * the specified resource.
     * @parameter expression="${requirejs.requireUrl}" default=false
     */
    private String requireUrl = "require.js";

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
     * If skipModuleInsertion is false, then files that do not use define()
     * to define modules will get a define() placeholder inserted for them
     * Also, require.pause/resume calls will be inserted.
     * Set it to true to avoid this. This is useful if you are building code that
     * does not use require() in the built project or in the JS files, but you
     * still want to use the optimization tool from RequireJS to concatenate modules
     * together.
     * @parameter default-value=false
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
    private List<Module> modules;

    /**
     * Set paths for modules. If relative paths, set relative to baseUrl above.
     * If a special value of "empty:" is used for the path value, then that
     * acts like mapping the path to an empty file. It allows the optimizer to
     * resolve the dependency to path, but then does not include it in the output.
     * Useful to map module names that are to resources on a CDN or other
     * http: URL when running in the browser and during an optimization that
     * file should be skipped because it has no dependencies.
     * @parameter
    */
    private Map<String, String> paths = Collections.emptyMap();

    /**
     * Configure CommonJS packages. See http://requirejs.org/docs/api.html#packages
     * @parameter
     */
    private Map<String, List<String>> packagePaths;
    
    /**
     * @parameter
     */
    private List<String> packages;
    
    /**
     * Set to true to only minify the aggregated module files, rather
     * than all files in the dependency tree.
     * @parameter
     */
    private boolean minifyOnlyAggregatedFiles = false;

    /**
     * If using UglifyJS for script optimization, these config options can be
     * used to pass configuration values to UglifyJS.
     * See https://github.com/mishoo/UglifyJS for the possible values.
     * @parameter
     */
    private Uglify uglify;

    /**
     * If using Closure Compiler for script optimization, these config options
     * can be used to configure Closure Compiler. See the documentation for
     * Closure compiler for more information.
     * @parameter
     */
    private Closure closure;

    /**
     * Same as "pragmas", but only applied once during the file save phase
     * of an optimization. "pragmas" are applied both during the dependency
     * mapping and file saving phases on an optimization. Some pragmas
     * should not be processed during the dependency mapping phase of an
     * operation, such as the pragma in the CoffeeScript loader plugin,
     * which wants the CoffeeScript compiler during the dependency mapping
     * phase, but once files are saved as plain JavaScript, the CoffeeScript
     * compiler is no longer needed. In that case, pragmasOnSave would be used
     * to exclude the compiler code during the save phase.
     * @parameter
     */
    private Map<String, Boolean> pragmasOnSave;

    /**
     * Allows trimming of code branches that use has.js-based feature detection:
     * https://github.com/phiggins42/has.js
     * The code branch trimming only happens if minification with UglifyJS or
     * Closure Compiler is done. For more information, see:
     * http://requirejs.org/docs/optimization.html#hasjs
     * @parameter
     */
    private Map<String, Boolean> has;

    /**
     * Similar to pragmasOnSave, but for has tests -- only applied during the
     * file save phase of optimization, where "has" is applied to both
     * dependency mapping and file save phases.
     * @parameter
     */
    private Map<String, Boolean> hasOnSave;

    /**
     * Allows namespacing requirejs, require and define calls to a new name.
     * This allows stronger assurances of getting a module space that will
     * not interfere with others using a define/require AMD-based module
     * system. The example below will rename define() calls to foo.define().
     * See http://requirejs.org/docs/faq-advanced.html#rename for a more
     * complete example.
     * @parameter
     */
    private String namespace;

    /**
     * If it is not a one file optimization, scan through all .js files in the
     * output directory for any plugin resource dependencies, and if the plugin
     * supports optimizing them as separate files, optimize them. Can be a
     * slower optimization. Only use if there are some plugins that use things
     * like XMLHttpRequest that do not work across domains, but the built code
     * will be placed on another domain.
     * @parameter default-value=false
     */
    private boolean optimizeAllPluginResources;

    /**
     * Wrap any build layer in a start and end text specified by wrap.
     * @parameter
     */
    private Wrap wrap;

    /**
     * Defines whether the default requirejs plugins text, order and i18n should
     * be copied to the working directory.
     * @parameter default-value=false
     */
    private boolean providePlugins = false;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            Optimizer builder = new Optimizer();
            ErrorReporter reporter = new DefaultErrorReporter(getLog(), true);
            builder.build( optimizeBuildDir, providePlugins, createBuildProfile(), reporter );
            moveModulesToOutputDir();

        } catch (RuntimeException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new MojoExecutionException(exc.getMessage(), exc);
        }
    }

    public String getBaseUrl()
    {
        return optimizeSourceDir.getAbsolutePath();
    }

    public File getDir()
    {
        return optimizeBuildDir;
    }

    public String getLocale()
    {
        return locale;
    }

    public String getRequireUrl()
    {
        return requireUrl;
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

    public List<Module> getModules() {
        if(modules == null) {
            modules = new ArrayList<Module>();
            modules.add( new Module(module));
        }
        return modules;
    }
    public Map<String, String> getPaths() {
        return paths;
    }

    public Map<String, List<String>> getPackagePaths() {
        return packagePaths;
    }

    public List<String> getPackages() {
        return packages;
    }

    public Uglify getUglify() {
        return uglify;
    }

    public Closure getClosure() {
        return closure;
    }
    public Map<String, Boolean> getPragmasOnSave() {
        return pragmasOnSave;
    }

    public Map<String, Boolean> getHas() {
        return has;
    }

    public Map<String, Boolean> getHasOnSave() {
        return hasOnSave;
    }

    public String getNamespace() {
        return namespace;
    }

    public boolean isOptimizeAllPluginResources() {
        return optimizeAllPluginResources;
    }

    public Wrap getWrap() {
        return wrap;
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

    private void moveModulesToOutputDir() throws IOException {
        if( ! optimizeBuildDir.equals(optimizeOutputDir) || getOptimizedFileNameSuffix().length() > 0) {
            File from, to;
            for(Module mod : getModules() ) {
                from = new File(optimizeBuildDir, mod.getName() + ".js");
                to = new File(optimizeOutputDir, mod.getName() + getOptimizedFileNameSuffix() + ".js");
                FileUtils.copyFile( from, to );
            }
        }
    }

    private String getOptimizedFileNameSuffix() {
        return optimizedFileNameSuffix.equalsIgnoreCase("false") ? "" : optimizedFileNameSuffix;
    }

    public boolean isMinifyOnlyAggregatedFiles() {
        return minifyOnlyAggregatedFiles;
    }
}
