package com.voltvoodoo.brew;

import java.util.List;

public class Module
{
    /**
     * @parameter
     * @required
     */
    private String name;
    
    /**
     * @parameter
     */
    private List<String> includes;
    
    /**
     * @parameter
     */
    private List<String> excludes;
    
    /**
     * @parameter
     */
    private List<String> excludeShallow;
    
    /**
     * @parameter
     */
    private OptimizeMojo override;
    

    public Module() {
        
    }
    
    public Module( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public List<String> getIncludes()
    {
        return includes;
    }

    public List<String> getExcludes()
    {
        return excludes;
    }

    public List<String> getExcludeShallow()
    {
        return excludeShallow;
    }

    public OptimizeMojo getOverride()
    {
        return override;
    }
    
    
}
