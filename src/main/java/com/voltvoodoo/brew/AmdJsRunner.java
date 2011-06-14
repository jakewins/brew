package com.voltvoodoo.brew;

import java.io.IOException;

public class AmdJsRunner extends JSRunner
{
    private static final String requireClasspathFilename = "require.js";
    private static final String requireRhinoClasspathFilename = "rhinoAdaptor.js";
    
    public AmdJsRunner() throws IOException
    {
        super();
        evalScript( requireClasspathFilename );
        evalScript( requireRhinoClasspathFilename );
    }

}
