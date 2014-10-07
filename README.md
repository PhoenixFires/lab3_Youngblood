# Lab 3

Refer to the lab handout lab3.pdf for details about the assignment.  This file provides some information to help you get started with setting up your development environment for the labs.


## Scala Development Tools

We will use a slightly older version of [Scala](http://www.scala-lang.org/), namely 2.10.3. The provided build.sbt uses this dependency.

The TAs will support [Scala IDE](http://scala-ide.org/) for [Eclipse](http://www.eclipse.org/) for Scala development in this course.  You are welcome to use any development environment, but we may not be able answer questions in your particular environment.

### Eclipse Import

To import the Lab 3 project files into Eclipse, go to

    File > Import > Existing Projects into Workspace (under General)

and then select

    Select archive file:

and browse to the lab3.zip that you downloaded from Piazza.

Now you need to update the lib files (they will not import properly). Right click on any file in your lab3 project and go to 

	Build Path > Configure Build Path 
	
Go to the "Libraries" tab and delete the original .jar files. Then "Add external JARs" and navigate to the lib folder within your eclipse workspace folder for this project. Select the jar files and import them.

Repeat for the lab3-grader project (if applicable). 

### Scala Interpreter (instead of Eclipse)

From the command-line, you can start the scala interpreter using the command

    $ scala

and can import the functions in your lab in the following way

    scala> import Lab3._

Note that you will need to run the scala interpreter in the same directory as the compiled version (lab3/bin/).

In Scala IDE, you can start a Scala interpreter with the project files available by selecting

    Window > Show View > Scala Interpreter

 
### Command-Line Tools

While not required if you have installed Scala IDE, you may also want to install the command-line Scala compiler and tools.  If you would like to build the project on the command-line, you should install [sbt](http://www.scala-sbt.org/).  Scala and sbt is available in many OS-specific package managers.

For your convenience, an sbt script (build.sbt) is included.  You can issue the following commands to compile, run and test your code:

    $ sbt run
    
runs your project (after compiling).

    $ sbt clean

deletes the previous compilation.

It is often convenient to run sbt interactively

    $ sbt
    
and then run via

    > run

at the sbt prompt.

## ScalaTest

We provide some unit tests in Lab3Spec.scala to drive your implementation.  To run tests, right-click on the file and select

    Run As > ScalaTest - File
    
You can also run tests via

    $ sbt test
    
For running the autograder from the Windows command line, cd to your lab3 folder then type:

	sbt "program lab3-grader" run
	
This will run the autograder packaged in the original zip file. This autograder is not included in this repository.


## JavaScripty Interpreter

You can run your JavaScripty interpreter with a file (e.g., tests in testjsy/) in Eclipse by right-clicking on Lab3.scala. Initially you need to right-click Lab3.scala and select 

	Run As > Scala Application
	
This will generate an error, requiring a jsy file. Now you right-click Lab3.scala and set up a Run Configuration: 

    Run As > Run Configuration ...

Now, under "Arguments" -> "Program Arguments" add the full absolute path of your .jsy file in quotations:

	"C:\<...>\testjsy\lab2.jsy"

For quick experimentation, it is more convenient to use the Scala Interpreter window.

