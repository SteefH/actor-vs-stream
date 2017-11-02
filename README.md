# Actor vs stream

This repository contains the source code for two Scala CLI programs.

From within sbt, run either `actor/run [base directory]` or `stream/run [base directory]`.
The application will scan all files in the base directory and subdirectories, and it will determine the frequency of all words it
can find in the files. It will then print the top ten of the most used words.
