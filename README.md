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

Configure packages that need to be mapped by JCROM in application.conf in the jcrom entry. Several packages can be listed there, the key used does not matter.

```
jcrom {
    package = todo.models
    otherPackage = todo.other.models
}
```

When using wisdom-modeshape, you must specify in application.conf the link to the modeshape configuration file for the required environnements:
```
modeshape {
    dev = modeshape-dev.json
    test = mdoeshape-test.json
    prod = modeshape.json
}
```
