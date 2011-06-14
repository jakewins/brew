package com.voltvoodoo.brew;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;


public class CommonModuleToAmdConverterTest
{

    private static final String commonModuleFile = 
        "var x = require(\"x\");" +
        "alert(x);";
    
    private static final String expectedAmdOutput = "define([\"require\", \"exports\", \"module\", \"x\"], function(require, exports, module) {\nvar x = require(\"x\");alert(x);\n});";
    
    @Test
    public void shouldConvertModuleFormat() throws IOException {
        CommonModuleToAmdConverter converter = new CommonModuleToAmdConverter();
        
        File moduleFile = getCommonModuleFile();
        
        String result = converter.convert(moduleFile.getAbsolutePath());
        
        assertEquals(expectedAmdOutput, result);
    }
    
    private File getCommonModuleFile() throws IOException {
        File moduleFile = File.createTempFile( "commonModule", "js" );
        FileWriter writer = new FileWriter(moduleFile);
        writer.write( commonModuleFile );
        writer.close();
        return moduleFile;
    }
    
}
