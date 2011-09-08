package com.voltvoodoo.brew;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.mozilla.javascript.ErrorReporter;

public class Optimizer
{


    private static final String[] requirePlugins = {"i18n.js", "order.js", "text.js"};

    private static final String buildScriptClasspathFilename = "/r.js";

    private File buildScript;


    public Optimizer() throws IOException
    {
        buildScript = File.createTempFile( "build", "js" );

        copyFileFromClassPathToFilesystem( buildScriptClasspathFilename,
                buildScript );

        buildScript.deleteOnExit();
    }

    public void convertCommonJsModulesToAmdModules( File inputFile, File outputFile, ErrorReporter reporter ) throws IOException
    {

        String[] includes = new String[0];

        String[] args = new String[3];
        args[0] = "-v";
        args[1] = inputFile.getAbsolutePath();
        args[2] = outputFile.getAbsolutePath();

        Map<String, Object> globalVariables = new HashMap<String, Object>();

        RhinoRunner.exec(includes, buildScript.getAbsolutePath(), args, globalVariables, reporter);
    }

    public void build( File buildDir, boolean providePlugins, File buildProfile, ErrorReporter reporter ) throws IOException
    {
        
        // Move require deps into project dir temporarily.
        // These can not be loaded from classpath, because
        // require.js assumes these will be intermingled with the
        // raw source files we are compiling.
        putRequirePluginsIn(buildDir);
        
        String[] includes = new String[0];

        String[] args = new String[2];
        args[0] = "-o";
        args[1] = buildProfile.getAbsolutePath();

        Map<String, Object> globalVariables = new HashMap<String, Object>();
        
        RhinoRunner.exec(includes, buildScript.getAbsolutePath(), args, globalVariables, reporter);
    }

    private File copyFileFromClassPathToFilesystem( String classpathFilename,
            File outputFile ) throws IOException
    {
        FileOutputStream out = null;
        InputStream in = null;
        try
        {
            in = getClass().getResourceAsStream( classpathFilename );
            out = new FileOutputStream( outputFile );
            IOUtil.copy( in, out );
        }
        finally
        {
            IOUtil.close( in );
            IOUtil.close( out );
        }

        return outputFile;
    }

    private void putRequirePluginsIn(File buildDir) throws IOException {
        File outputFile;
        buildDir.mkdirs();
        for(String plugin : requirePlugins) {
            outputFile = new File(buildDir, plugin);
            copyFileFromClassPathToFilesystem( "/require/" + plugin, outputFile );
        }
    }


}
