The XabslEditor is a graphical editor for the "Extensible Agent Behavior Specification Language" XABSL. It is implemented in Java and should run on every platform that supports Java (Windows, Linux, Mac).

Further informations can be found on the [NaoTH website](https://www.naoteamhumboldt.de/de/projects/xabsleditor/).

The XabslEditor is available under a [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).


# Compiling
XabslEditor uses Gradle to build. To compile the source and build the distribution jar,
simply run the following gradle command:

```bash
./gradlew clean build
```

This creates `dist` directory with the scripts for executing the XabslEditor. 

# Run XabslEditor
To execute the XabslEditor simply run the following command, if you not already compiled the project:
```bash
./gradlew run
```

Or, if you already have complied the project, you can run the script file in the `dist` directory:
```bash
./dist/xabsleditor
```

# IDE
All IDEs, which support Gradle, should work to compile and run the XabslEditor.

We use Netbeans for developing. You can download and install Netbeans (6.8 or higher), open this project with Netbeans and compile it. The result will be a script file and a lib-directory in the `dist` folder which will run without any further installation.

# 3rd party tools and libraries

 * XABSL (http://www.xabsl.de/)
 * RSyntaxTextArea (http://fifesoft.com/rsyntaxtextarea/)
 * AutoComplete (http://fifesoft.com/autocomplete/)
 * JUNG (http://jung.sourceforge.net/)
 * JRuby (http://jruby.org/)
 * Crystal Icons (http://everaldo.com/crystal/)

Their licenses are located in the 3RD_PARTY_LICENSES folder.

# Changelog

## 1.2
TODO

## 1.1
TODO

## 1.0-rc1

released at 2010-01-19
https://launchpad.net/xabsleditor/+milestone/1.0-rc1

- initial release
