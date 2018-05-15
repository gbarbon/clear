## CLEAR Framework
# Example: Interleaving Bug v2


Specification
-------------
This example presents an extended and more complicated version of the 
'Interleaving Bug' example. The LNT specification consists of three parts in 
sequence. The initial part (INITi actions) and the final part (CLOSEi actions)
are used, respectively, for initialisation and closing purposes. The central 
part consists of a parallel composition where several EXECi actions are 
executed in parallel with another branch where there is a 'select' construct.
The 'select' construct allows one to choose between two branches with several 
SENDi actions.

Property
--------
The property states that a SEND2 action should never be followed by a SEND1 
action: `([ true* . 'SEND2' . true* . 'SEND1' . true* ] false)`.

Figure Description
------------------
We can clearly distinguish the initial part with black transitions because all 
these transitions can lead to a possibly erroneous part of the system. 
Likewise, we can see the closing part of the specification where all 
transitions are incorrect (red) and where the bug cannot be avoided. 
These two parts (entirely black or entirely red) can be viewed as 'noise' or 
actions that are not helpful from a debugging perspective. 
In contrast the central part is highly interesting. There are six neighbourhood 
states in that part of the LTS corresponding to a choice between executing a 
correct part of the specification (avoiding the sequence with a SEND2 action 
followed by a SEND1 action) leading to the white state (sink state), or 
executing an incorrect part of the specification. There are six choices because 
this choice is in parallel with the sequence of EXECi actions and can then 
appear at different states (interleaving). This is typical of a bug which is 
interleaved with other actions, looking in that case like a 'spider web' due to 
the attraction of the sink state in the visualization.

Files
-----
- `INTERLEAVINGV2.lnt` : LNT specification file
- `INTERLEAVINGV2.aut` : AUT model file
- `prop.mcl` : MVL property
- `cexplts_gen.svl` : SVL script for the Counterexample LTS generation
- `bad_prop_INTERLEAVINGV2.aut` : AUT Counterexample LTS
- `bad_prop_INTERLEAVINGV2_unclean.aut` : used by the CLEAR analyser
- `bad_prop_INTERLEAVINGV2_unclean.prd` : used by the CLEAR analyser
- `bad_prop_INTERLEAVINGV2_res-dump.autx` : Counterexample LTS with 
    neighbourhoods, input file for the CLEAR visualizer 
