package clear_analyser.sccfinder;

import clear_analyser.graph.LTS;

/**
 *  testing SCCSet
 */
public class SCCtester {

    public SCCtester() {

    }

    public void tester(LTS lts) {
        System.out.println("\nTesting Recursive AbstractTarjan:");
        long recursiveTarjanTimeStart = System.currentTimeMillis();
        SCCSet sccSetRec = new TarjanRecursive(lts).tarjanCaller();
        System.out.println(sccSetRec.toString());
        System.out.println("Number of SCCs: " + sccSetRec.sccNumber());
        System.out.println("Length of the longest SCC: " + sccSetRec.biggestSccSize());
        long recursiveTarjanTimeEnd = System.currentTimeMillis();

        System.out.println("\nTesting Iterative AbstractTarjan:");
        long iterativeTarjanTimeStart = System.currentTimeMillis();
        SCCSet sccSetIter = new TarjanIterative(lts).tarjanCaller();
        System.out.println(sccSetIter.toString());
        System.out.println("Number of SCCs: " + sccSetIter.sccNumber());
        System.out.println("Length of the longest SCC: " + sccSetIter.biggestSccSize());
        long iterativeTarjanTimeEnd = System.currentTimeMillis();

        System.out.println("\nRecursive AbstractTarjan exec time: " +
                (recursiveTarjanTimeEnd-recursiveTarjanTimeStart) + " ms");
        System.out.println("Iterative AbstractTarjan exec time: " +
                (iterativeTarjanTimeEnd-iterativeTarjanTimeStart) + " ms");

        System.out.print("\n");
        System.out.println("Check equivalence of recursive and iterative: " + sccSetIter.equals(sccSetRec));
    }
}
