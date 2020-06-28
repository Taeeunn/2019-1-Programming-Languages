/* How to run */

$ antlr4 -no-listener -visitor Koltin.g4
$ javac Kotlin*.java
$ java Kotlin2Java input.kt [output.java]

Accept input and optinally output(if not specified, input.java is default output name) from file-path at command line
