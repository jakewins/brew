package com.voltvoodoo.brew;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import org.jcoffeescript.JCoffeeScriptCompileException;
import org.jcoffeescript.JCoffeeScriptCompiler;
import org.jcoffeescript.Option;

/**
 * TODO: This should, of course, be a compiler plugin. But I needed this quickly,
 * and have never built a compiler plugin. Anyone interested is welcome to
 * convert this to a compiler plugin.
 * 
 * @goal compile-coffeescript
 * @phase compile
 * 
 */
public class CoffeeMojo extends AbstractMojo {

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
    
    private JCoffeeScriptCompiler compiler ;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            
            compiler = new JCoffeeScriptCompiler(new LinkedList<Option>());
            
            for(String relativePath : getCoffeeScriptsRelativePaths()) {
               compile(relativePath);
            }
            
        } catch (RuntimeException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new MojoExecutionException(exc.getMessage(), exc);
        }
    }
    
    public File getBaseUrl()
    {
        return coffeeSourceDir;
    }

    public File getDir()
    {
        return coffeeOutputDir;
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
    
    private void compile(String relativePath) throws FileNotFoundException, JCoffeeScriptCompileException, IOException {
        File coffee = new File(coffeeSourceDir, relativePath);
        File js = new File(coffeeOutputDir, relativePath.substring(0, relativePath.lastIndexOf('.')) + ".js");

        if(js.exists()) {
            js.delete();
        }
        js.getParentFile().mkdirs();
        js.createNewFile();
        
        FileInputStream in = new FileInputStream(coffee);
        FileOutputStream out = new FileOutputStream(js);
        
        String compiled = compiler.compile( IOUtil.toString(in) );
        IOUtil.copy( compiled, out );
        
        in.close();
        out.close();
    }
    
    private String[] getCoffeeScriptsRelativePaths() throws MojoFailureException {
      DirectoryScanner scanner = new DirectoryScanner();
      scanner.setBasedir(coffeeSourceDir);
      scanner.setIncludes(new String[] {"**/*.coffee"});
      scanner.scan();
      
      return scanner.getIncludedFiles();
  }
    
}
