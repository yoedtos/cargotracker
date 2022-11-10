# Eclipse Cargo Tracker - Applied Domain-Driven Design Blueprints for Jakarta EE

![compile and build](https://github.com/hantsy/cargotracker/workflows/build/badge.svg)
![Integration Test with Arquillian Payara Managed Container](https://github.com/hantsy/cargotracker/workflows/it-with-arq-payara-managed/badge.svg)
![Integration Test with Arquillian WildFly Managed Container](https://github.com/hantsy/cargotracker/workflows/it-with-arq-wildfly-managed/badge.svg)

[![CircleCI](https://circleci.com/gh/hantsy/cargotracker.svg?style=svg)](https://circleci.com/gh/hantsy/cargotracker)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=hantsy_cargotracker&metric=alert_status)](https://sonarcloud.io/dashboard?id=hantsy_cargotracker)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=hantsy_cargotracker&metric=coverage)](https://sonarcloud.io/dashboard?id=hantsy_cargotracker)


> **This is a personal fork of [Eclipse EE4J CargoTracker](https://github.com/eclipse-ee4j/cargotracker), I also [a contributor of the CargoTracker project](https://github.com/eclipse-ee4j/cargotracker/graphs/contributors).**

> For the detailed introduction to the CargoTracker project, go to the original project website: https://eclipse-ee4j.github.io/cargotracker/.

There are some highlights when comparing to the upstream project.

* Utilize Docker to run a Postgres Database in both development and production to erase the risk brought by different enviroments.
* Fully support running the application on WildFly, and aslo add configuratoins to run testing codes against varied Arquillian adapters, such as Payara Managed and WildFly Managed conttainer adapters.
* The project *pom.xml* file includes multiple fine-grained Maven profille based configurations for varied Arquillian adapters, which is derived from [Jakarta EE 8 template project](https://github.com/hantsy/jakartaee8-starter-boilerplate).
* Add a plenty of testing codes to cover more use cases.
* Analyse code quality automaticially at every build defined in the Github actions workflow, eg. code coverage report via Jacoco, SonarCloud analysis report.

Follow the following steps to build and run the applicaiton on your local system.

## Prerequisites

* Java 11
* Apache Maven 3.8.1
* Git
* Docker
* A Jakarta EE 8 compatible application server
  * [Payara 5](https://www.payara.fish/downloads/) 
  * [WildFly 26](https://www.wildfly.org)

## Building

### Startup PostgresSQL Database

There is a *docker-compose.yaml* file available in the project root folder.

In your terminal, switch to the project root folder, and run the following command to start a Postgres instance in Docker container.

```bash
docker-compose up postgres
```

### Payara 5

Run the following command to run the application on Payara 5 using cargo maven plugin.

```bash
mvn clean package cargo:run
```
Open your browser, go to http://localhost:8080/cargo-tracker

### WildFly 

Run the following command to run the application on WildFly using the official WildFly maven plugin.

```bash
mvn clean package wildfly:run -Pwildfly
```
Open your browser, go to http://localhost:8080/cargo-tracker


## Testing

Cargo Tracker's testing is done using [JUnit](https://junit.org) and [Arquillian](http://arquillian.org/). There are several Maven profiles configured for running the testing codes against varied Arquillian Container adapters.

###  Payara Managed Adapter

Run Arquillian tests against Payara Managed Adaper.

```shell script
mvn clean verify -Parq-payara-managed -DskipTests=false
```

###  WildFly Managed Adapter

Run Arquillian tests against WildFly Managed Adaper.

```shell script
mvn clean verify -Parq-wildfly-managed -DskipTests=false
```

> I have remove other profiles in the latest commits, if you are interested in it, go to  [Jakarta EE 8 template project](https://github.com/hantsy/jakartaee8-starter-boilerplate), and follow the guides and expereince yourself.
