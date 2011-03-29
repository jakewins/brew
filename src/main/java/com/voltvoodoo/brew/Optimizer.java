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

    private static final String buildScriptClasspathFilename = "/buildScript.js";
    private static final String requireJsClasspathFilename = "/require.js";
    private static final String rhinoAdaptorClasspathFilename = "/rhinoAdaptor.js";
    
    private final String[] requirePlugins = {"i18n.js", "order.js", "text.js"};
    
    private File buildScript;
    private File requireJs;
    private File rhinoAdaptor;
    

    public Optimizer() throws IOException
    {
        buildScript = File.createTempFile( "build", "js" );
        requireJs = File.createTempFile( "require", "js" );
        rhinoAdaptor = File.createTempFile( "rhinoAdaptor", "js" );
        
        copyFileFromClassPathToFilesystem( buildScriptClasspathFilename,
                buildScript );
        copyFileFromClassPathToFilesystem(requireJsClasspathFilename,
                requireJs );
        copyFileFromClassPathToFilesystem( rhinoAdaptorClasspathFilename,
                rhinoAdaptor );
        
        buildScript.deleteOnExit();
        requireJs.deleteOnExit();
        rhinoAdaptor.deleteOnExit();
    }

    public void build( File projectDir, File outputDir, File buildProfile, ErrorReporter reporter ) throws IOException
    {
        
        // Move require deps into output dir temporarily.
        // These can not be loaded from classpath, because
        // require.js assumes these will be intermingled with the
        // raw source files we are compiling.
        putRequirePluginsIn(outputDir);
        
        String[] includes = new String[2];
        includes[0] = requireJs.getAbsolutePath();
        includes[1] = rhinoAdaptor.getAbsolutePath();
        
        String[] args = new String[2];
        args[0] = projectDir.getAbsolutePath();
        args[1] = buildProfile.getAbsolutePath();
        
        Map<String, Object> globalVariables = new HashMap<String, Object>();
        
        RhinoRunner.exec(includes, buildScript.getAbsolutePath(), args, globalVariables, reporter);
        
        
        removeRequirePluginsFrom(outputDir);
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
    
    private void putRequirePluginsIn(File folder) throws IOException {
        File pluginsFolder = new File(folder, "require");
        File outputFile;
        pluginsFolder.mkdirs();
        for(String plugin : requirePlugins) {
            outputFile = new File(pluginsFolder, plugin);
            copyFileFromClassPathToFilesystem( "/require/" + plugin, outputFile );
        }
    }
    
    private void removeRequirePluginsFrom(File folder) throws IOException {
        File pluginsFolder = new File(folder, "require");
        if(pluginsFolder.exists()) {
            FileUtils.deleteDirectory( pluginsFolder );
        }
    }

}
