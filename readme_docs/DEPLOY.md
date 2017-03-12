## Deploy
Deploying to the upper environments (QA -> UAT -> PROD) will be handled by the Continuous Integration (CI) environment. The app is expected to contain all necessary tasks for building, testing, and packaging within it's build framework for execution on the command line. 

## Table of Contents
* [Continuous Integration (CI)](#continuous-integration-ci)
* [Server Configuration](#server-configuration)
* [App Build & Test Manually](#app-build--test-manual)
* [Docker Support](#docker-support)

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

#### 4. Apacht Tomcat Setup > Override Parameters By Environment
The base Voyage API WAR file has bundled into it a base set of parameters that are configured for a local development environment. When deploying to another environment, review the parameters and override as needed. 

> NOTE: At the very least, change the database username/password and the default security settings!

There are [a number of ways to override parameters](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) within the application. The method discussed in this section for overriding parameters is to create an application.yaml file that is stored within /tomcat/conf and loaded as a System parameter on Tomcat startup. The only exception will be the JNDI DataSource configuraiton, which will be added to the server.xml to take advantage of Tomcat's database connection pooling. 

#### JDBC Configuration
Configure the JDBC connection pool within the server.xml and then make it available to the web apps in the context.xml. 

conf/server.xml
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

conf/context.xml
```
<Context>
    <ResourceLink
                name="jdbc/voyage"
                global="jdbc/voyage"
                type="javax.sql.DataSource" />
</Context>

```

##### Override application.yaml properties
Override the properties defined within the application.yaml file that is bundled within the app for the server environment. 

1. Create a new file `/conf/voyage-application.yaml`
2. Copy the entire contents of the application.yaml file from the application into the `/conf/voyage-application.yaml`
3. Override the properties in `/conf/voyage-application.yaml` to be environment specific
   - jpa.properties.hibernate.dialect - should match the database type being connected to. 
   - datasource.jndi-name - point to the JNDI name of the database resource defined in the Apache Tomcat /conf/server.xml (see prior section)
   - oauth2.resource.jwt.key-value - update the JWT public key for the resource server to decode the JWT token
   - jwt.key-store-filename - change the filename to a locally defined JWT keystore file
   - jwt.key-store-password - provide the keystore password used to to unlock the keystore file on the server
   - jwt.private-key-password - provide the private key password that was used to generate the private key within the environment
   ```
   jpa:
     hibernate:
       ddl-auto: none
     properties:
       hibernate:
         dialect: org.hibernate.dialect.MySQL5InnoDBDialect
   
   datasource:
     jndi-name: jdbc/voyage
   
   oauth2:
     resource:
       id: voyage
       jwt:
         key-value: |
           -----BEGIN PUBLIC KEY-----
           MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4REj5EYufU5OUnv9nij+
           j9irwALL3BwX9XxB7oDx3uj93P5h8rzTTdG/suaG3aBqRr5rqXpmTgwG1nf6FBfR
           8kiPp9R196cAT9g4OInsdNbux7oy5akUVsRo9pagEL0JB7eGbASi0z5A38QkpbjB
           MhIN0W9zwghsGNbf7N6wTVQN1NFHDW9zMdWUS9VBPeEGUZAMkKElGltHVhCdJGBf
           OdriLIO2KdimjO5q9Q9+qG2B96DFGNYvmuDlDLM11Q2fsre305CV1HN0vQulLhlr
           MJo9QdZt1g2d1VN5uIKid5dxWTAuUvJhgla6yCaTfYeV1OGq5C3DFV7tKDGNAIXL
           TQIDAQAB
           -----END PUBLIC KEY-----

   # FOR PRODUCTION: The following MUST be overridden to ensure secrecy of the passwords for the keystore and private
   # See where you can override at https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
   jwt:
     key-store-filename: /usr/share/tomcat7/conf/voyage-jwt.jks
     key-store-password: changeme
     private-key-name: jwt
     private-key-password: changeme   
   ```
4. Update CATALINA_OPTS OS environment variable with a reference to the `voyage-application.yaml` file
   - CATALINA_OPTS="-Denvfile=file:/usr/share/tomcat8/voyage-application.yaml"
   - This will work on Windows or Linux. Search online for your respective OS if you are unsure about how to apply an OS environment variable to the server. 

#### 4. Apache Tomcat 8.0 Deploy
The voyage API requires a database to be configured within Tomcat

Tomcat unpacks and hosts the .war file. It requires a resource for your database connection. That is done in the server.xml and context.xml configuration files. You will also need to provide a JDBC driver for your MySQL or MSSQL connection. 

##### Server.xml
```
	<Resource
			name="jdbc/voyageapi"
			auth="Container"
			type="javax.sql.DataSource"
			username="user"
			password="pass"
			driverClassName="com.mysql.jdbc.Driver"
			url="jdbc:mysql://database.url:3306/voyageapi"
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
```

##### Context.xml
```
	<ResourceLink
		name="jdbc/voyageapi"
		global="jdbc/voyageapi"
		type="javax.sql.DataSource" />
```

:arrow_up: [Back to Top](#table-of-contents)




## Docker Support
> __FINISH DOCUMENTATION__

* Dockerfile configuration
  - Why the config
  - AWS EB components
* AWS Elastic Beanstalk HOWTO
  - how to build the docker zip to upload to AWS
  - How to deploy in AWS EB console

:arrow_up: [Back to Top](#table-of-contents)

