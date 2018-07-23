## CLEAR Framework
# Example: Sanitary Agency

Specification
-------------
This is a bugged version of the Sanitary Agency example, which models an 
agency that aims at supporting elderly citizens in receiving sanitary 
assistance from the public administration.  
A bank model is defined to manage fees and payments, while a cooperative model 
is built to provide transportation and meal services.
A citizen and a sanitary agency models are also defined.

Property
--------
The provided property states that the payment of a transportation service to the
transportation cooperative cannot occur after submission of a request
by a citizen to the sanitary agency. This is written in MCL as follows:  
`[ true* . 'REQUEST_EM' . true* . 'PAYMENTT_EM' . true*] false`.
The specification of this .

Files
-----
- `EX153.lnt` : main LNT specification file, required by the *cexplts_gen.svl* script
- `EX153.aut` : AUT model file, required by the *cexplts_gen.svl* script
- `prop2.mcl` : MCL property, required by the *cexplts_gen.svl* script and 
               and the *CLEAR analyser*
- `cexplts_gen.svl` : counterexample LTS generation SVL script
- `bad_prop_EX153.aut` : AUT counterexample LTS
- `bad_prop_EX153_unclean.aut` : file required by the *CLEAR analyser*
- `bad_prop_EX153_unclean.prd` : file required by the *CLEAR analyser*

References
----------
The Sanitary Agency example has been detailed in:  

*G. Salaün, L. Bordeaux and M. Schaerf,*   
*Describing and Reasoning on Web Services using Process Algebra,*  
*Proc. of ICWS'04, IEEE Computer Society, 2004, pp. 43-50*  
