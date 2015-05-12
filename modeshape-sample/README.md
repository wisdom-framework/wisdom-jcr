# ModeShape sample

This projects demonstrate the use of wisdom-jcr with ModeShape. It is based on the TODO application used in [wisdom-jdbc](https://github.com/wisdom-framework/wisdom-jdbc/tree/master/openjpa-sample) and in [wisdom-orientdb](https://github.com/wisdom-framework/wisdom-orientdb/tree/master/wisdom-orientdb-sample)

The configuration declares two jcr repositories, one for the DEV mode and one for the PROD mode :
- The DEV repository configuration uses a light ModeShape/Infinspan configuration with no cache store, so that the data is kept only in memory and is not persisted upon application restart. 
- The PROD repository configuration uses an infinispan [Single File Store](http://infinispan.org/docs/7.1.x/user_guide/user_guide.html#_single_file_store) to persist data on disk


To run this sample in DEV mode :

    mvn wisdom:run

To run this sample in PROD mode :

    mvn wisdom:run -Dapplication.mode=PROD
