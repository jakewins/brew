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
    private static final String ONE_IDENTIFIER = "one.coffee";
    private static final String TWO_FILE = "two.js";
    private static final String TWO_IDENTIFIER = "two.coffee";
    
    private File output = new File("target/classes");

    @Test
    public void outputFilesShouldExist() throws Exception {
        File oneFile = new File(output, ONE_FILE);
        File twoFile = new File(output, TWO_FILE);
        assertExists(oneFile);
        assertExists(twoFile);
    }
    
    @Test
    public void outputFilesShouldContainExpectedIdentifiers() throws Exception {

        File oneFile = new File(output, ONE_FILE);
        File twoFile = new File(output, TWO_FILE);
        
        assertContains(oneFile, ONE_IDENTIFIER);
        assertContains(twoFile, TWO_IDENTIFIER);
    }

    private static void assertExists(File file) throws Exception {
        assertThat( file.exists(), is( true ) );
    }

    private static void assertNotExists(File file) throws Exception {
        assertThat( file.exists(), is( false) );
    }
    
    private static void assertContains(File file, String substr) throws IOException {
        assertThat(FileUtils.readFileToString( file ), (containsString( substr )));
    }
    
    private static void assertNotContains(File file, String substr) throws IOException {
        assertThat(FileUtils.readFileToString( file ), not(containsString( substr )));
    }
}
