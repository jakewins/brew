package com.voltvoodoo.brew;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.DirectoryScanner;

public class FileSetChangeMonitor
{
    private DirectoryScanner scanner = new DirectoryScanner();
    
    private File baseDir;
    
    private Map<String, File> fileCache = new HashMap<String, File>();
    private Map<String, Long> fileTimestamps = new HashMap<String, Long>();
    
    public FileSetChangeMonitor(File baseDir, String includePattern) {
        this.baseDir = baseDir;
        
        scanner.setBasedir( baseDir );
        scanner.setIncludes(new String[] {  includePattern } );
        
        for(String path : getPaths()) {
            rememberLastModifiedFor( path );
        }
    }
    
    public List<String> getModifiedFilesSinceLastTimeIAsked() {
        List<String> modified = new ArrayList<String>();
        
        for(String path : getPaths()) {
            if(hasBeenChangedSinceLastTimeIChecked(path)) {
                modified.add( path );
            }

            rememberLastModifiedFor(path);
        }
        
        return modified;
    }
    
    private boolean hasSeen(String path) {
        return fileCache.containsKey( path );
    }
    
    private boolean hasBeenChangedSinceLastTimeIChecked(String path) {
        return !hasSeen(path) || fileTimestamps.get( path ) < getLastModifiedFor( path );
    }

    private void rememberLastModifiedFor(String path) {
        fileTimestamps.put( path, getLastModifiedFor(path) );
    }
    
    private long getLastModifiedFor(String relativePath) {
        return fileFrom(relativePath).lastModified();
    }
    
    private File fileFrom(String relativePath) {
        if(!fileCache.containsKey( relativePath )) {
            fileCache.put( relativePath, new File(baseDir, relativePath));
        }
        return fileCache.get( relativePath );
    }
    
    private String[] getPaths() {
        scanner.scan();
        return scanner.getIncludedFiles();
    }
}
