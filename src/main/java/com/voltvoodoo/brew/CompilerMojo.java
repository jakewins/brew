package com.voltvoodoo.brew;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import org.jcoffeescript.JCoffeeScriptCompileException;
import org.jcoffeescript.JCoffeeScriptCompiler;
import org.jcoffeescript.Option;

/**
 * 
 * @goal compile
 * @phase process-classes
 * 
 */
public class CompilerMojo extends AbstractMojo
{

    private static final String COFFEE_PATTERN = "**/*.coffee";

    private static final String HAML_PATTERN = "**/*.haml";

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
    private CommonModuleToAmdConverter moduleConverter;
    private JCoffeeScriptCompiler coffeeCompiler;

    public void execute() throws MojoExecutionException
    {
        try
        {

            hamlCompiler = new HamlCompiler();
            moduleConverter = new CommonModuleToAmdConverter();
            coffeeCompiler = new JCoffeeScriptCompiler(
                    new LinkedList<Option>() );

            for ( String relativePath : getCoffeeScriptsRelativePaths() )
            {
                try
                {
                    compileCoffeescriptFile( relativePath );
                }
                catch ( JCoffeeScriptCompileException e )
                {
                    getLog().error( "[" + relativePath + "]: " + e.getMessage() );
                    throw e;
                }
            }

            for ( String relativePath : getHamlRelativePaths() )
            {
                compileHamlFile( relativePath );
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
            JCoffeeScriptCompileException, IOException
    {
        FileSetChangeMonitor hamlFiles = new FileSetChangeMonitor(
                hamlSourceDir, HAML_PATTERN );
        FileSetChangeMonitor coffeeFiles = new FileSetChangeMonitor(
                coffeeSourceDir, COFFEE_PATTERN );

        try
        {
            while ( true )
            {
                Thread.sleep( ms );

                for ( String file : hamlFiles.getModifiedFilesSinceLastTimeIAsked() )
                {
                    compileHamlFile( file );
                    System.out.println("[" + file + "]: Compiled");
                }

                for ( String file : coffeeFiles.getModifiedFilesSinceLastTimeIAsked() )
                {
                    try
                    {
                        compileCoffeescriptFile( file );
                        System.out.println("[" + file + "]: Compiled");
                    }
                    catch ( JCoffeeScriptCompileException e )
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
            throws FileNotFoundException, JCoffeeScriptCompileException,
            IOException
    {
        File coffee = new File( coffeeSourceDir, relativePath );
        File js = new File( coffeeOutputDir, relativePath.substring( 0,
                relativePath.lastIndexOf( '.' ) ) + ".js" );

        if ( js.exists() )
        {
            js.delete();
        }
        js.getParentFile().mkdirs();
        js.createNewFile();

        FileInputStream in = new FileInputStream( coffee );
        FileOutputStream out = new FileOutputStream( js );

        String compiled = coffeeCompiler.compile( IOUtil.toString( in ) );
        IOUtil.copy( compiled, out );

        in.close();
        out.close();
    }
    
    private void convertFromCommonModuleToAMD( String relativePath ) throws IOException {
        String relativeAmdPath = relativePath.substring( 0, relativePath.lastIndexOf( '.' ) ) + amdModuleSuffix + ".js";
        
        File commonFile = new File( moduleConversionSourceDir, relativePath ).getAbsoluteFile();
        File amdFile = new File( moduleConversionOutputDir, relativeAmdPath ).getAbsoluteFile();
        
        String converted = moduleConverter.convert( commonFile.getAbsolutePath() );
        
        if(amdFile.exists()) {
            amdFile.delete();
        }

        amdFile.getParentFile().mkdirs();
        amdFile.createNewFile();
        
        FileOutputStream out = new FileOutputStream( amdFile );
        IOUtil.copy( converted, out );
        out.close();
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
