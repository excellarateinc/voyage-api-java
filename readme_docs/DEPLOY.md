## Deploy
Deploying to the upper environments (QA -> UAT -> PROD) will be handled by the Continuous Integration (CI) environment. The app is expected to contain all necessary tasks for building, testing, and packaging within it's build framework for execution on the command line. 

## Table of Contents
* [Continuous Integration (CI)](#continuous-integration-ci)
* [Server Configuration](#server-configuration)
* [App Build & Deploy (Manual)](#app-build--test-manual)
* App Build & Test w/ Jenkins
* App Build & Deploy Docker
* [High Availability Support](#high-availability-support)

## Continuous Integration (CI)
[Jenkins](https://jenkins.io) continuous integration build manager will be used to configure jobs and triggers to faciliate the CI process. Reference your development team's WIKI for links to the Jenkins environment. 

### Quality Assurance (QA)
The QA environment is where the Quality Assurance team will exercise the new code in an effort to "sign off" on the feature. The QA team is typically signaled that a feature is ready for testing by updating a status within a work tracking system. Before the QA team will begin testing, they will look at the CI environment build logs for the latest Git commit comments. Creating well described Topic Branch names that include the work ticket number as well as well written commit message will communicate to the QA team what is included in the most recent QA build. 

__CI Job Name:__ QA Build, Test, & Deploy
* Pulls the latest source code from Git "master" long running branch
* Executes the "build" task to compile the source
* Executes the "test" task to run all unit, integration, and functional tests
* Tags the source code with the current version (1.1) + build (544). For example: 1.1.544
* Executes a deployment script that automates the complete deployment to the QA server(s)

### User Acceptance Testing (UAT)
The UAT environment is where stable code from QA is deployed so that the [Product Owner(s)](https://www.mountaingoatsoftware.com/agile/scrum/product-owner) (PO) exercise the new features. When the PO determines that the requested feature(s) meet the acceptance criteria, then the PO will update the work ticket and the feature will be left alone in the "master" branch for an eventual production release. 

__CI Job Name:__ UAT Build & Deploy
* Requests the source code tag to download and build
* Pulls the source code for the given tag from Git "master" long running branch
* Executes the "build" task to compile the source
* Executes a deployment script that automates the complete deployment to the UAT server(s)

### Produduction (PROD)
The PROD environment is where stable software is made available for live usage by the intended users. PROD is almost always very strictly guarded to ensure uptime of the app, data quality (no testing data), and restrict usage to authorized personnel (in case of sensitive data). 

Deploying apps to PROD are often times NOT automated processes unless proper controls are in place to prevent accidental deploys. In the case of this guide, all PROD deploys will be initiated manually by first creating a PROD distribution bundle and then manually executing the steps to properly deploy the software. 

__CI Job Name:__ PROD Build, & Package
* Requests the source code tag to download and build
* Pulls the source code for the given tag from Git "master" long running branch
* Executes the "build" task to compile the source
* Executes a deployment script that automates the complete deployment to the UAT server(s)

### Source Code Tagging
Source code tagging is used to mark a bundle of code that is ready for promotion into the upper environments. An ideal tagging strategy for all non/technical parties involved on a project is to tag a build once and then push that tag to the upper enviornments. Keeping track of a single tag/version is much more simple than having a tag for each environment (QA, UAT, PROD). For example, whenever a QA build completes successfully, a source code tag is applied in Git, like 1.1.544. When the QA team signs off on a tag/version of the app, then they can cite the tag/version that should be promoted to UAT without any confusion. When a Product Owner is testing in UAT, they can sign off on the final stable tag/version that should be promoted to Production. When creating the work order for pushing an app to production, the exact tag/version can be specified so that a precise point-in-time signed-off snapshot of the code is sent to production. 

__Rules__
* Tagging is _only_ applied after a successful build & test of the "QA Build, Test, & Deploy" job
* Tags have the format of _major-version.minor-version.build-number_ like 1.1.544
* Environments higher than the QA environment will request the tag name of the source to build and deploy
* Complete version number must be displayed in the footer of the app (1.1.544)

### Paired PROD Deploys
Deploying to production should always be done in pairs of two peers! Accidents will happen, but they can be mostly prevented if by having a peer watchfully checking deployment steps as they are performed. 

__Rules__ 
* Production deployments require 2 qualified system and/or software engineers
* Production deployment procedures _must_ be documented vertabim so that any system/software engineer can execute them
* Production deployment procedures _must_ document rollback procedures in the event of an error
* Before each action is performed it must be verbally confirmed by the other person(s)
* All parties involved in the production deploy _must_ manually test that the deploy released the correct version of the software
* All parties involved must verbally agree that the software has been successfully deployed
* In the event of an unresolveable error, the deployment team must contact a manager and an expert to assist with the resolution of the issue. 
* Once the deploy has been determined a success or failure, then an email must be sent to the entire project team with details (if necessary)

If at all possible, automate as much of the deployment process as possible to prevent human errors. 

:arrow_up: [Back to Top](#table-of-contents)


## Server Configuration

### Prerequisties
The following are required to be present on the server before building and hosting the Voyage API
* [Java JDK 1-8.0](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
  - Used for building, testing, and packaging the application
  - Used for the Apache Tomcat web application container
* [Apache Tomcat 8.0](http://tomcat.apache.org)
  - Only necessary if the server is going to host the web application
* [git](https://git-scm.com/downloads)
  - Used to download the latest source

Follow the instructions on the sites (linked above) as to the best methods for installing/configuring these prerequisites.

### Optional: HTTP Web Server
Apache Tomcat can act as both an HTTP web service and a Java web application container. Since Apache Tomcat doesn't provide much in the way of HTTP servers, it is often desireable to setup an HTTP Server that then proxies requests/responses to/from Apache Tomcat.

Any HTTP Server can be used, including
* [Apache HTTP Server](https://httpd.apache.org)
  - Use [mod_proxy_ajp](https://httpd.apache.org/docs/2.4/mod/mod_proxy_ajp.html) to create a proxy connection between Apache HTTP Server and Apache Tomcat
* [Microsoft IIS](https://www.iis.net)
  - Use the [Apache Tomcat ISAPI Connector](https://tomcat.apache.org/connectors-doc/webserver_howto/iis.html) to create a proxy channel between IIS and Apache Tomcat

Follow the instructions on the sites (linked above) as to the best methods for installing/configuring the HTTP server. 

:arrow_up: [Back to Top](#table-of-contents)

## App Build & Test (Manual)

### Prerequisties
The following are required to be present on the server before building Voyage API:
* [Java JDK 1-8.0](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [git](https://git-scm.com/downloads)

### Instructions
These instructions are the base tasks for manually building, testing, and deploying without the assistance of a Continuous Integration server such as Jenkins or Team Foundation Server (TFS). 

#### 1. Download the Source
Download the source code from Git into a local workspace
```
cd /my/workspace
git clone https://github.com/lssinc/voyage-api-java
```

You might be required to provide your username and password to authenticate before being allowed to download the source. If so, use the following GitHub format:
```
cd /my/workspace
git clone https://username@github.com/lssinc/voyage-api-java
```

#### 2. Build with Gradle Wrapper
Voyage API project uses [Gradle](https://gradle.org) as the build platform, which compiles the app, executes tests, and packages the binaries into a Web Archive (.WAR). Gradle is invoked by using the `gradlew` Gradle Wrapper script located within the root of the project. Gradle Wrapper is a OS specific script that looks at the build.gradle file to see which Gradle version is supported. Gradle Wrapper will do the work of detecting if the supported version of Gradle is installed, and if not, will download and install the correct version of Gradle before executing the Gradle task. 

The following Gradle tasks should be executed in the order defined below:

Clean the workspace from the previous build
```
./gradlew clean
```

Run the unit and integration tests embedded within the source code
```
./gradlew test
```

Run the CodeNarc static code analysis on the /src/main source code
```
./gradlew codenarcMain
```

Run the CodeNarc static code analysis on the /src/test source code
```
./gradlew codenarcTest
```

Package the compiled app into a Web Archive (WAR) file that is compatible with Java Application Servers like Apache Tomcat
```
./gradlew war
```

#### 3. Artifacts for deployment
Artifacts generated from `./gradlew war` are stored within `/voyage-api-java/build/libs`. There should only be 1 .war file named after the app name defined in build.grade. For example: `voyage-1.0.war`. 

The .war file generated from Gradle is compatible with J2EE Application Server containers like Apache Tomcat. The .war file will require configuration settings to be setup with the Java Application Server. See next step. 

#### 4. Apache Tomcat Setup > Override Parameters By Environment
The Voyage API WAR file has bundled into it a base set of parameters that are configured for a local development environment. When deploying to another environment, review the parameters and override as needed. 

> NOTE: At the very least, change the database username/password and the default security settings!

There are [a number of ways to override parameters](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) within the application. The method discussed in this section for overriding parameters is to create an application.yaml file that is securely stored on the host server. The only exception will be the JNDI DataSource configuraiton, which will be added to the server.xml to take advantage of Tomcat's database connection pooling. 

#### JDBC Configuration
Configure the JDBC connection pool within the server.xml. 

APACHE_TOMCAT_HOME/conf/server.xml
```
<GlobalNamingResources>
   <Resource
      name="jdbc/voyage"
      auth="Container"
      type="javax.sql.DataSource"
      username="voyage-user"
      password="password"
      driverClassName="com.mysql.jdbc.Driver"
      url="jdbc:mysql://localhost:3306/voyage"
      initialSize = "10"
      maxActive = "50"
      maxIdle = "30"
      maxWait = "1000"
      removeAbandoned = "true"
      removeAbandonedTimeout = "60"
      logAbandoned = "true"
      validationQuery = "SELECT 1"
      factory="org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory"
   />
</GlobalNamingResources>
```

Create a context configuration file specifically for the API web app so that other web applications within the Apache Tomcat instance are not able to see the following context parameters. Within the /conf/Catalina/localhost folder, create a file with the same name as the WAR file, but with a .xml extension. For example, if the WAR file is `voyage.war` in the /webapps folder, then create a `voyage.xml` context file in /conf/Catalina/localhost.   

APACHE_TOMCAT_HOME/conf/Catalina/localhost/voyage.xml
```
<Context>
    <ResourceLink
                name="jdbc/voyage"
                global="jdbc/voyage"
                type="javax.sql.DataSource" />
</Context>
```

Add the ResourceLink property to the context file to make the global JDBC connection pool created in the server.xml available to the web app associated with this context file.   

> Read more about configuring Apache Tomcat web-app specific contexts on the [Tomcat Documentation site](https://tomcat.apache.org/tomcat-8.0-doc/config/context.html). 

##### Override application.yaml properties
Override the properties defined within the `application.yaml` file that is bundled within the app to match the needs of the host server. Review the security configurations for the application within the [Security Configurations](SECURITY.md#security-configuration) section of the API [Security](SECURITY.md) documentation. 

> NOTE: Be sure to update the default passwords for:
> * Default users created in /src/main/resources/db.changelog/v1-0/user.yaml
> * Default password for the /src/main/resources/Keystore.jks file (requires recreating the Keystore
> * Default password for the 'asymmetric' public/private key
> * Default password fro the 'jwt' public/private key
> Thoroughly read through the [Security](SECURITY.md) documentation, specifically [Security Configurations](SECURITY.md#security-configuration)

1. Copy the contents of the `application.yaml` file from the app source code into a new file on the host server
    - Example Linux: `/etc/voyage-api-config/application.yaml`
    - Example Windows: `D:\voyage-api-config\application.yaml`
2. Secure the host server so that only Apache Tomcat (and Super Users) can access the `/etc/voyage-api-config/application.yaml` file
3. Override the properties in `/etc/voyage-api-config/application.yaml` to be specific to the host server
   - jpa.properties.hibernate.dialect - should match the database type being connected to. 
   - datasource.jndi-name - point to the JNDI name of the database resource defined in the Apache Tomcat /conf/server.xml (see prior section)
   - security.key-store.filename - change the filename to a locally defined public/private key keystore file
   - security.key-store.password - provide the keystore password used to to unlock the keystore file on the server
   - security.crypto.private-key-password - provide the password used to secure the private key for general RSA asymmetric encryption
   - security.jwt.private-key-password - provide the private key password that was used to generate the JWT public/private keys 
   - security.oauth2.resource.jwt.key-value - update the JWT public key for the resource server to decode the JWT token provided by the authentication server
   ```
   jpa:
     hibernate:
       ddl-auto: none
     properties:
       hibernate:
         dialect: org.hibernate.dialect.MySQL5InnoDBDialect
   
   datasource:
     jndi-name: jdbc/voyage
   
   security:
     # FOR PRODUCTION: The following MUST be overridden to ensure secrecy of the passwords for the keystore and private
     # See where you can override at https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
     key-store:
       filename: keystore.jks
       password: changeme
     
     crypto:
       private-key-name: asymmetric
       private-key-password: changeme

     # FOR PRODUCTION: The following MUST be overridden to ensure secrecy of the passwords for the keystore and private
     # See where you can override at https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
     jwt:
       private-key-name: jwt
       private-key-password: changeme

     # FOR PRODUCTION: The following MUST be overridden to ensure secrecy of the passwords for the keystore and private
     # See where you can override at https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
     oauth2:
       resource:
         id: voyage
         jwt:
           key-value: |
             -----BEGIN PUBLIC KEY-----
             MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyfgFBATB9oJjCUOVtwsr
             s8H6b8jiwl1gAuOVEHCgQbxuZPJ+YvcJad2xsEQLKbZBatbWF8gQIE0YNPW27niN
             CrH9QKYyFih5Ko2B8M5xbDr1L/AiQUsVwiqBmyj0krswacF9zRHwKHurFoxihhP0
             L6/NYrny8f5No8DNCC/abDYGFksqCE6gzLVB8moFGGcOk71l4CHJmlVrGS/Ec5Jj
             ktQuBza5RwiSb62PYiHGy5mLl8owdH0m0PCaXZBO2QzPbecFp2+W/5aXfIRchcjH
             Itcr8HKAqDO13XDo+xtqtVkFEn6hXXj5YESMkwukbWopDWOpfcGoQZStMhAEN7Xt
             zQIDAQAB
             -----END PUBLIC KEY-----
      ```
4. Update the Apache Tomcat web app specific context file to point to the location of the host server `application.yaml` file
    - This context file should have been created in the [JDBC Configuration](#jdbc-configuration) section.
    - Add an [<Environment>](https://tomcat.apache.org/tomcat-5.5-doc/config/context.html#Environment_Entries) property to override the [spring.config.location](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-application-property-files) value, which is the default location where Spring Framework will look for an application properties file. 
    - Example: APACHE_TOMCAT_HOME/conf/Catalina/localhost/voyage.xml
      ```
      <Context>
          <Environment 
          		name="spring.config.location" 
          		value="/etc/voyage-api-config/" 
          		type="java.lang.String" 
          		override="false"/>
          		
          <ResourceLink
                  name="jdbc/voyage"
                  global="jdbc/voyage"
                  type="javax.sql.DataSource" />
      </Context>
      ```
5. Restart Apache Tomcat to ensure the new settings are loaded
   - Look at the log files in /logs to verify that the database connected properly
   - Ensure that Apache Tomcat has proper privileges to READ the file

#### 4. Apache Tomcat 8.0 Deploy
To deploy the WAR file into the Apache Tomcat container, perform the following steps:
1. Obtain a voyage-1.0.war file from a trusted source. 
2. Make sure that the version number in the file or indicated from the WAR file source is the correct version for deployment. 
3. Upload the voyage-1.0.war file to a temporary location on the deployment environment
4. Copy the voyage-1.0.war file to a backup location for quick access in the event the application needs to be rolled back to a prior version
5. Zip the current /webapps/voyage app to a file called `voyage-war-bkp-20161215` where the last 8 digits are the current date in format YYYYMMDD
   - Zipping the /voyage folder is done in the event that artifacts were manually added or updated 
   - This folder will be deleted when the app is deployed and it would be undesireable to lose manually updated files, even though modifing files directly on the server outside of the normal deploy process is a bad practice. 
6. Copy the `voyage-war-bkp-DATE` file to a backup location for quick access in the event a rollback is required. 
7. Initiate a hot backup of the Voyage database
   - This is vitally important!!
   - The database will likely need to be rolled back to a backup snapshot date if you have to roll back the version of the application. 
8. Stop the Apache Tomcat service 
   - Windows: Use the "Apache Tomcat 8.0" window service
   - Linux: service tomcat8 stop
   - Alt: You can manually kill the Apache Tomcat process if the service is not shutting down the process. Usually this indicates something is wrong with the application or configuration of Apache Tomcat
9. Remove `/webapps/voyage.war` file and `/webapps/voyage` folder
   - Be sure you have completed step 5 & 6
11. Copy the `backups/voyage-1.0.war` file (from step 4) into the `/webapps` folder
   - `cp /usr/share/tomcat8/backups/voyage-1-5.war /usr/share/tomcat8/webapps/voyage.war`
   - NOTE: BE SURE TO COPY THE CORRECT VERSION OF THE WAR FILE!
   - NOTE: BE SURE TO RENAME THE FILE TO EXCLUDE THE VERSION NUMBER (ex: voyage-1.5.war -> voyage.war). Apache Tomcat will use the filename (not extention) as the context path for the URL of the app. `voyage.war` will be `http://myserver/voyage`
12. Start the Apache Tomcat service
   - Windows: Use the "Apache Tomcat 8.0" window service
   - Linux: service tomcat8 start

:arrow_up: [Back to Top](#table-of-contents)


## High Availability Support

### Overview
Voyage has been constructed to support high availability scaling at any level. The choices that have been made with regards to the technology framework and configuration are always bearing in mind that the app will need to be hosted on multiple servers in datacenter regions all over the world. 

### Mostly Stateless
Voyage API follow a common pattern where the app container Session is only available for the life of the HTTP Request. In order for the server to track a session between HTTP Requests is for the end-user to hold the SESSIONID (typically in a browser Cookie) and pass this value into the HTTP Request header. The API could require the consumer to pass a Cookie on the header, but ideally an API is providing its consumers with a single transactable action per HTTP Request. Web service endpoints that have a sequential dependency of execution that requires the consumer to keep track of a SESSIONID for proper processing makes for a complicated and confusing API. Additionally, using SESSIONIDs in the Cookie opens up additional hacking possibilities that are simply not necessary in most web services. 

While Voyage API implements the base components for an API platform, the Voyage architects strongly recommend finding every possible opportunity to remain as stateless as possible and avoid requiring the consumer to track a SESSIONID unnecessarily. 

> __OAuth2 Requires a Session__
> An exception to this Stateless claim is with the OAuth2 implementation. When authenticating using OAuth2 Implicit Authentication, the consumer invokes `/oauth/authorize` with a set of parameter. The `/oauth/authorize` endpoint is secured and requires the end-user to authenticate with a form-based login page hosted by the Voyage API server. Before Spring Security redirects the end-user to the form-based login page, the parameters included within the `/oauth/authorize` request are stored in a Servlet Session with a JSESSIONID Cookie passed back to the end-users web browser (during redirect). When the form-based login page is submitted to the Voyage API server, the end-user web browser is expected to pass the JSESSIONID within a Cookie to the server so that the OAuth authentication can resume the session using the parameters stored within the Session. 

### Load Balancer Support
Given that Voyage API is not a stateless API when OAuth2 is active, all requests for the /oauth/ path should be routed to the same server or employ sticky sessions. Ideally, all requests coming in on `/api/` would be evenly distributed to healthy servers as these web service endpoints _should_ be stateless. 

> NOTE: As the application evolves, consider revising this section to be accurrate for the current app. 

### Automatic Database Migration
When the Voyage API app is started on a new server in any sized application cluster, the app will connect to the database and determine if it needs to be migrated to a new version. If the database has already been upgraded, then nothing will occur with the database migration. If the database needs an upgrade, then the new app will execute the migration scripts that haven't yet been applied. The benefit of integrating migration scripts is that everything needed to upgrade an environment is completely bundled into the single WAR file package. 

> NOTE: When performing rolling deploys during high volumes of traffice, the database migration scripts might cause servers-yet-to-be-upgraded to throw exceptions on transactions in process. In large environments, consider segregating databases by region and employing tactics that would redirect all traffic to another region before upgrading an entire region at once.

### Database Support
The Voyage API does not provide guideance on how to implement a high availability database. The most that the Voyage API provides for high transaction database management is to encourage usage of the built-in Apache Tomcat database connection pool. Otherwise, the Voyage API will create connections to the datasources and will assume that the Database Administrators have the database infrastructure configured appropriately. 

### Amazon Elastic Beanstalk 
[Amazon AWS Elastic Beanstalk](https://aws.amazon.com/elasticbeanstalk/) (EB) is supported via the Docker implementation specificed within this document. Voyage API works very well within AWS EB environment, especially as a Docker container. We've tested and deployed Voyage API to test and production environments successfully. 

Reference the Docker configuraiton described in this document for instructions on how to bundle the app into a deployable Docker container. 

:arrow_up: [Back to Top](#table-of-contents)
