package com.voltvoodoo.brew.compile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import org.mozilla.javascript.JavaScriptException;

import com.voltvoodoo.brew.DefaultErrorReporter;
import com.voltvoodoo.brew.FileSetChangeMonitor;
import com.voltvoodoo.brew.Optimizer;

/**
 * 
 * @goal compile
 * @phase compile
 * 
 */
public class CompilerMojo extends AbstractMojo
{

    private static final String COFFEE_PATTERN = "**/*.coffee";

    private static final String HAML_PATTERN = "**/*.haml";

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
    private Optimizer moduleConverter;
    private CoffeeScriptCompiler coffeeCompiler;
    private RawCopyCompiler resourceCompiler;

    public void execute() throws MojoExecutionException
    {
        try
        {

            hamlCompiler = new HamlCompiler();
            moduleConverter = new Optimizer();
            coffeeCompiler = new CoffeeScriptCompiler(
                    new LinkedList<CoffeeScriptOption>() );
            resourceCompiler = new RawCopyCompiler();

            for ( String relativePath : getCoffeeScriptsRelativePaths() )
            {
                try
                {
                    compileCoffeescriptFile( relativePath );
                }
                catch ( CoffeeScriptCompileException e )
                {
                    getLog().error( "[" + relativePath + "]: " + e.getMessage() );
                    throw e;
                }
            }

            for ( String relativePath : getHamlRelativePaths() )
            {
                try {
                    compileHamlFile(relativePath);
                } catch(JavaScriptException e) {
                    getLog().error( "[" + relativePath + "]: " + e.getMessage() );
                    throw e;
                }
            }

            // TODO am guessing this is a copy/paste bug :)
            /*
            for ( String relativePath : getHamlRelativePaths() )
            {
                try {
                    compileHamlFile( relativePath );
                } catch(JavaScriptException e) {
                    getLog().error( "[" + relativePath + "]: " + e.getMessage() );
                    throw e;
                }
            }
            */

            String[] resourcePaths = getResourceRelativePaths();
            if (resourcePaths != null && resourcePaths.length > 0)
            {
                try
                {
                    List<String> files = Arrays.asList(resourcePaths);
                    resourceCompiler.compile(files, resourceSourceDir, resourceOutputDir);
                }
                catch (Exception e)
                {
                    getLog().error(e.getMessage(), e);
                    throw e;
                }
            }

            if ( watch )
            {
                System.out.println( "Watching for changes to coffeescript and haml files.." );
                checkForChangesEvery( 500 );
            }

        }
        catch ( RuntimeException exc )
        {
            throw exc;
        }
        catch ( Exception exc )
        {
            throw new MojoExecutionException( exc.getMessage(), exc );
        }
    }

    private void checkForChangesEvery( long ms ) throws FileNotFoundException,
            CoffeeScriptCompileException, IOException
    {
        FileSetChangeMonitor hamlFiles = new FileSetChangeMonitor(
                hamlSourceDir, HAML_PATTERN );
        FileSetChangeMonitor coffeeFiles = new FileSetChangeMonitor(
                coffeeSourceDir, COFFEE_PATTERN );
        FileSetChangeMonitor resourceFiles = new FileSetChangeMonitor(
                resourceSourceDir, ANY_PATTERN );

        try
        {
            while ( true )
            {
                Thread.sleep( ms );

                for ( String file : hamlFiles.getModifiedFilesSinceLastTimeIAsked() )
                {
                    try {
                        compileHamlFile( file);
                        System.out.println("[" + file + "]: Compiled");
                    } catch(Exception e) {
                        getLog().error( "[" + file + "]: " + e.getMessage() );
                    }
                }

                for ( String file : coffeeFiles.getModifiedFilesSinceLastTimeIAsked() )
                {
                    try
                    {
                        compileCoffeescriptFile( file );
                        System.out.println("[" + file + "]: Compiled");
                    }
                    catch ( Exception e )
                    {
                        getLog().error( "[" + file + "]: " + e.getMessage() );
                    }
                }

                for ( String file : resourceFiles.getModifiedFilesSinceLastTimeIAsked() )
                {
                    try
                    {
                        RawCopyCompiler.copyFile(file, resourceSourceDir, resourceOutputDir);
                    }
                    catch (IOException e)
                    {
                        getLog().error( "[" + file + "]: " + e.getMessage() );
                    }
                }
            }
        }
        catch ( InterruptedException e )
        {
            getLog().info( "Caught interrupt, quitting." );
        }
    }

    private void compileCoffeescriptFile( String relativePath )
            throws FileNotFoundException, CoffeeScriptCompileException,
            IOException
    {
        File coffee = new File( coffeeSourceDir, relativePath );
        File js = new File( coffeeOutputDir, relativePath.substring( 0,
                relativePath.lastIndexOf( '.' ) ) + ".js" );

        coffeeCompiler.compile( coffee, js );
    }

    private void convertFromCommonModuleToAMD( String relativePath ) throws IOException {
        String relativeAmdPath = relativePath.substring( 0, relativePath.lastIndexOf( '.' ) ) + amdModuleSuffix + ".js";

        File commonFile = new File( moduleConversionSourceDir, relativePath ).getAbsoluteFile();
        File amdFile = new File( moduleConversionOutputDir, relativeAmdPath ).getAbsoluteFile();
        moduleConverter.convertCommonJsModulesToAmdModules(commonFile, amdFile, new DefaultErrorReporter(getLog(), true));
    }

    private void compileHamlFile( String relativePath ) throws IOException
    {
        File coffee = new File( hamlSourceDir, relativePath ).getAbsoluteFile();
        File js = new File( hamlOutputDir, relativePath.substring( 0,
                relativePath.lastIndexOf( '.' ) ) + ".js" ).getAbsoluteFile();

        if ( js.exists() )
        {
            js.delete();
        }
        js.getParentFile().mkdirs();
        js.createNewFile();

        FileInputStream in = new FileInputStream( coffee );
        FileOutputStream out = new FileOutputStream( js );

        String compiled = hamlCompiler.compile( IOUtil.toString( in ) );
        IOUtil.copy( compiled, out );

        in.close();
        out.close();
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

    private String[] getResourceRelativePaths() throws MojoFailureException
    {
        return getRelativePaths( resourceSourceDir, ANY_PATTERN );
    }

    private String[] getRelativePaths( File baseDir, String pattern )
            throws MojoFailureException
    {
        if (baseDir == null || !baseDir.exists())
        {
            return new String[0];
        }
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( baseDir );
        scanner.setIncludes( new String[] { pattern } );
        scanner.scan();

        return scanner.getIncludedFiles();
    }

}
