package com.voltvoodoo.brew.compile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.codehaus.plexus.util.IOUtil;

public abstract class AbstractTextFileCompiler extends AbstractCompiler {

    private String outputFileSuffix;

    public AbstractTextFileCompiler(String outputFileSuffix, boolean onlyCompileFilesThatHaveChanged) {
    	super(onlyCompileFilesThatHaveChanged);
        this.outputFileSuffix = outputFileSuffix;
    }
    
    public abstract String compile(String string);
    
    @Override
	public void compile(File source, File target)
            throws CoffeeScriptCompileException, IOException 
    {
    	String targetPath = target.getAbsolutePath();
        target = new File(targetPath.substring(0, targetPath.lastIndexOf('.'))
                + "." + outputFileSuffix);
    	
        if (target.exists()) 
        {
            target.delete();
        }
        target.getParentFile().mkdirs();
        target.createNewFile();

        FileInputStream in = new FileInputStream(source);
        FileOutputStream out = new FileOutputStream(target);

        String compiled = compile(IOUtil.toString(in));
        IOUtil.copy(compiled, out);

        in.close();
        out.close();
    }
}
