package com.voltvoodoo.brew.compile;

import org.codehaus.plexus.util.IOUtil;

import java.io.*;
import java.util.List;

public class RawCopyCompiler implements Compiler
{
    public void compile(List<String> files, File sourceDir, File targetDir)
    {
        for (String path : files)
        {
            try
            {
                copyFile(path, sourceDir, targetDir);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public static void copyFile(String path, File sourceDir, File targetDir) throws IOException
    {
        FileOutputStream out = null;
        FileInputStream in = null;
        try
        {
            File source = new File(sourceDir, path);
            File target = new File(targetDir, path);
            File parentDir = target.getParentFile();
            parentDir.mkdirs();
            if (parentDir.exists())
            {
                in = new FileInputStream(source);
                out = new FileOutputStream(target);
                IOUtil.copy(in, out);
            }
        }
        finally
        {
            IOUtil.close(in);
            IOUtil.close(out);
        }
    }

}
