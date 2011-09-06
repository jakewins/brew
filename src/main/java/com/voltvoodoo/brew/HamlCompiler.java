package com.voltvoodoo.brew;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HamlCompiler extends JSRunner
{
    private static final String hamlClasspathFilename = "/haml.js";
    private static final String jsonClasspathFilename = "/json2.js";
    
    public HamlCompiler() throws IOException
    {
        super();
        evalScript(jsonClasspathFilename);
        evalScript(hamlClasspathFilename);
    }
    
    public String compile( String haml ) throws IOException
    {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("hamlSource", haml);
        
        return addModuleDefinition(evalString("Haml.optimize(Haml.compile(hamlSource));","HamlCompiler", vars));        
    }
    
    private String addModuleDefinition(String script) {
        return  "(function(define){\n"+
                  "define(function(){return function(vars){\n" +
        		    "with(vars||{}) {\n" +
                      "return " + script + "; \n"+
                    "}};\n"+
                  "})(typeof define==\"function\"?\n"+
                    "define:\n"+
                    "function(factory){module.exports=factory.apply(this, deps.map(require));});\n";
    }
}