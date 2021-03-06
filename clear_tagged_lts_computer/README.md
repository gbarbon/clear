## CLEAR framework

CLEAR 'Tagged LTS Computation' Module
=====================================


Installation
------------

The CLEAR 'Tagged LTS Computation' module requires Apache Maven and Java 8.
To install the this module, download the content of the clear_tagged_lts 
folder and run `mvn install`. 
Maven will generate two `.jar` file in the `target` directory.

Note that some features of the CLEAR 'Tagged LTS Computation' module require the installation of
the CADP toolbox (http://cadp.inria.fr/registration/).


Usage
-----

** Counterexample LTS Generation **

To generate the counterexample LTS run the svl script contained in the chosen 
example folder:

`svl cexp_generator.svl model_name prop_name`

- `model_name` is the name of the model file
- `prop_name` is the filename of the property


** Tagged LTS and Neighbourhood Computation **

To discover neighbourhoods in the Counterexample LTS:

`java - jar clear_analyser-0.1-jar-with-dependencies.jar dir_path model_name prop_name -f`

- `dir_path` is the absolute path for the directory containing the test file
- `model_name` is the name of the model file (without the extension `.aut`)
- `prop_name` is the filename of the property (without the extension `.mcl`)
- `-f` is the option to load the `.prd` file containing the states matching

The CLEAR 'Tagged LTS Computation' module will detect all the neighbourhoods 
and produce an `.autx` file to be used with the CLEAR visualizer.
The `.autx` file represent the tagged LTS with all the neighbourhoods. 


License
=======

Copyright 2018

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


