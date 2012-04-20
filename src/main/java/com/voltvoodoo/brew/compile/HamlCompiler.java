package com.voltvoodoo.brew.compile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.voltvoodoo.brew.JSRunner;
import org.codehaus.plexus.util.IOUtil;

public class HamlCompiler extends AbstractTextFileCompiler
{
    private static final String hamlClasspathFilename = "/haml/haml.js";
    private static final String jsonClasspathFilename = "/haml/json2.js";
    
    private JSRunner js = new JSRunner();
    
    public HamlCompiler()
    {
        super("js");
        
        
        js.evalScript(jsonClasspathFilename);
        js.evalScript(hamlClasspathFilename);
    }
    
    public String compile( String haml )
    {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("hamlSource", haml);
        
        return addModuleDefinition(js.evalString("Haml.optimize(Haml.compile(hamlSource));","HamlCompiler", vars));        
    }

    public void compile(java.io.File input, java.io.File output) throws IOException {
        if ( output.exists() )
        {
            output.delete();
        }
        output.getParentFile().mkdirs();
        output.createNewFile();

        FileInputStream in = new FileInputStream( input );
        FileOutputStream out = new FileOutputStream( output );

        String compiled = compile(IOUtil.toString(in));
        IOUtil.copy( compiled, out );

        in.close();
        out.close();
    }


    
    private String addModuleDefinition(String script) {
        return  "(function(define){\n"+
                  "define(function(){return function(vars){\n" +
        		    "with(vars||{}) {\n" +
                      "return " + script + "; \n"+
                    "}};\n"+
                  "})" +
                ";})(typeof define==\"function\"?\n"+
                    "define:\n"+
                    "function(factory){module.exports=factory.apply(this, deps.map(require));});\n";
    }
}