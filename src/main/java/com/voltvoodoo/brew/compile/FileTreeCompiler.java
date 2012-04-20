package com.voltvoodoo.brew.compile;

import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FileTreeCompiler {

    Map<String, Compiler> compilers = new TreeMap<String,Compiler>();
    
    public void addDefinition(String filePattern, Compiler compiler) {   
        compilers.put(filePattern, compiler);
    }

    public void compile(List<File> sourceDirs, File outputDir) 
    {
        for(String pattern : compilers.keySet() ) 
        {
            for(File sourceDir : sourceDirs) {
                compile(pattern, sourceDir, outputDir, compilers.get(pattern));
            }
        }
    }

    private void compile(String pattern, File sourceDir, File outputDir, Compiler compiler) {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(sourceDir);
        scanner.setIncludes(new String[]{ pattern });
        
        scanner.scan();
        compiler.compile(Arrays.asList(scanner.getIncludedFiles()), sourceDir, outputDir);
    }
}
