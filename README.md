# CLEAR framework



Installation
============
-------------------------------------------------------


CLEAR analyser
--------------

The CLEAR analyser requires Apache Maven to be installed.
If Maven is already installed on your system, then download the content 
clear_analyser folder and run `mvn install`. 
Maven will generate two `.jar` file in the `target` directory.

Note that some features of the CLEAR Analyser require the installation of
the CADP toolbox (http://cadp.inria.fr/registration/).


CLEAR visualizer
----------------

The CLEAR visualiser can be installed in your machine or used directly online 
(click [here](https://gbarbon.github.io/clear/) to use the CLEAR visualizer online).
In the first case, these are the requirements to install it:

1. download the content of the clear_visualizer directory
2. install `npm` (necessary for angular, jquery, bootstrap, 3D graph force)
3. install an Apache Web Server: `sudo apt-get install apache2` in Linux systems

The CLEAR visualizer will then be available in through a web browser at `http://localhost/`.



Usage
======
-------------------------------------------------------

CLEAR Analyser
----------------

To discover neighbourhoods in the Counterexample LTS:

`java - jar clear_analyser-0.1-jar-with-dependencies.jar dir_path model_name prop_name -f`

- `dir_path` is the absolute path for the directory containing the test file
- `model_name` is the name of the model file (without the extension `.aut`)
- `prop_name` is the filename of the property (without the extension `.mcl`)
- `-f` is the option to load the `.prd` file containing the states matching

The CLEAR Analyser will display the discovered neighbourhoods and produce an 
`.autx` file to be used with the CLEAR visualizer.

CLEAR Visualiser
----------------

Open the CLEAR Visualizer in a web browser (online or from your localhost) 
and load an `.autx` file from the directory `\examples`. 


Examples usage
--------------

The directory `examples` contains some examples to evaluate our tool. 
You can :

   1. produce the Counterexample LTS  
   2. discover neighbourhoods in the Counterexample LTS with the CLEAR Analyser
   3. Visualize the Counterexample LTS and neighbourhoods with CLEAR Visualizer

You can directly jump to points 2 or 3, the necessary files are already 
available for all the three steps.

**NOTE:** *in order to generate the Counterexample LTS the CLEAR analyser exploits 
a feature of the CADP toolbox that is not yet publicly available, but will be 
released in a future CADP release. Anyway the neighbourhood detection and the
visualisation can anyway be performed, since the necessary files are all 
available in each example directory.*

License
=======
-------------------------------------------------------

Copyright 2018

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


