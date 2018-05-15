## CLEAR Framework
# Example: Causality Bug

Specification
-------------
This example is a producer-consumer system. The specification is composed of 
three main processes: a producer process, a consumer process and a process that 
can either be a consumer or a producer. Each process can loop infinitely or 
break the loop and terminate the execution. A deployer process is also part of 
the specification in order to initiate the three other processes.

Property
--------
The provided property states that a process cannot consume if something has not
been produced before. This is written in MCL as follows:
`[ ( not ”PRODUCE” )* . ”CONSUME” . true* ] false`.
The specification violates this property when the PRODCONS process acts as a 
consumer, because it can consume without ensuring that PRODUCE has been
performed beforehand.

Figure Description
------------------
The LTS is divided into three parts. The initial part represents the portion 
of code in which every process performs the deployment and this part of the 
model has no impact on the bug (no neighbourhoods and all neutral transitions).
Then, a set of neighbourhoods of the same type is present between the
first part of the LTS and the second (central) one. These neighbourhoods have
all a correct and a neutral transition, and represent the first choice that
contributes to the cause of the bug (when the PRODCONS process decides to be
a consumer). Those neighbourhoods can be viewed as a 'frontier' between
the initial and central part of the LTS. All the correct transitions are
directed to the sink state, that abstracts the correct part of the LTS.
A second frontier is present between the central part of the LTS and the third
part (with all red transitions). This frontier is composed of neighbourhoods
that represent the second cause of the bug, that happens when a CONSUME
action has been performed without an initial PRODUCE action. The figure
with the two frontiers helps in understanding that there is a causality between
both kinds of neighbourhoods.

Files
-----
- `COMMPROC.lnt` : LNT specification file
- `COMMPROC.aut` : AUT model file
- `prop.mcl` : MVL property
- `cexplts_gen.svl` : SVL script for the Counterexample LTS generation
- `bad_prop_COMMPROC.aut` : AUT Counterexample LTS
- `bad_prop_COMMPROC_unclean.aut` : used by the CLEAR analyser
- `bad_prop_COMMPROC_unclean.prd` : used by the CLEAR analyser           
- `bad_prop_COMMPROC_res-dump.autx` : Counterexample LTS with neighbourhoods,
    input file for the CLEAR visualizer 
