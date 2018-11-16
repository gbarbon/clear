## CLEAR framework

Test cases used for the empirical study:

- test1: Vending Machine case study
- test2: Communicating Processes case study

Evaluation:
-------

The developers were divided in two groups, in order to evaluate both specifications with and without the abstracted counterexample. The first group was provided with the vending machine specification without the abstracted counterexample and the communicating processes test case with the abstracted counterexample. We did the opposite with the second group of users. We gave to the users a description of the test case, the LNT specification of the test, the property, a normal counterexample and an abstracted counterexample with an explanation of our method. The developers were asked to discover the bug and measure the total time spent in debugging each specification, then to send us the results by email.
When a developer did not discover an actual bug we considered her result as a false positive and we did not take it into account in computing total average times, since it invalidated the final results.

The total average time spent in finding the bug in both test cases without our techniques is of 19 minutes, while the average time using the abstracted counterexample is of 15 minutes, showing a gain in terms of time with the use of our approach. In general, the gain in time is not that high. This is due to two main reasons. First, the length of the two counterexamples is quite short (27 and 15 actions respectively) w.r.t real cases, making the advantage of our techniques less obvious. This is caused by simple test cases specifications, expressly chosen to allow developers to carry out tests in a reasonable time.
The aim of our approach is not to debug simple test cases like these ones, but is rather to complement existing analysis techniques to help the developer when debugging complex real cases. Second, a part of the developers were using our method for the first time, while it is worth noting that our method requires some knowledge in order to use it properly.


Finally, we also asked developers’ opinion about the benefit given by our method in detecting the bug. Over 17 developers, only 4 of them said the abstracted counterexample was not useful or that they did not need it. Almost two-thirds of the developers agreed considering our approach helpful: 8 found that our technique was useful and 3 said that it can be useful in some circumstances (the remaining 2 did not express an opinion).

License
=======

Copyright 2018

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


