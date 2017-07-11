#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${symbol_pound} Graphalytics ${platform-name} platform driver

${platform-name}... (TODO: provide a short description on ${platform-name}). To execute Graphalytics benchmark on ${platform-name}, follow the steps in the Graphalytics tutorial on [Running Benchmark](https://github.com/ldbc/ldbc_graphalytics/wiki/Manual%3A-Running-Benchmark) with the ${platform-name}-specific instructions listed below.

${symbol_pound}${symbol_pound}${symbol_pound} Obtain the platform driver
There are two possible ways to obtain the ${platform-name} platform driver:

 1. **Download the (prebuilt) [${platform-name} platform driver](http://graphalytics.site/dist/stable/) distribution from our website.

 2. **Build the platform drivers**: 
  - Download the source code from this repository.
  - Execute `mvn clean package` in the root directory (See details in [Software Build](https://github.com/ldbc/ldbc_graphalytics/wiki/Documentation:-Software-Build)).
  - Extract the distribution from  `graphalytics-{graphalytics-version}-${platform-acronym}-{platform-version}.tar.gz`.

${symbol_pound}${symbol_pound}${symbol_pound} Verify the necessary prerequisites
The softwares listed below are required by the ${platform-name} platform driver, which should be properly configured in the cluster environment....

${symbol_pound}${symbol_pound}${symbol_pound} Adjust the benchmark configurations
Adjust the ${platform-name} configurations in `config/platform.properties`...

