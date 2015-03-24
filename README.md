# Wisdom-JCR

This project integrates JCR repositories within Wisdom-Framework.

The Wisdom-JCR project relies on [JCROM](https://code.google.com/p/jcrom/) to map objects between Wisdom and the JCR repository.

## Supported repositories

The Wisdom-JCR project is compatible with any JCR implementation. It expects that a service provides a `javax.jcr.RepositoryFactory` to load the repository.
Currently, the wisdom-modeshape module provides a direct integration with modeshape repositories.

## Usage

Add the wisdom-jcr-core module to your project:
````
<dependency>
    <groupId>org.wisdom-framework</groupId>
    <artifactId>wisdom-jcr-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
````

And pick-up a module providing access to the repository implementation, for example wisdom-modeshape:
````
<dependency>
    <groupId>org.wisdom-framework</groupId>
    <artifactId>wisdom-modeshape</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
````

## Configuration

To use wisdom-jcr, you need to configure JCROM and the JCR repository used.

### JCROM configuration

  - ```packages``` Configure packages that need to be mapped by JCROM in application.conf in the jcrom entry. Several packages can be listed comma-separated there.
  - ```dynamic.instantiation``` flag to enable [dynamic instantiation](https://code.google.com/p/jcrom/wiki/DynamicInstantiation) for JCROM
  - ```clean.names``` flag to enable [automatic name cleaning](http://jcrom.googlecode.com/svn/branches/2.0.0/jcrom/apidocs/org/jcrom/Jcrom.html#Jcrom(boolean)) for JCROM
  - ```env.repository``` the name of the repository to use with JCROM for the given environment

```
jcrom {
    packages = todo.models,todo.other.models
    dynamic.instantiation = true
    clean.names = true
    dev.repository = sample-repository-dev
    test.repository = sample-repository-test
    prod.repository = sample-repository-prod
}
```

### JCR configuration

To use wisdom-jcr, a repository matching the name of the repository specified in the JCROM configuration must be available.

For each repository to load, you can specify a map of parameters to pass to the RepositoryFactory in the jcr configuration block. These parameters depends on the repository vendor. The following example is for the Modeshape repository factory provided with the wisdom-modeshape module:

```
jcr {
    sample-repository-dev {
        "org.modeshape.jcr.RepositoryName" = sample-repository-dev
        "org.modeshape.jcr.URL" = "modeshape.json"
    }
}
```
