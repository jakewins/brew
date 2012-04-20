package com.voltvoodoo.brew.compile;

import com.voltvoodoo.brew.DefaultErrorReporter;
import com.voltvoodoo.brew.FileSetChangeMonitor;
import com.voltvoodoo.brew.Optimizer;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.mozilla.javascript.JavaScriptException;

import java.io.*;
import java.util.LinkedList;

/**
 * @goal compile
 * @phase compile
 * 
 */
public class CompilerMojo extends AbstractMojo
{

    private static final String COFFEE_PATTERN = "**/*.coffee";

    private static final String HAML_PATTERN = "**/*.haml";

    private static final String LESS_PATTERN = "**/*.less";

    private static final String ANY_PATTERN = "**/*";

    /**
     * @parameter expression="${basedir}/src/main/coffeescript"
     */
    private File hamlSourceDir;

    /**
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File hamlOutputDir;
    
    /**
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File moduleConversionSourceDir;

    /**
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File moduleConversionOutputDir;

    /**
     * Javascript source directory.
     * 
     * @parameter expression="${basedir}/src/main/coffeescript"
     */
    private File coffeeSourceDir;

    /**
     * Build modules are put here.
     * 
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File coffeeOutputDir;
    
    /**
     * Javascript source directory.
     *
     * @parameter expression="${basedir}/src/main/coffeescript"
     */
    private File lessSourceDir;

    /**
     * Build modules are put here.
     *
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File lessOutputDir;

    /**
     * Used only by the watch option. Files in this directory
     * are watched for changes and copied on the fly over to 
     * {@link #resourceOutputDir}.
     * @parameter expression="${basedir}/src/main/resources"
     */
    private File resourceSourceDir;
    
    /**
     * Output for watched resources.
     * 
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File resourceOutputDir;

    /**
     * Set to true to watch for changes to files and re-compile them on the fly.
     * 
     * @parameter expression="${brew.watch}"
     */
    private boolean watch = false;

    /**
     * Optional suffix to add before ".js" on files that
     * have been converted from commonjs modules to AMD modules.
     * 
     * Default is ".amd", resulting in files like "myfile.amd.js".
     * 
     * If you leave this empty, and keep module conversion input
     * and output directories the same, behaviour will be to
     * overwrite the commonjs module files with amd module files.
     * 
     * @parameter expression=".amd"
     */
    private String amdModuleSuffix;

    private HamlCompiler hamlCompiler;
    private LessCompiler lessCompiler;
    private Optimizer moduleConverter;
    private CoffeeScriptCompiler coffeeCompiler;

    public void execute() throws MojoExecutionException {
        try {
            lessCompiler = new LessCompiler();
            hamlCompiler = new HamlCompiler();
            moduleConverter = new Optimizer();
            coffeeCompiler = new CoffeeScriptCompiler(
                    new LinkedList<CoffeeScriptOption>());

            for (String relativePath : getLessRelativePaths()) {
                try {
                    compileLessFile(relativePath);
                } catch (CoffeeScriptCompileException e) {
                    getLog().error("[" + relativePath + "]: " + e.getMessage());
                    throw e;
                }
            }

            for (String relativePath : getCoffeeScriptsRelativePaths()) {
                try {
                    compileCoffeescriptFile(relativePath);
                } catch (CoffeeScriptCompileException e) {
                    getLog().error("[" + relativePath + "]: " + e.getMessage());
                    throw e;
                }
            }

            for (String relativePath : getHamlRelativePaths()) {
                try {
                    compileHamlFile(relativePath);
                } catch (JavaScriptException e) {
                    getLog().error("[" + relativePath + "]: " + e.getMessage());
                    throw e;
                }
            }

            if (watch) {
                System.out.println("Watching for changes to coffeescript and haml files..");
                checkForChangesEvery(500);
            }

        } catch (RuntimeException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new MojoExecutionException(exc.getMessage(), exc);
        }
    }

    private void checkForChangesEvery(long ms) throws FileNotFoundException,
            CoffeeScriptCompileException, IOException {
        FileSetChangeMonitor lessFiles = new FileSetChangeMonitor(
                lessSourceDir, LESS_PATTERN);
        FileSetChangeMonitor hamlFiles = new FileSetChangeMonitor(
                hamlSourceDir, HAML_PATTERN);
        FileSetChangeMonitor coffeeFiles = new FileSetChangeMonitor(
                coffeeSourceDir, COFFEE_PATTERN);
        FileSetChangeMonitor resourceFiles = new FileSetChangeMonitor(
                resourceSourceDir, ANY_PATTERN);

        try {
            while (true) {
                Thread.sleep(ms);

                for (String file : lessFiles.getModifiedFilesSinceLastTimeIAsked()) {
                    try {
                        compileLessFile(file);
                        System.out.println("[" + file + "]: Compiled");
                    } catch (Exception e) {
                        getLog().error("[" + file + "]: " + e.getMessage());
                    }
                }

                for (String file : hamlFiles.getModifiedFilesSinceLastTimeIAsked()) {
                    try {
                        compileHamlFile(file);
                        System.out.println("[" + file + "]: Compiled");
                    } catch (Exception e) {
                        getLog().error("[" + file + "]: " + e.getMessage());
                    }
                }

                for (String file : coffeeFiles.getModifiedFilesSinceLastTimeIAsked()) {
                    try {
                        compileCoffeescriptFile(file);
                        System.out.println("[" + file + "]: Compiled");
                    } catch (Exception e) {
                        getLog().error("[" + file + "]: " + e.getMessage());
                    }
                }

                for (String file : resourceFiles.getModifiedFilesSinceLastTimeIAsked()) {
                    try {
                        File source = new File(resourceSourceDir, file);
                        File target = new File(resourceOutputDir, file);
                        FileReader sourceReader = null;
                        FileWriter targetWriter = null;
                        try {
                            sourceReader = new FileReader(source);
                            targetWriter = new FileWriter(target);

                            IOUtils.copy(sourceReader, targetWriter);
                            System.out.println("[" + file + "]: Copied to output dir");
                        } finally {
                            if (sourceReader != null)
                                sourceReader.close();
                            if (targetWriter != null)
                                targetWriter.close();
                        }
                    } catch (Exception e) {
                        getLog().error("[" + file + "]: " + e.getMessage());
                    }
                }

            }
        } catch (InterruptedException e) {
            getLog().info("Caught interrupt, quitting.");
        }
    }

    private void compileCoffeescriptFile( String relativePath )
            throws CoffeeScriptCompileException, IOException
    {
        File coffee = new File( coffeeSourceDir, relativePath );
        File js = new File( coffeeOutputDir, relativePath.substring( 0,
                relativePath.lastIndexOf( '.' ) ) + ".js" );

        if(coffee.lastModified() != js.lastModified()){
            coffeeCompiler.compile( coffee, js );
        }
        js.setLastModified(coffee.lastModified());

    }

    private void compileLessFile( String relativePath )
            throws IOException, LessException {
        File less = new File( lessSourceDir, relativePath );
        File css = new File( lessOutputDir, relativePath.substring( 0,
                relativePath.lastIndexOf( '.' ) ) + ".css" );

        if(less.lastModified() != css.lastModified()){
            lessCompiler.compile(less, css);
        }
        css.setLastModified(less.lastModified());
    }

    private void convertFromCommonModuleToAMD( String relativePath ) throws IOException {
        String relativeAmdPath = relativePath.substring( 0, relativePath.lastIndexOf( '.' ) ) + amdModuleSuffix + ".js";

        File commonFile = new File( moduleConversionSourceDir, relativePath ).getAbsoluteFile();
        File amdFile = new File( moduleConversionOutputDir, relativeAmdPath ).getAbsoluteFile();
        moduleConverter.convertCommonJsModulesToAmdModules(commonFile, amdFile, new DefaultErrorReporter(getLog(), true));
    }

    private void compileHamlFile( String relativePath ) throws IOException
    {
        File haml = new File( hamlSourceDir, relativePath ).getAbsoluteFile();
        File html = new File( hamlOutputDir, relativePath.substring( 0,
                relativePath.lastIndexOf( '.' ) ) + ".js" ).getAbsoluteFile();

        if(haml.lastModified() != html.lastModified()){
            hamlCompiler.compile(haml, html);
        }
        html.setLastModified(haml.lastModified());
    }

    private String[] getHamlRelativePaths() throws MojoFailureException
    {
        return getRelativePaths( hamlSourceDir, HAML_PATTERN );
    }

    private String[] getCoffeeScriptsRelativePaths()
            throws MojoFailureException
    {
        return getRelativePaths( coffeeSourceDir, COFFEE_PATTERN );
    }

    private String[] getLessRelativePaths()
            throws MojoFailureException
    {
        return getRelativePaths( lessSourceDir, LESS_PATTERN );
    }

    private String[] getRelativePaths( File baseDir, String pattern )
            throws MojoFailureException
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( baseDir );
        scanner.setIncludes( new String[] { pattern } );
        scanner.scan();

        return scanner.getIncludedFiles();
    }

}
