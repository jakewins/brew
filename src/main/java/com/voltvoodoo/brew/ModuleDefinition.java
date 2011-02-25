package com.voltvoodoo.brew;

import java.util.List;

public class ModuleDefinition
{
    /**
     * @parameter
     * @required
     */
    private String name;
    
    /**
     * @parameter
     */
    private List<String> includeModules;
    
    /**
     * @parameter
     */
    private List<String> excludeModules;
    
    /**
     * @parameter
     */
    private List<String> shallowExcludeModules;
    
    /**
     * @parameter
     */
    private OptimizeMojo override;
    

    public ModuleDefinition() {
        
    }
    
    public ModuleDefinition( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public List<String> getIncludeModules()
    {
        return includeModules;
    }

    public List<String> getExcludeModules()
    {
        return excludeModules;
    }

    public List<String> getShallowExcludeModules()
    {
        return shallowExcludeModules;
    }

    public OptimizeMojo getOverride()
    {
        return override;
    }
    
    
}
