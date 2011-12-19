package com.voltvoodoo.brew;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.IOUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

public class JSRunner
{

    private final ScriptableObject globalScope;

    public JSRunner()
    {
        Context context = Context.enter();
        context.setOptimizationLevel(-1); // Without this, Rhino hits a 64K bytecode limit and fails
        try {
            globalScope = context.initStandardObjects();

            String[] names = { "print", "load", "readFile", "warn", "getResourceAsStream" };
            globalScope.defineFunctionProperties(names, JSRunner.class, ScriptableObject.DONTENUM);

        } finally {
            Context.exit();
        }
    }

    public void evalScript(String filename) {
        if(!filename.startsWith( "/" )) {
            filename = "/" + filename;
        }
        InputStream inputStream = getClass().getResourceAsStream(filename);

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
                throw new Error(e);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public String evalString( String scriptString) throws IOException {
        return evalString(scriptString, "Anonymous script", new HashMap<String,Object>());
    }

    public String evalString( String scriptString, String sourceName, Map<String, Object> objectsToPutInScope )
    {
        Context context = Context.enter();
        try {
            Scriptable compileScope = context.newObject(globalScope);
            compileScope.setParentScope(globalScope);

            for(String name : objectsToPutInScope.keySet()) {
                compileScope.put( name, compileScope, objectsToPutInScope.get( name ));
            }

            return (String)context.evaluateString(
                    compileScope,
                    scriptString,
                    sourceName, 0, null);

        } finally {
            Context.exit();
        }
    }

    //
    // FUNCTIONS EXPOSED IN JS LAND
    //


    /**
     * Print the string values of its arguments.
     *
     * This method is defined as a JavaScript function. Note that its arguments
     * are of the "varargs" form, which allows it to handle an arbitrary number
     * of arguments supplied to the JavaScript function.
     *
     */
    public static void print(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        for (int i = 0; i < args.length; i++) {
            if (i > 0) System.out.print(" ");

            // Convert the arbitrary JavaScript value into a string form.
            String s = Context.toString(args[i]);
            System.out.print(s);
        }
        System.out.println();
    }

    public static void warn(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        String message = Context.toString( args[ 0 ] );
        int line = (int) Context.toNumber( args[ 1 ] );
        String source = Context.toString( args[ 2 ] );
        int column = (int) Context.toNumber( args[ 3 ] );
        cx.getErrorReporter().warning( message, null, line, source, column );
    }

    /**
     * This method is defined as a JavaScript function.
     */
    public String readFile(String path) {
        try {
            return IOUtil.toString(getResourceAsStream(path));
        } catch (RuntimeException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new RuntimeException("wrap: " + exc.getMessage(), exc);
        }
    }

    /**
     * Load and execute a set of JavaScript source files.
     *
     * This method is defined as a JavaScript function.
     *
     */
    public static void load(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        for (Object element : args) {
            String filename = Context.toString(element);

            InputStreamReader in = null;
            try {
                in = new InputStreamReader(getResourceAsStream( filename ));
            } catch( Exception e ) {
                //Context.reportError("Couldn't open file \"" + filename + "\".");
                throw new RuntimeException(e);
            }

            try {
                // Here we evalute the entire contents of the file as
                // a script. Text is printed only if the print() function
                // is called.
                cx.evaluateReader(thisObj, in, filename, 1, null);
            } catch (WrappedException we) {
                System.err.println(we.getWrappedException().toString());
                we.printStackTrace();
            } catch (EvaluatorException ee) {
                System.err.println("js: " + ee.getMessage());
            } catch (JavaScriptException jse) {
                System.err.println("js: " + jse.getMessage());
            } catch (IOException ioe) {
                System.err.println(ioe.toString());
            } finally {
                try {
                    in.close();
                } catch (IOException ioe) {
                    System.err.println(ioe.toString());
                }
            }
        }
    }

    public static InputStream getResourceAsStream(String path) {
        File file = new File(path);
        if(file.exists()) {
            try
            {
                return new FileInputStream(path);
            }
            catch ( FileNotFoundException e )
            {
                throw new RuntimeException(e);
            }
        } else {
            if(path.startsWith( "./" )) {
                path = path.substring( 2 );
            }

            return JSRunner.class.getClassLoader().getResourceAsStream( path );
        }
    }
}
