## CLEAR Framework
# Example: Allbad Bug

Specification
-------------
The LNT process consists of two consecutive parallel constructs.

Property
--------
The safety property states that a EXEC1 action should never be followed 
by a EXEC2 action. This is written in MCL as follows: 
`([ true* . 'EXEC1' .  true* . 'EXEC2' . true* ] false )`.


Figure Description
------------------
In this particular case the property is always false, since the sequence of 
actions described in the property is present in every execution. 
The model thus only describes incorrect behaviour, and shows only red 
transitions.
A single neighbourhood is present at the initial state. 
No sink is present, since there are no correct parts.

Files
-----
- `ALLBAD.lnt` : LNT specification file, required by the *cexplts_gen.svl* script
- `ALLBAD.aut` : AUT model file, required by the *cexplts_gen.svl* script
- `prop.mcl` : MCL property, required by the *cexplts_gen.svl* script and 
               and the *CLEAR analyser*
- `cexplts_gen.svl` : counterexample LTS generation SVL script
- `bad_prop_ALLBAD.aut` : AUT counterexample LTS
- `bad_prop_ALLBAD_unclean.aut` : file required by the *CLEAR analyser*
- `bad_prop_ALLBAD_unclean.prd` : file required by the *CLEAR analyser*
- `bad_prop_ALLBAD_res-dump.autx` : counterexample LTS with neighbourhoods, 
    required by the *CLEAR visualizer* 
