# prewave-task

Short Task for Prewave Application Process.
The task is to implement a backend service using Spring Boot with Kotlin to manage a tree data structure. The tree will
be represented using a set of edges, where each edge connects two nodes. The implementation should support
functionalities to add and delete edges, as well as retrieve the entire tree starting from a given node.

## Start Appication

### Start Local Postgres Database with Docker

Postgresql DB needs to be started with Docker, it is included with docker-compose.yml file.
If necessary you can update the [`dev/.local.env`](dev/.local.env) file with your own values (for db connection).

```shell
docker compose --env-file dev/.local.env up -d
```

### Running App

You can either run the application in Intellij IDEA with follwoing steps:

1. Make sure that the [EnvFile IntelliJ plugin](https://plugins.jetbrains.com/plugin/7861-envfile) is installed
2. Start the application by running the compound Run Configuration `Run Application`

Or you can run the application using the following command:

```shell
env $(grep -v '^#' dev/.local.env | xargs) ./gradlew bootRun
```

## Test Appication

### Locally

The REST-API can be tested locally using Swagger UI: http://localhost:8080/swagger-ui/index.html

### Automated Tests

Automated tests using Testcontainers so no local database is required.

Different use cases for creating and deleting Edges and loading the supply chain tree are tested with spring boot
feature tests in
in [SupplyChainFeatureTest.kt](src/test/kotlin/com/prewave/prewavetask/supplychain/SupplyChainFeatureTest.kt).  
It is an incomplete list of tests, but should cover the most important use cases.

## Database Access with jOOQ

Database access is implemented with [jOOQ](https://www.jooq.org/). The table and record classes are generated from the
Flyway migrations in `src/main/resources/db/migration` during the build (`jooqCodegen` runs before `compileKotlin`). No
running database is required for this step.

The generated sources live in `build/generated-src/jooq` and are not committed.