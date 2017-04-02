## Overview
A foundational set of web services that implement industry standard guidelines, common best practices, and the experienced insights afforded to Lighthouse Software thru decades of enterprise business software development. 

If you are extending this API to build a new app, then replace this section with a detailed overview of the new app. Include as much or as little detail as necessary to convey to the developers what this project is about. Often times less is more. 

## Topics
* [Features](#features)
* [Security](readme_docs/SECURITY.md)
* [Development](readme_docs/DEVELOPMENT.md)
* [Development Recipes](readme_docs/DEVELOPMENT-RECIPES.md)
* [Testing](readme_docs/STANDARDS-TESTING.md)
* [Deploy](readme_docs/DEPLOY.md)

## Features

### Web Services
* __HTTP Compliant RESTful API__
  - Follows HTTP protocols for RESTful web services
  - Lightweight JSON requests and responses
  - See our [Web Service Standards](readme_docs/STANDARDS-WEB-SERVICES.md)
* __Public API Status Service__
  - Web serbice that provides general status of the API to the public
  - Helpful endpiont for automated monitoring
* __User Administration Services__
  - Full suite of user administration web services (list, get, create, update, delete)
  - Secured access through role based security
* __Account Management Services__
  - Users can update their account information themselves
  - Manage account settings
  - Password reset
* __API Documentation__
  - Complete documentation for web services consumers
  - Includes detailed descriptions and example to quickly interact with the API

### Security
* __[OWASP](https://www.owasp.org/index.php/Category:OWASP_Top_Ten_Project) Hacker Proof__
  - Tested nightly against OWASP common hacks (particularly the top 10)
  - Tested nightly using 3rd party penetration testing services to ensure entperprise grade security!
* __[OAuth2](https://oauth.net/2/) Authentication__
  - Bearer Token authentication configuraiton
  - SHA2 hash encrypted user password (when authenticating using the database)
  - Supports other authentication methods
* __Active Directory / LDAP Authentication__
  - Extends OAuth2 to support authentication with an AD/LDAP system
  - Supports Enterprise SSO environments using AD/LDAP
* __Role Based Authorization__
  - Custom role definitions to suit any situation
  - Supports granular security permissions 
  - Full suite of role administration web services (list, get, create, update, delete)
* __Forgot Username / Password Support__
  - Web services that allow users to reset their username and/or password
  - Validates a user via their email address
* __Auditing__
  - Complete enterprise access and data auditing to meet compliance requirements
  - HTTP Request / Response logging to track user activity (anonymous and authenticated users)
  - Database change logging to track manipulation of data over time (anonymous and authenticated users)

### Tech Stack
* __JSON RESTful Web Services__
  - JSON request/response interaction
  - Strict [REST implementation](readme_docs/STANDARDS-WEB-SERVICES.md)
  - [apiDoc](http://apidocjs.com) documentation generated from source code comments
* __Spring Boot__
  - Spring MVC / REST
  - Groovy
  - Spring Security
  - Hibernate
  - (auditing, logging, ...)
* __Database Neutral__
  - Capable of integrating with any major database vendor (SQL Server, Oracle, DB2, MySQL, etc)
  - Database interactions follow [SQL99](https://en.wikipedia.org/wiki/SQL:1999) standards with no vendor specific database features relied upon
  - Liquibase database migrations produce on-demand SQL specific to the integrated database
* __Integrated Test Suite__
  - Automated test coverage using Spock testing framework
  - Tests executed during every build to ensure high quality code coverage
* __Continuous Integration (CI)__
  - Jenkins CI jobs able to invoke Gradle and apiDoc commands to build, test, and package
  - Jenkins jobs included with with API source
  - Supports other CI environments like Team Foundation Server (TFS)

### Developers
* __Team Protocols__
  - Fast learning curve through clear documentation
  - Easy values, standards, best practices that most developers will agree to follow
* __Core Values__
  - Documented core values that we believe will resonate with most development teams
  - Unifies teams and promotes healthy communication
  - See our [Core Values](readme_docs/DEVELOPMENT.md#core-values) documentation
* __Coding Standards__ 
  - Industry accepted language coding standards
  - Best practices when developing within the code base
  - Standard enforced using static code analysis at build time (CodeNarc)
  - See our [Development Team Standards](#development-team-standards)
  
### System Administrators
* __Deploy Instructions__
  - Full instructions on how to properly build, test, and package the API app for deploy
  - Continuous Integration job templates for QA, UAT, and PROD
* __Docker Support__
  - Preconfigured Dockerfile for deployment within Amazon Web Services environment
  - Generate a Docker bundle for distribution using built-in tasks
  - Customize to fit any environment
* __Amazon Web Services (AWS) - Elastic Beanstalk__
  - Supports AWS Elastic Beanstalk using a Docker image
  - Run a build task to generate an AWS EB compatible .zip file
* __API Monitoring__
  - Configure automated web uptime monitoring to use the Status Web Service
* __DevOps Ready__
  - [Ansible](https://www.ansible.com) scripts for deploying the API Docker image to the Amazon Web Service (AWS) environment
  - Customize scripts to support any environment
