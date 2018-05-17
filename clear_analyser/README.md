## CLEAR framework

CLEAR analyser
==============


Installation
------------

The CLEAR analyser requires Apache Maven and Java 8.
To install the CLEAR analyser, download the content of the clear_analyser 
folder and run `mvn install`. 
Maven will generate two `.jar` file in the `target` directory.

Note that some features of the CLEAR Analyser require the installation of
the CADP toolbox (http://cadp.inria.fr/registration/).


Usage
-----

To discover neighbourhoods in the Counterexample LTS:

`java - jar clear_analyser-0.1-jar-with-dependencies.jar dir_path model_name prop_name -f`

- `dir_path` is the absolute path for the directory containing the test file
- `model_name` is the name of the model file (without the extension `.aut`)
- `prop_name` is the filename of the property (without the extension `.mcl`)
- `-f` is the option to load the `.prd` file containing the states matching

The CLEAR Analyser will display the discovered neighbourhoods and produce an 
`.autx` file to be used with the CLEAR visualizer.


**NOTE:** *in order to generate the Counterexample LTS the CLEAR analyser 
exploits some private features of the CADP toolbox, which will be made publicly available in a future CADP release.*

License
=======

Copyright 2018

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


