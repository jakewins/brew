package com.voltvoodoo.brew;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.util.FileUtils;

public class CommonModuleToAmdConverter extends AmdJsRunner
{
    
    private static final String compileScript = 
        "(function() {" +
          "var output = 'blaaa';" +
          "require(['commonJs'], function(commonJsConverter) {" +
             "output = commonJsConverter.convert(filename, filecontents, false);" +
          "});" +
          "return output;" +
        "})();";
    
    public CommonModuleToAmdConverter() throws IOException
    {
        super();
    }
    
    public String convert(String filePath) throws IOException {
        Map<String, Object> vars = new HashMap<String, Object>();
        
        FileReader reader = new FileReader(new File(filePath));
        
        vars.put("filename", filePath);
        vars.put("filecontents", FileUtils.readFully( reader ) );

        reader.close();
        
        return evalString( compileScript, "CommonModuleToAmdConverter", vars );
    }

}
