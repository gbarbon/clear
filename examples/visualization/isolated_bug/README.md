## CLEAR Framework
# Example: Isolated Bug


Specification
-------------
The LNT process consists of an  initial parallel construct (that will not have 
an impact on the bug) and two nested select constructs, each containing a 
parallel construct in one of the branch.

Property
--------
The safety property states that a SEND action should never be followed by an
EXEC action. This is written in MCL as follows: 
`([ true* . 'SEND' .  true* . 'EXEC' . true* ] false )`.

Figure Description
-----------------
The bug is only in a part of the model, and it is caused by the central parallel 
construct.
In the image this is highlighted by the neighbourhoods being concentrated in 
only one part of the model.
Notice that the sink is abstracting all the correct part.

Files
-----
- `ISOLATED.lnt` : LNT specification file, required by the *cexplts_gen.svl* script
- `ISOLATED.aut` : AUT model file, required by the *cexplts_gen.svl* script
- `prop.mcl` : MCL property, required by the *cexplts_gen.svl* script and 
               and the *CLEAR analyser*
- `cexplts_gen.svl` : counterexample LTS generation SVL script
- `bad_prop_ISOLATED.aut` : AUT counterexample LTS
- `bad_prop_ISOLATED_unclean.aut` : file required by the *CLEAR analyser*
- `bad_prop_ISOLATED_unclean.prd` : file required by the *CLEAR analyser*
- `bad_prop_ISOLATED_res-dump.autx` : counterexample LTS with neighbourhoods, 
    required by the *CLEAR visualizer* 
