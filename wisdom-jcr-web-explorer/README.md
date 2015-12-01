This module is a simple read-only JCR web explorer. 

It allows to navigate through the JCR graph provided by the JcrRepository and displays some informations about the current node :
- name and path
- mixins
- children
- properties

This explorer is provided as an extension of the wisdom-monitor bundle. To use it, simply add the following dependency to your wisdom application :
```
<dependency>
    <groupId>org.wisdom-framework.jcr</groupId>
    <artifactId>wisdom-jcr-web-explorer</artifactId>
    <version>${wisdom-jcr.version}<version>
</dependency>
```
