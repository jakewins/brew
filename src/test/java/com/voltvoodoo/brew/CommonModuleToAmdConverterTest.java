package com.voltvoodoo.brew;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.util.logging.Logger;

import org.apache.tools.ant.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.mozilla.javascript.ErrorReporter;


public class CommonModuleToAmdConverterTest
{

    private static final Logger LOG = Logger.getLogger(CommonModuleToAmdConverterTest.class.getSimpleName());

    private static final String commonModuleFile =
        "var x = require(\"x\");" +
        "alert(x);";
    
    private static final String expectedAmdOutput = "define([\"require\", \"exports\", \"module\", \"x\"], function(require, exports, module) {\nvar x = require(\"x\");alert(x);\n});";

    /**
     * This does not work yet, as the --convert flag in r.js only
     * supports node.
     * @throws IOException
     */
    @Test
    @Ignore
    public void shouldConvertModuleFormat() throws IOException {
        Optimizer converter = new Optimizer();
        
        //File moduleFile = getCommonModuleFile();
        File moduleFile = new File("/tmp/asdf.asdf");
        File amdFile = File.createTempFile("amdModule", "js");
        ErrorReporter reporter = new LoggerErrorReporter(LOG, true);
        converter.convertCommonJsModulesToAmdModules(moduleFile, amdFile, reporter);

        String result = readFile(amdFile);

        assertEquals(expectedAmdOutput, result);
    }

    private String readFile(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            return IOUtil.toString(inputStream);
        } catch (RuntimeException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new RuntimeException("wrap: " + exc.getMessage(), exc);
        }
    }

    private File getCommonModuleFile() throws IOException {
        File moduleFile = File.createTempFile( "commonModule", "js" );
        FileWriter writer = new FileWriter(moduleFile);
        writer.write( commonModuleFile );
        writer.close();
        return moduleFile;
    }
    
}
