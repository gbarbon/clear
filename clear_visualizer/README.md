## CLEAR framework

CLEAR visualizer
==============


Installation
------------

The CLEAR visualiser can be installed in your machine or used directly online 
([here](https://gbarbon.github.io/clear/)).
In the first case, these are the requirements to install it:

1. Download the content of the clear_visualizer directory.
2. Install `npm` (necessary for angular, jquery, bootstrap, 3D graph force).
3. Install an Apache Web Server: `sudo apt-get install apache2` in Linux 
systems.

The CLEAR visualizer will then be available in through a web browser at `http://localhost/`.


Usage
-----

Open the CLEAR Visualizer in a web browser (online or from your localhost) 
and load one of the `.autx` files available in the directory `\examples`. 
Two modes of use are possible:

#### Mode 1: Path Traversal ####

1. Click on *Choose File* to load the `.autx` file containing the Counterexample
LTS with neighbourhoods. 
    - optional: a counterexample trace (in `.aut` format) can be loaded and 
      highlighted on the LTS.
2. Choose the path initial state with a mouse click on a state or use the 
   button *LTS initial state* to choose a path from the initial state.
3. Traverse a path by choosing the next state with mouse click. Only states 
   that are successors of the current state can be chosen. Options:
    - use the button *Previous state* to go back of one step;
    - choose one of the previous states on the list on the left side of the 
      screen;
    - change the view on the LTS using mouse; if visual contact with the last 
      state on the path is lost, use the button *Re-center camera* to re-center 
      the view on the last state;
    - use the button *Explore LTS* to enter the LTS Exploration Mode;
    - use the button *Load Another LTS* to load another LTS.

#### Mode 2: LTS Exploration ####

1. (see step 1 of the Path Traversal Mode above)
2. Use mouse click to explore each state  on the graph. Options: 
    - use buttons *LTS initial state* or *Another initial state* to enter the 
      Path Traversal Mode;
    - use the button *Load Another LTS* to load another LTS.

#### Interface details ####

A legend to describe elements of the LTS is shown on the left side part of the 
screen. The name of the chosen file is shown on the right part of the screen.
In the Path Traversal Mode the list of the traversed state and transitions is 
also shown on the right part of the screen. Buttons are located on the bottom
side of the screen.


License
=======

Copyright 2018

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
