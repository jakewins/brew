package com.voltvoodoo.brew.compile;

import java.util.HashMap;
import java.util.Map;

import com.voltvoodoo.brew.JSRunner;

public class HamlCompiler extends AbstractTextFileCompiler
{
    private static final String hamlClasspathFilename = "/haml/haml.js";
    private static final String jsonClasspathFilename = "/haml/json2.js";
    
    private JSRunner js = new JSRunner();
    
    public HamlCompiler(boolean onlyCompileFilesThatHaveChanged)
    {
        super("js", onlyCompileFilesThatHaveChanged);
        
        js.evalScript(jsonClasspathFilename);
        js.evalScript(hamlClasspathFilename);
    }
    
    @Override
	public String compile( String haml )
    {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("hamlSource", haml);
        
        return addModuleDefinition(js.evalString("Haml.optimize(Haml.compile(hamlSource));","HamlCompiler", vars));        
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