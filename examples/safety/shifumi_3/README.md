## CLEAR Framework
# Example: Shifumi Tournament

Specification
-------------
The shifumi example models in LNT a tournament of 
rock-paper-scissors games. 
In a typical game between two players each player forms one 
of the three possible shapes (rock, paper or scissors).
Each shape allows to defeat one of the two others, but is defeated by the 
remaining one (e.g. the rock defeats the scissors but is defeated by the paper). 
When a shape is used against the same shape the game ends in a draw, and it 
is repeated. 
The tournament allows more than two players to compete. 
When a player wins a game, she continues playing with the next player. 
On the contrary, the player who loses the game has to stop playing.
The tournament continues until there is only a winner. 
This example describes a tournament between three players.

Property
--------
The provided property guarantees no cheating by any player.
More precisely, it avoids that a player that has previously lost can play again 
in the tournament.
This is written in MCL as follows:

`[(true* . 'LOOSER !1' . true* . 'GAME . * . !1 . * ' . true*) |`<br/>
` (true* . 'LOOSER !2' . true* . 'GAME . * . !2 . * ' . true*) |`<br/>
` (true* . 'LOOSER !3' . true* . 'GAME . * . !3 . * ' . true*)`<br/>
`] false `. 

The analysed tournament contains a bug, since a dishonest player is able to 
play again after having lost a game.

Files
-----
- `shifumi.lnt` : LNT specification file, required by the *shifumi.svl* script
- `shifumi.svl` : tournament generation SVL script
- `game_3.bcg` : 3 players tournament BCG model file, required by the *cexplts_gen_bcg.svl* script
- `prop_v3_game_3.mcl` : MCL property, required by the *cexplts_gen_bcg.svl* script and 
               and the *CLEAR analyser*
- `cexplts_gen_bcg.svl` : counterexample LTS generation SVL script (from a bcg model)
- `bad_prop_v3_game_3.aut` : AUT counterexample LTS
- `bad_prop_v3_game_3_unclean.aut` : file required by the *CLEAR analyser*
- `bad_prop_v3_game_3_unclean.prd` : file required by the *CLEAR analyser*

References
----------
The Shifumi Tournament example has been developed by [Hugues Evrard](http://hevrard.org/ "Hugues Evrard"). 

