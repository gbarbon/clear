## CLEAR Framework
# Example: Unique Choice Bug


Specification
-------------
The LNT process consists of three main parts:an  initialization part, a central 
computation part and a final session closing part. Each part is composed of a 
select construct, which contains parallel constructs in each branches.

Property
--------
The safety property states that a RECV action should never be followed by a 
SEND action. This is written in MCL as follows: 
`([ true* . 'RECV' . true* . 'SEND' . true* ] false )`.

Figure Description
-----------------
The figure shows that there is only one neighbourhood in the model.
This neighbourhood corresponds to the select in the central computation part of 
the spec. 
One branch of the select construct is always correct, while the other one is 
always incorrect.
The bug can thus be reached from a single neighbourhood, which represents a 
mandatory and unique choice to obtain the bug.

Files
-----
- `UNIQUECHOICE.lnt` : LNT specification file, required by the *cexplts_gen.svl* script
- `UNIQUECHOICE.aut` : AUT model file, required by the *cexplts_gen.svl* script
- `prop.mcl` : MCL property, required by the *cexplts_gen.svl* script and 
               and the *CLEAR analyser*
- `cexplts_gen.svl` : counterexample LTS generation SVL script
- `bad_prop_UNIQUECHOICE.aut` : AUT counterexample LTS
- `bad_prop_UNIQUECHOICE_unclean.aut` : file required by the *CLEAR analyser*
- `bad_prop_UNIQUECHOICE_unclean.prd` : file required by the *CLEAR analyser*
- `bad_prop_UNIQUECHOICE_res-dump.autx` : counterexample LTS with neighbourhoods, 
    required by the *CLEAR visualizer*
- `cexp_dump.aut` : example counterexample trace that can be loaded in the 3D 
                    LTS representation with the *CLEAR visualizer*  
