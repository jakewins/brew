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

    private static final String EXPEXTED_CONTENTS_ONE = "\"<html><head><title>Hello, world!</title></head><body><div id=\\\"anid\\\">\" + \nsomevar.attributename + \n\"</div></body></html>\"";
    private static final String EXPEXTED_CONTENTS_TWO = "\"<div id=\\\"someotherid\\\"><div id=\\\"hello\\\">\" + \nasd + \n\"</div></div>\"";

    private static File output = new File("target/classes");

    private static final File OUTPUT_ONE = new File(output, "one.js");
    private static final File OUTPUT_TWO = new File(output, "two.js");

    @Test
    public void outputFilesShouldExist() throws Exception {
        assertExists(OUTPUT_ONE);
        assertExists(OUTPUT_TWO);
    }

    @Test
    public void outputFilesShouldContainExpectedData() throws Exception {
        assertContains(OUTPUT_ONE, EXPEXTED_CONTENTS_ONE);
        assertContains(OUTPUT_TWO, EXPEXTED_CONTENTS_TWO);
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
