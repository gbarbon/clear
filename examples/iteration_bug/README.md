## CLEAR Framework
# Example: Iteration Bug


Specification
-------------
This LNT specification exhibits a looping process with a nondeterministic choice 
executed at each iteration of that loop. In one of the two branches of the 
choice, there is a parallel construct that allows one to obtain a LOSS action 
followed by a REC action.

Property
--------
The MCL property `([ true* . 'LOSS' . 'REC' . true* ] false)` states that 
a LOSS action must never be followed by a REC action.

Figure Description
------------------
The visualization of the erroneous part of the LTS corresponding to this LNT
specification looks like a 'flower'. Each petal corresponds to an iteration 
of the loop. There is a neighbourhood present at the beginning of each 
iteration, which represents a choice between reaching the incorrect behaviour, 
going to the sink state (both at the center of the picture), or continuing to 
the next petal. All the petals consist of neutral (black) transitions because 
the bug can still be avoided. There is a part of the LTS with red transitions, 
which is reached after executing an incorrect transition in one of the 
neighbourhoods. After nine iterations, executing at each iteration the first 
branch of the select construct, a final correct transition leads to the sink 
state and makes the whole specification definitely avoid the incorrect part 
of the behaviour.

Files
-----
- `ITERATION.lnt` : LNT specification file
- `ITERATION.aut` : AUT model file
- `prop.mcl` : MVL property
- `cexplts_gen.svl` : SVL script for the Counterexample LTS generation
- `bad_prop_ITERATION.aut` : AUT Counterexample LTS
- `bad_prop_ITERATION_unclean.aut` : used by the CLEAR analyser
- `bad_prop_ITERATION_unclean.prd` : used by the CLEAR analyser
- `bad_prop_ITERATION_res-dump.autx` : Counterexample LTS with neighbourhoods,
    input file for the CLEAR visualizer 

