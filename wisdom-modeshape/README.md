# Wisdom-JCR ModeShape integration

This module provides [ModeShape](http://modeshape.jboss.org) integration for the wisdom-jcr module.

## Maven dependencies

To enable this module, the following dependencies must be added to your pom.xml :

````
 <!-- wisdom-jcr -->
<dependency>
    <groupId>org.wisdom-framework.jcr</groupId>
    <artifactId>wisdom-modeshape</artifactId>
    <version>${wisdom.jcr.version}</version>
</dependency>
<dependency>
    <groupId>org.wisdom-framework.jcr</groupId>
    <artifactId>wisdom-jcr-core</artifactId>
    <version>${wisdom.jcr.version}</version>
</dependency>
<dependency>
    <groupId>joda-time</groupId>
    <artifactId>joda-time</artifactId>
    <version>2.6</version>
</dependency>
<dependency>
    <groupId>org.jcrom</groupId>
    <artifactId>jcrom</artifactId>
    <version>${jcrom.version}</version>
</dependency>
<!-- /wisdom-jcr -->
````

Since some ModeShape dependencies are not OSGi compliant, some libraries need to be added statically to the wisdom application runtime using the following configuration of the wisdom-maven-plugin :
(see [modeshape documentation](http://wisdom-framework.org/reference/0.8.0/index.html#_using_non_osgi_dependencies) for more information on non-OSGi dependencies.
````
<plugin>
    <groupId>org.wisdom-framework</groupId>
    <artifactId>wisdom-maven-plugin</artifactId>
    <version>0.9.0</version>
     <extensions>true</extensions>
    <configuration>
        <libraries>
            <includes>
                <!-- wisdom-jcr -->
                <include>org.modeshape:modeshape-jcr</include>
                <include>joda-time:joda-time</include>
                <include>org.jcrom:jcrom</include>
                <!-- /wisdom-jcr -->
            </includes>
            <excludeFromApplication>true</excludeFromApplication>
        </libraries>
        <disableDistributionPackaging>true</disableDistributionPackaging>
    </configuration>
</plugin>
````