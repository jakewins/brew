package com.voltvoodoo.brew;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class HamlCompiler
{
    private static final String hamlClasspathFilename = "/haml.js";
    private static final String jsonClasspathFilename = "/json2.js";

    private final Scriptable globalScope;
    
    public HamlCompiler() throws IOException
    {
        Context context = Context.enter();
        context.setOptimizationLevel(-1); // Without this, Rhino hits a 64K bytecode limit and fails
        try {
            globalScope = context.initStandardObjects();
        } finally {
            Context.exit();
        }
        
        evalScript(jsonClasspathFilename);
        evalScript(hamlClasspathFilename);
    }
    
    public void evalScript(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(filename);        
        
        try {
            try {
                Reader reader = new InputStreamReader(inputStream, "UTF-8");
                try {
                    Context context = Context.enter();
                    context.setOptimizationLevel(-1); // Without this, Rhino hits a 64K bytecode limit and fails
                    try {
                        context.evaluateReader(globalScope, reader, filename, 0, null);
                    } finally {
                        Context.exit();
                    }
                } finally {
                    reader.close();
                }
            } catch (UnsupportedEncodingException e) {
                throw new Error(e); // This should never happen
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new Error(e); // This should never happen
        }
    }

    public String compile( String haml ) throws IOException
    {
        Context context = Context.enter();
        try {
            Scriptable compileScope = context.newObject(globalScope);
            compileScope.setParentScope(globalScope);
            compileScope.put("hamlSource", compileScope, haml);
            return (String)context.evaluateString(
                    compileScope, 
                    "Haml.optimize(Haml.compile(hamlSource));",
                    "HamlCompiler", 0, null);
        } finally {
            Context.exit();
        }
    }

}