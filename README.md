# Polite Proxy

The importance of web crawlers grows with the ongoing expansion of the World Wide Web. Without web crawlers search engines or web archives would not exist. Efficient crawling, however, is not possible without following best practices and obeying politeness rules. Not all robots crawling the web obey these rules leading to the exhaustive usage of network and computing resources which can be interpreted as Denial of Service attacks leading to the blocking of these crawlers. Our solution tries to overcome these issues by both, enforcing politeness through working as a proxy, and additionally offering services to check if a URL does not violate the constraints defined by the Robots Exclusion Protocol. We show a solution, implemented through a proxy, capable of handling a variety of clients and scenarios, enforce politeness and enable monitoring and caching without significantly increasing latency.

## Getting started

* Clone the GIT repository or download the ZIP folder

  ```bash
  git clone https://github.com/PatrickRi/Zuul_proxy.git
  ```

* Install with maven

  Maven can be downloaded here: https://maven.apache.org/download.cgi

  ```bash
  mvn clean package
  ```

* Run the JAR-archive

  ```bash
  java -jar ./target/zuul_proxy-0.0.1-SNAPSHOT.jar
  ```
