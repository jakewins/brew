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
    private boolean includeRequire;

    /**
     * @parameter
     */
    private List<String> include;

    /**
     * @parameter
     */
    private List<String> exclude;

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

    public boolean getIncludeRequire()
    {
        return includeRequire;
    }

    public List<String> getInclude()
    {
        return include;
    }

    public List<String> getExclude()
    {
        return exclude;
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
