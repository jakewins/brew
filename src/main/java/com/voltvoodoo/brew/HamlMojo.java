package com.voltvoodoo.brew;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;

/**
 * 
 * @goal compile-haml
 * @phase compile
 * 
 */
public class HamlMojo extends AbstractMojo {

    /**
     * @parameter expression="${basedir}/src/main/coffeescript"
     */
    private File hamlSourceDir;
    
    /**
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File hamlOutputDir;

    private HamlCompiler compiler;

    public void execute() throws MojoExecutionException {
        try {
            
            compiler = new HamlCompiler();
            
            for(String relativePath : getHamlRelativePaths()) {
               compile(relativePath);
            }
            
        } catch (RuntimeException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new MojoExecutionException(exc.getMessage(), exc);
        }
    }
    
    public void compile(String relativePath) throws IOException
    {
        File coffee = new File(hamlSourceDir, relativePath).getAbsoluteFile();
        File js = new File(hamlOutputDir, relativePath.substring(0, relativePath.lastIndexOf('.')) + ".js").getAbsoluteFile();

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
    
    private String[] getHamlRelativePaths() throws MojoFailureException {
      DirectoryScanner scanner = new DirectoryScanner();
      scanner.setBasedir(hamlSourceDir);
      scanner.setIncludes(new String[] {"**/*.haml"});
      scanner.scan();
      
      return scanner.getIncludedFiles();
  }

}
