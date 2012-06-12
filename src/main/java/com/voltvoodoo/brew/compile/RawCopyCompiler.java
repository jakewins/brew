package com.voltvoodoo.brew.compile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.codehaus.plexus.util.IOUtil;

public class RawCopyCompiler implements Compiler {

    public void compile(List<String> files, File sourceDir, File targetDir) {
        FileOutputStream out = null;
        FileInputStream in = null;
        for(String path : files) {
            try
            {
                in = new FileInputStream( new File(sourceDir, path) );
                out = new FileOutputStream( new File(targetDir, path) );
                IOUtil.copy( in, out );
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                IOUtil.close( in );
                IOUtil.close( out );
            }
        }
    }

}
