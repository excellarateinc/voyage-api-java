## Deploy
Deploying to the upper environments (QA -> UAT -> PROD) will be handled by the Continuous Integration (CI) environment. The app is expected to contain all necessary tasks for building, testing, and packaging within it's build framework for execution on the command line. 

## Table of Contents
* [Continuous Integration (CI)](#continuous-integration-ci)
* [Server Configuration](#server-configuration)
* [App Build & Test](#app-build--test)
* [Docker Support](#docker-support)
* [Ansible Automated Deployment](#ansible-automated-deployment)

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
> __FINISH DOCUMENTATION__

### Windows Server 2012

### Webserver
The Java API can be hosted with any of the following:

* IIS
* Apache Web Server
* (Stand-alone) Apache Tomcat
* MySQL or MSSQL Database


:arrow_up: [Back to Top](#table-of-contents)

## App Build & Test
> __FINISH DOCUMENTATION__

### Prerequisties
The following are the prequisties for building the application from the CI server.

* Java JKD 1-8.0 
* Gradle 3.3 -- Included in Gradle Wrapper
* Apache Tomcat 8.0

### Instructions

#### 1. Build with Gradle Wrapper
The Java API project uses Gradle to run unit and intergration tests, and then to build and package the API.
Graddle Wrapper requires a Graddle installation and will install one if none can be found. 
```
gradlew clean
```
```
gradlew test
```
```
gradlew codenarcMain
```
```
gradlew codenarcTest
```
```
gradlew war
```
## Notes:
* **clean**: wipes out old artifacts that may break new builds
* **test**: runs the unit and intergration tests 
* **codenarc**: framework of rules, analyzes groovey code against supplied rule set
* **codenarcMain**: supplies the rule set
* **codenarcTest**: Run static code analysis with rule set 
* **war**: package API for deployment into a .war file

  

#### 2. Artifacts for deployment
The artifacts that should be deployed will be contained in the .war file. This .war file can be dropped in Apache Tomcat's webapp folder to begin the deployment process.




#### 3. Apache Tomcat 8.0
Tomcat unpacks and hosts the .war file. It requires a resource for your database connection. That is done in the server.xml and context.xml configuration files. You will also need to provide a JDBC driver for your MySQL or MSSQL connection. 


## Server.xml
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

## Context.xml
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

## Ansible Automated Deployment
> __FINISH DOCUMENTATION__

* Ansible script to deploy to AWS EB
  - Discuss everything necessary to educate
  - steps to run the script, variables, etc

:arrow_up: [Back to Top](#table-of-contents)
