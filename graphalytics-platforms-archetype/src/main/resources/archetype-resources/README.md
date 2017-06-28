#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${symbol_pound} Graphalytics ${platform-name} platform extension

${symbol_pound}${symbol_pound} Dependency
The following dependencies are required for this platform extension (in parentheses are the recommended versions):
* maven 3.1
* jvm 1.7.0
* graphalytics-core
* any C compiler (`gcc` 5.2.1)
* CMake (3.2.2)
* GNU Make (4.0)

${symbol_pound}${symbol_pound} Installation
Download [${platform-name}](https://--), unpack into any directory and compile/build using the instructions given by the authors.

${symbol_pound}${symbol_pound} Configuration
Edit `config/platform.properties` to change the following settings:

 - `${platform-acronym}.home`: Set to the root directory where ${platform-name} has been installed.
 - `${platform-acronym}.disable_mpi`: Set this flag if ${platform-name} has been compiled without MPI support (i.e., configured with `-no_mpi`)
 - `${platform-acronym}.num-threads`: Set the number of threads ${platform-name} should use.
 - `${platform-acronym}.command`: Set the command to run when launching ${platform-name}. The default value is "%s %s" where the first argument refers to the binary name and the second arguments refers to the binary arguments. For example, change the value to "mpirun -np 2 %s %s" to execute ${platform-name} using MPI on two nodes.


${symbol_pound}${symbol_pound} Run benchmark
