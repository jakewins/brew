# Brew

A maven plugin to brew coffeescript client-side apps. This includes 
the coffeescript cross-compiler, the require.js optimizer, and a 
HAML compiler.

### Usage

Brew is not yet in maven central, so for now you have to install
it manually:

    git clone git://github.com/jakewins/brew.git
    cd brew
    mvn clean install

Then, add the plugin to your pom:

    <plugins> 
      ..
      <plugin>
        <groupId>com.voltvoodoo</groupId>
        <artifactId>brew</artifactId>
        <version>1.0-SNAPSHOT</version>
        <executions>
          <execution>
            <goals>
              <goal>compile-coffeescript</goal>
              <goal>compile-haml</goal>
              <goal>optimize</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>

### Goal: compile-coffeescript

compile-coffeescript is by default attached to the "compile" phase.

compile-coffescript will by default look for coffeescript files (**/*.coffee) 
recursively in ${basedir}/src/main/coffeescript, and output the compiled js files 
into ${project.build.outputDirectory}.

You can change these two settings by configuration:
  
    <plugins> 
      ..
      <plugin>
        <groupId>com.voltvoodoo</groupId>
        <artifactId>brew</artifactId>
        <version>1.0-SNAPSHOT</version>
        <executions>
          <execution>
            <goals>
              <goal>compile-coffeescript</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <coffeeSourceDir>${basedir}/my/awesome/directory</coffeeSourceDir>
          <coffeeOutputDir>${basedir}/src/main/webapp/js</coffeeOutputDir>
        </configuration>
      </plugin>
    </plugins>
    
### Goal: optimize

The "optimize" goal is by default attached to the
"process-resources" maven phase. "optimize" goal will go through
all the require modules you have defined, aggregate and minify them,
and put the result in your output directory.

By default, optimize looks for a js file named "main.js" in 
${project.build.outputDirectory}, and puts the resulting main.min.js aggregate
file in ${project.build.outputDirectory}.

The source and output directories are can be set in your POM:

    <configuration>
      <optimizeSourceDir>${basedir}/my/awesome/directory</optimizeSourceDir>
      <optimizeOutputDir>${basedir}/somewhere</optimizeOutputDir>
    </configuration>

You can change the module name from the default ("main") by setting the
"module" config property. You can also provide a list of modules using the
"modules" config property, the modules setting overrides the module setting.

Minimum settings for the "modules" property would be:

    <configuration>
      ..
      <modules>
        <module>
          <name>myapp</name> <!-- Will lead to looking for a file named "myapp.js" -->
        </module>
        ..
      </modules>
    </configuration>

Requirejs-maven supports almost all settings that the requirejs optimization
tool does. Unfortunately, these are currently not documented properely.

Until they are, please refer to the java-doc in com.voltvoodoo.brew.OptimizeMojo