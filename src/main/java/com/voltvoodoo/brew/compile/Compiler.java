package com.voltvoodoo.brew.compile;

import java.io.File;
import java.util.List;

public interface Compiler {

    public void compile(List<String> files, File sourceDir, File targetDir);

}
