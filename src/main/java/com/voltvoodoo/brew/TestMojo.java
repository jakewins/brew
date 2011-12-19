package com.voltvoodoo.brew;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;

import com.voltvoodoo.brew.compile.CoffeeScriptCompiler;
import com.voltvoodoo.brew.compile.FileTreeCompiler;
import com.voltvoodoo.brew.compile.HamlCompiler;
import com.voltvoodoo.brew.compile.RawCopyCompiler;


/**
 *
 * @goal test
 * @phase test
 *
 */
public class TestMojo extends AbstractMojo {

    /**
     * Test source directories
     *
     * @parameter
     */
    private List<File> testSourceDirs;

    /**
     * Coffeescript test files will be compiled into this
     * directory. JS test files will be copied here.
     *
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File testWorkDir;

    /**
     * File name patterns to execute as tests.
     * Default is "**\/*Test.*" and "**\/Test*.*"
     *
     * @parameter
     */
    private String [] includes;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if(includes == null) {
            includes = new String[]{"*/*Test.*", "*/Test.*"};
        }

        compile(includes, getTestSourceDirs(), testWorkDir);

        runTests(testWorkDir, includes);

    }

    private List<File> getTestSourceDirs() {
        return testSourceDirs == null ? new ArrayList<File>() : testSourceDirs;
    }

    private void compile(String [] includes, List<File> sourceDirs, File outputDir) {
        FileTreeCompiler compiler = new FileTreeCompiler();
        compiler.addDefinition("**/*.coffee", new CoffeeScriptCompiler());
        compiler.addDefinition("**/*.haml",   new HamlCompiler());
        compiler.addDefinition("**/*",        new RawCopyCompiler());

        compiler.compile(sourceDirs, outputDir);
    }

    private void runTests(File workDir, String [] includes) {
        DirectoryScanner scanner = new DirectoryScanner();

        if(!workDir.exists()) {
            workDir.mkdirs();
        }

        scanner.setBasedir(workDir);
        scanner.setIncludes(includes);

        scanner.scan();
        for(String path : scanner.getIncludedFiles()) {
            System.out.println(path);
        }
    }
}
