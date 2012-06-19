package com.voltvoodoo.brew.compile;

import java.io.File;
import java.util.List;

public abstract class AbstractCompiler implements Compiler {

	private boolean onlyCompileFilesThatHaveChanged;

	public AbstractCompiler(boolean onlyCompileFilesThatHaveChanged) {
		this.onlyCompileFilesThatHaveChanged = onlyCompileFilesThatHaveChanged;
	}

	public abstract void compile(File source, File target) throws Exception;

	public void compile(List<String> files, File sourceDir, File targetDir) 
	{
        try {
            for (String path : files) 
            {
            	File source = new File(sourceDir, path), 
            	     target = new File(targetDir, path);
            	
            	if(onlyCompileFilesThatHaveChanged && sourceOlderThanTarget(source, target))
            	{
            		continue;
            	}
            	
                compile(source, target);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean sourceOlderThanTarget(File source, File target) {
		if(source.exists() && target.exists())
		{
			return source.lastModified() < target.lastModified();
		}
		return false;
	}

}
