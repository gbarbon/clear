## CLEAR Framework
# Example: Unique Choice Bug


Specification
-------------
 

Property
--------
 

Figure Description
-----------------
The bug can be reached from a single neighbourhood, which represents a 
mandatory and unique choice to obtain the bug.

Files
-----
- `UNIQUECHOICE.lnt` : LNT specification file
- `UNIQUECHOICE.aut` : AUT model file
- `prop.mcl` : MVL property
- `cexplts_gen.svl` : SVL script for the Counterexample LTS generation
- `bad_prop_UNIQUECHOICE.aut` : AUT Counterexample LTS
- `bad_prop_UNIQUECHOICE_unclean.aut` : used by the CLEAR analyser
- `bad_prop_UNIQUECHOICE_unclean.prd` : used by the CLEAR analyser
- `bad_prop_UNIQUECHOICE_res-dump.autx` : Counterexample LTS with neighbourhoods,
    input file for the CLEAR visualizer 
