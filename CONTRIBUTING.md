# Contributing
## catan-sdk
Assuming you are using Intellij, Gradle -> catan -> Tasks -> runstuff -> buildSDK

## catan-client
### Building/Running
You need to build the SDK first <br>
Assuming you are using Intellij, Gradle -> catan -> Tasks -> runstuff -> runClient

## catan-server
### Building/Running
You need to build the SDK first <br>
Assuming you are using Intellij, Gradle -> catan -> Tasks -> runstuff -> runServer

### Propreties
In [hibernate.cfg.xml](catan-server%2Fsrc%2Fmain%2Fresources%2Fhibernate.cfg.xml), you want to tell the program where it can find, your postgers database


```xml
<?xml version = "1.0" encoding = "utf-8"?>
<!DOCTYPE hibernate-configuration SYSTEM
        "http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
    <session-factory>

        <property name="hibernate.dialect">
            org.hibernate.dialect.PostgreSQLDialect
        </property>

        <property name="hibernate.connection.driver_class">
            org.postgresql.Driver
        </property>

        <property name="hibernate.connection.url">
            jdbc:postgresql://localhost:5432/?currentSchema=catan <!-- replace it with your database settings-->
        </property>

        <property name="hibernate.connection.username">
            postgres  <!-- replace it with your database settings-->
        </property>
        <property name="hibernate.connection.password">
            asdasd <!-- replace it with your database settings-->
        </property>
        <property name="hibernate.default_schema">
            catan <!-- replace it with your database settings-->
        </property>
        <property name="hbm2ddl.auto">
            update
        </property>

    </session-factory>
</hibernate-configuration>


```
