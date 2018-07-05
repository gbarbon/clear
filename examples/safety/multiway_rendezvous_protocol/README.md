## CLEAR Framework
# Example: Multiway Rendezvous Protocol (bugged version)

Specification
-------------
This is a preliminary bugged version of the multiway rendezvous protocol 
implemented in DLC (Distributed LNT Compiler), a tool that automatically 
generates a distributed implementation in C of a given LNT specification.
The multiway rendezvous protocol must allow processes (called tasks) to
synchronize, through message exchange, on a given gate.
In this case messages (e.g., abort, commit, ready) are exchanged between two 
tasks `T1` and `T16` and a gate `A` to synchronize. 

Property
--------
The provided property states that one of the two tasks cannot execute more than two actions on gate `A`. This is written in MCL as follows:
`[ true* . 'ACTION !DLC_GATE_A .*' . true* . 'ACTION !DLC_GATE_A .*' . true* . 'ACTION !DLC_GATE_A . *' . true* ] false`.
The specification of this preliminary version of the protocol violates the property because it allows to perform three synchronizations on gate `A`, 
that is prohibited by the property.

Files
-----
- `implem.lnt` : main LNT specification file, required by the *cexplts_gen.svl* script
- `data.lnt` : LNT spec file, required by *implem.lnt* 
- `generic_data.lnt` : LNT spec file, required by *implem.lnt*
- `latest.lnt` : LNT spec file, required by *implem.lnt*
- `task_T1.lnt` : LNT spec file, required by *implem.lnt*
- `task_T16.lnt` : LNT spec file, required by *implem.lnt*
- `implem.aut` : AUT model file, required by the *cexplts_gen.svl* script
- `prop.mcl` : MCL property, required by the *cexplts_gen.svl* script and 
               and the *CLEAR analyser*
- `cexplts_gen.svl` : counterexample LTS generation SVL script
- `bad_prop_implem.aut` : AUT counterexample LTS
- `bad_prop_implem_unclean.aut` : file required by the *CLEAR analyser*
- `bad_prop_implem_unclean.prd` : file required by the *CLEAR analyser*

References
----------
The DLC compiler has been developed by [Hugues Evrard](http://hevrard.org/ "Hugues Evrard"). 
The bug detailed in this example was only present in a preliminary version of the compiler.
The latest version of DLC can be found 
[here](http://hevrard.org/project/dlc/ "Distributed LNT Compiler").

