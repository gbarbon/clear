## CLEAR Framework
# Example: Interleaving Bug


Specification
-------------
The LNT process consists of a parallel construct with two branches. In the 
first branch, there is a sequence of actions EXECi. In the second branch, 
there is a choice between a 'null' statement (correct termination) and a 
LOSS action. 

Property
--------
The safety property states that a LOSS action should never happen. 
This is written in MCL as follows: `( [ true* . 'LOSS' . true* ] false )`.

Figure Description
------------------
The initial state appears in orange (not in red as is usually colored the 
initial state) because that state is a neighbourhood. There is a sequence in 
which the bug does not appear (sequence of neutral black transitions leading 
to a correct green one). This happens when all EXECi actions execute and the 
second branch terminates correctly ('null'). At any state there is an incorrect transition (in red) corresponding to the execution of the LOSS action. 
This is typical of a bug which is interleaved with other actions, and the 
representation in our tool is similar to a 'comb'.

Files
-----
- `INTERLEAVING.lnt` : LNT specification file
- `INTERLEAVING.aut` : AUT model file
- `prop.mcl` : MVL property
- `cexplts_gen.svl` : SVL script for the Counterexample LTS generation
- `bad_prop_INTERLEAVING.aut` : AUT Counterexample LTS
- `bad_prop_INTERLEAVING_unclean.aut` : used by the CLEAR analyser
- `bad_prop_INTERLEAVING_unclean.prd` : used by the CLEAR analyser           
- `bad_prop_INTERLEAVING_res-dump.autx` : Counterexample LTS with neighbourhoods,
    input file for the CLEAR visualizer 

