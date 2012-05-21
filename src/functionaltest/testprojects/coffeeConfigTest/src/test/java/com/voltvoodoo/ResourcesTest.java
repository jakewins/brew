package com.voltvoodoo;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ResourcesTest  {

    private static final String ONE_FILE = "one.js";
    private static final String FUNCTION_SCOPE = "(function() {";
    
    private File output = new File("target/classes");
    
    @Test
    public void outputFileShouldExist() throws Exception {
        File oneFile = new File(output, ONE_FILE);
        assertExists(oneFile);
    }
    
    @Test
    public void outputFileShouldNotContainFunction() throws Exception {
        File oneFile = new File(output, ONE_FILE);
        assertNotContains(oneFile, FUNCTION_SCOPE);
    }

    private static void assertExists(File file) throws Exception {
        assertThat( file.exists(), is( true ) );
    }

    private static void assertNotExists(File file) throws Exception {
        assertThat( file.exists(), is( false ) );
    }
    
    private static void assertContains(File file, String substr) throws IOException {
        assertThat(FileUtils.readFileToString( file ), (containsString( substr )));
    }
    
    private static void assertNotContains(File file, String substr) throws IOException {
        assertThat(FileUtils.readFileToString( file ), not(containsString( substr )));
    }
}
