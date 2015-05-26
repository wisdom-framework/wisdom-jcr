# Wisdom-JCR

This project integrates JCR repositories within Wisdom-Framework.

## Introduction

### JCR

JCR is a Content Repository API for Java and is speficied in the [JSR 283](https://jcp.org/en/jsr/detail?id=283): Content Repository for JavaTM Technology API Version 2.0. See [Wikipedia](http://en.wikipedia.org/wiki/Content_repository_API_for_Java) for an overview of the JCR API.

### Implementations

Several JCR implementations exists. The most famous open source implementations are [Apache Jackrabbit](http://jackrabbit.apache.org/jcr/index.html) and [ModeShape](http://modeshape.jboss.org)

The Wisdom-JCR project is compatible with any JCR implementation. It expects that a service provides a [`javax.jcr.RepositoryFactory`](http://www.day.com/maven/jsr170/javadocs/jcr-2.0/javax/jcr/RepositoryFactory.html) to load the repository.

Currently, the [wisdom-modeshape](https://github.com/wisdom-framework/wisdom-jcr/tree/master/wisdom-modeshape) module provides an integration with ModeShape repositories.

### Object mapping

JCR repositories do not provide a mapping layer with Java entities. The Wisdom-JCR project relies on [JCROM](https://code.google.com/p/jcrom/) to map objects between Wisdom and the JCR repository.

## Usage

Add the wisdom-jcr-core module to your project:
````
<dependency>
    <groupId>org.wisdom-framework</groupId>
    <artifactId>wisdom-jcr-core</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
````

And pick-up a module providing access to the repository implementation, for example wisdom-modeshape :

````
<dependency>
    <groupId>org.wisdom-framework</groupId>
    <artifactId>wisdom-modeshape</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
````

The full list of Maven dependencies depends on the JCR implementation used. See [wisdom-modeshape](https://github.com/wisdom-framework/wisdom-jcr/tree/master/wisdom-modeshape) module documentation for the dependencies to add for the ModeShape implementation.


## Configuration

wisdom-jcr reads its configuration from the wisdom application configuration file.

Two sections are used in the configuration file

  - the **jcrom** section configures the object mapping layer
  - the **jcr** section configures which JCR repositories should be loaded

### JCROM configuration

JCROM configuration keys starts with **jcrom**. This part allow to configure the mapping between the JCR repository and the Java entities.

Basic JCROM options :

  - ```packages``` Configure packages that need to be mapped by JCROM. Several packages can be listed comma-separated there.
  - ```dynamic.instantiation``` flag to enable [dynamic instantiation](https://code.google.com/p/jcrom/wiki/DynamicInstantiation) for JCROM
  - ```clean.names``` flag to enable [automatic name cleaning](http://jcrom.googlecode.com/svn/branches/2.0.0/jcrom/apidocs/org/jcrom/Jcrom.html#Jcrom(boolean)) for JCROM 
  

Additionnal options :

  - ```create.path``` if true, automatically create missing parent nodes when saving an entity 

This is also the place where the link with the JCR repository used is configured :

  - ```"env".repository``` the name of the repository to use with JCROM for the given environment "env"

Full example :

```
jcrom {
    packages = todo.models,todo.other.models
    dynamic.instantiation = true
    clean.names = true
    create.path = true
    dev.repository = sample-repository-dev
    test.repository = sample-repository-test
    prod.repository = sample-repository-prod
}
```

### JCR configuration

The JCR repositories declared in the JCROM configuration must also be configured in the wisdom application configuration file, starting with a **jcr** key. Each jcr repository must be declared using a key matching the repository named referenced in the JCROM configuration.

For each repository to load, you can specify a map of parameters to pass to the [RepositoryFactory](http://www.day.com/maven/jsr170/javadocs/jcr-2.0/javax/jcr/RepositoryFactory.html) in the jcr configuration block. These parameters depends on the repository vendor. The following example is for the Modeshape repository factory provided with the wisdom-modeshape module :

```
jcr {
    sample-repository-dev {
        "org.modeshape.jcr.RepositoryName" = sample-repository-dev
        "org.modeshape.jcr.URL" = "modeshape.json"
    }
}
```

Checkout an [example](https://github.com/wisdom-framework/wisdom-jcr/blob/master/modeshape-sample/src/main/configuration/application.conf) of configuration in the [modeshape-sample](https://github.com/wisdom-framework/wisdom-jcr/tree/master/modeshape-sample) project.

### ModeShape configuration

This section is specific to ModeShape repositories.

The org.modeshape.jcr.URL declared in the configuration file references a json file used to configure the ModeShape repository.

See [ModeShape documentation](https://docs.jboss.org/author/display/MODE/ModeShape+in+Java+applications#ModeShapeinJavaapplications-ModeShaperepositoryconfigurationfiles) for more details about the modeshape configuration file.

> Note that the name of the repository declared in the jcr part of the wisdom configuration must match the name of the repository declared in the modeshape json configuration file

ModeShape configuration file might itself reference other configuration files such as [cnd](https://docs.jboss.org/author/display/MODE/Registering+custom+node+types) files defining the [node types](https://docs.jboss.org/author/display/MODE/Defining+custom+node+types) used in the repository.

To allow Modeshape to correctly load cnd files when running tests, the following configuration must be added to the surefire plugin in your pom:

````
<plugin>    
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <additionalClasspathElements>
            <additionalClasspathElement>${basedir}/target/wisdom</additionalClasspathElement>
        </additionalClasspathElements>
    </configuration>
</plugin>
````

See [#12](https://github.com/wisdom-framework/wisdom-jcr/issues/12) for more information about this issue.

## Sample

The [modeshape-sample](https://github.com/wisdom-framework/wisdom-jcr/tree/master/modeshape-sample) provides a simple wisdom application demonstrating the use of the wisdom-jcr module with a ModeShape repository.

## Links

### JCR

  - [Java Content Repository: The Best Of Both Worlds](http://java.dzone.com/articles/java-content-repository-best)
  - [JCR v2.0 Specification (HTML version)](http://www.day.com/specs/jcr/2.0/)
