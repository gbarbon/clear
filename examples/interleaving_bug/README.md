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
The initial state appears in orange (not in red as is usually coloured the 
initial state) because that state is a neighbourhood. There is a sequence in 
which the bug does not appear (sequence of neutral black transitions leading 
to a correct green one). This happens when all EXECi actions execute and the 
second branch terminates correctly ('null'). At any state there is an incorrect transition (in red) corresponding to the execution of the LOSS action. 
This is typical of a bug which is interleaved with other actions, and the 
representation in our tool is similar to a 'comb'.

Files
-----
- `INTERLEAVING.lnt` : LNT specification file, required by the *cexplts_gen.svl* script
- `INTERLEAVING.aut` : AUT model file, required by the *cexplts_gen.svl* script
- `prop.mcl` : MCL property, required by the *cexplts_gen.svl* script and 
               and the *CLEAR analyser*
- `cexplts_gen.svl` : counterexample LTS generation SVL script
- `bad_prop_INTERLEAVING.aut` : AUT counterexample LTS
- `bad_prop_INTERLEAVING_unclean.aut` : file required by the *CLEAR analyser*
- `bad_prop_INTERLEAVING_unclean.prd` : file required by the *CLEAR analyser*
- `bad_prop_INTERLEAVING_res-dump.autx` : counterexample LTS with neighbourhoods, 
    required by the *CLEAR visualizer* 
