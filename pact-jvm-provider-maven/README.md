Maven plugin to verify a provider [version 2.1.9+]
==================================================

Maven plugin for verifying pacts against a provider.

The Maven plugin provides a `verify` goal which will verify all configured pacts against your provider.

## To Use It

### 1. Add the pact-jvm-provider-maven plugin to your `build` section of your pom file.

```xml
<build>
    [...]
    <plugins>
      [...]
      <plugin>
        <groupId>au.com.dius</groupId>
        <artifactId>pact-jvm-provider-maven_2.11</artifactId>
        <version>2.1.9</version>
      </plugin>
      [...]
    </plugins>
    [...]
  </build>
```

### 2. Define the pacts between your consumers and providers

You define all the providers and consumers within the configuration element of the maven plugin.

```xml
<plugin>
    <groupId>au.com.dius</groupId>
    <artifactId>pact-jvm-provider-maven_2.11</artifactId>
    <version>2.1.9</version>
    <configuration>
      <serviceProviders>
        <!-- You can define as many as you need, but each must have a unique name -->
        <serviceProvider>
          <name>provider1</name>
          <!-- All the provider properties are optional, and have sensible defaults (shown below) -->
          <protocol>http</protocol>
          <host>localhost</host>
          <port>8080</port>
          <path>/</path>
          <consumers>
            <!-- Again, you can define as many consumers for each provider as you need, but each must have a unique name -->
            <consumer>
              <name>consumer1</name>
              <!--  currently supports a file path using pactFile or a URL using pactUrl -->
              <pactFile>path/to/provider1-consumer1-pact.json</pactFile>
            </consumer>
          </consumers>
        </serviceProvider>
      </serviceProviders>
    </configuration>
</plugin>
```

### 3. Execute `mvn au.com.dius:pact-jvm-provider-maven_2.11:verify`

You will have to have your provider running for this to pass.

## Verifying all pact files in a directory for a provider. [2.1.10+]

You can specify a directory that contains pact files, and the Pact plugin will scan for all pact files that match that
provider and define a consumer for each pact file in the directory. Consumer name is read from contents of pact file.

```xml
<plugin>
    <groupId>au.com.dius</groupId>
    <artifactId>pact-jvm-provider-maven_2.11</artifactId>
    <version>2.1.9</version>
    <configuration>
      <serviceProviders>
        <!-- You can define as many as you need, but each must have a unique name -->
        <serviceProvider>
          <name>provider1</name>
          <!-- All the provider properties are optional, and have sensible defaults (shown below) -->
          <protocol>http</protocol>
          <host>localhost</host>
          <port>8080</port>
          <path>/</path>
          <pactFileDirectory>path/to/pacts</pactFileDirectory>
        </serviceProvider>
      </serviceProviders>
    </configuration>
</plugin>
```

## Modifying the requests before they are sent

Sometimes you may need to add things to the requests that can't be persisted in a pact file. Examples of these would
be authentication tokens, which have a small life span. The Pact Maven plugin provides a request filter that can be
set to a Groovy script on the provider that will be called before the request is made. This script will receive the HttpRequest
bound to a variable named `request` prior to it being executed.

```xml
<plugin>
    <groupId>au.com.dius</groupId>
    <artifactId>pact-jvm-provider-maven_2.11</artifactId>
    <version>2.1.9</version>
    <configuration>
      <serviceProviders>
        <serviceProvider>
          <name>provider1</name>
          <requestFilter>
            // This is a Groovy script that adds an Authorization header to each request
            request.addHeader('Authorization', 'oauth-token eyJhbGciOiJSUzI1NiIsIm...')
          </requestFilter>
          <consumers>
            <consumer>
              <name>consumer1</name>
              <pactFile>path/to/provider1-consumer1-pact.json</pactFile>
            </consumer>
          </consumers>
        </serviceProvider>
      </serviceProviders>
    </configuration>
</plugin>
```

## Plugin Properties

The following plugin properties can be specified with `-Dproperty=value` on the command line or in the configuration section:

|Property|Description|
|--------|-----------|
|pact.showStacktrace|This turns on stacktrace printing for each request. It can help with diagnosing network errors|
|pact.filter.consumers|Comma seperated list of consumer names to verify|
|pact.filter.description|Only verify interactions whose description match the provided regular expression|
|pact.filter.providerState|Only verify interactions whose provider state match the provided regular expression. An empty string matches interactions that have no state|

Example in the configuration section:

```xml
<plugin>
    <groupId>au.com.dius</groupId>
    <artifactId>pact-jvm-provider-maven_2.11</artifactId>
    <version>2.1.9</version>
    <configuration>
      <serviceProviders>
        <serviceProvider>
          <name>provider1</name>
          <consumers>
            <consumer>
              <name>consumer1</name>
              <pactFile>path/to/provider1-consumer1-pact.json</pactFile>
            </consumer>
          </consumers>
        </serviceProvider>
      </serviceProviders>
      <configuration>
        <pact.showStacktrace>true</pact.showStacktrace>
      </configuration>
    </configuration>
</plugin>
```

## Provider States

For each provider you can specify a state change URL to use to switch the state of the provider. This URL will
receive the providerState description from the pact file before each interaction via a POST. The stateChangeUsesBody
controls if the state is passed in the request body or as a query parameter.

These values can be set at the provider level, or for a specific consumer. Consumer values take precedent if both are given.

```xml
<plugin>
    <groupId>au.com.dius</groupId>
    <artifactId>pact-jvm-provider-maven_2.11</artifactId>
    <version>2.1.9</version>
    <configuration>
      <serviceProviders>
        <serviceProvider>
          <name>provider1</name>
          <stateChangeUrl>http://localhost:8080/tasks/pactStateChange</stateChangeUrl>
          <stateChangeUsesBody>false</stateChangeUsesBody> <!-- defaults to true -->
          <consumers>
            <consumer>
              <name>consumer1</name>
              <pactFile>path/to/provider1-consumer1-pact.json</pactFile>
              <stateChangeUrl>http://localhost:8080/tasks/pactStateChangeForConsumer1</stateChangeUrl>
              <stateChangeUsesBody>false</stateChangeUsesBody> <!-- defaults to true -->
            </consumer>
          </consumers>
        </serviceProvider>
      </serviceProviders>
    </configuration>
</plugin>
```

If the `stateChangeUsesBody` is not specified, or is set to true, then the provider state description will be sent as
 JSON in the body of the request. If it is set to false, it will passed as a query parameter.

As for normal requests (see Modifying the requests before they are sent), a state change request can be modified before
it is sent. Set `stateChangeRequestFilter` to a Groovy script on the provider that will be called before the request is made.

## Filtering the interactions that are verified

You can filter the interactions that are run using three properties: `pact.filter.consumers`, `pact.filter.description` and `pact.filter.providerState`.
Adding `-Dpact.filter.consumers=consumer1,consumer2` to the command line or configuration section will only run the pact files for those
consumers (consumer1 and consumer2). Adding `-Dpact.filter.description=a request for payment.*` will only run those interactions
whose descriptions start with 'a request for payment'. `-Dpact.filter.providerState=.*payment` will match any interaction that
has a provider state that ends with payment, and `-Dpact.filter.providerState=` will match any interaction that does not have a
provider state.
