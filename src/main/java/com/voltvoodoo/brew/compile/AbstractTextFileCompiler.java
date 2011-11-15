package com.voltvoodoo.brew.compile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.util.IOUtil;

public abstract class AbstractTextFileCompiler implements Compiler {

    private String outputFileSuffix;

    public AbstractTextFileCompiler(String outputFileSuffix) {
        this.outputFileSuffix = outputFileSuffix;
    }
    
    public abstract String compile(String string);
    
    public void compile(File source, File target)
            throws CoffeeScriptCompileException, IOException {

        if (target.exists()) {
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

    public void compile(List<String> files, File sourceDir, File targetDir) {
        try {
            for (String path : files) {
                String newPath = path.substring(0, path.lastIndexOf('.'))
                        + outputFileSuffix;
                compile(new File(sourceDir, path), new File(targetDir, newPath));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
