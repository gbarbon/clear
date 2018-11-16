package clear_analyser.affix;

import clear_analyser.utils.STDOut;

import java.util.Collections;
import java.util.List;

/**
 * Created by gbarbon.
 */
public class Suffix extends Affix<String> {

    public Suffix() {
        super();
    }

    public Suffix(List<String> actions) {
        super(actions);
    }

    public Suffix(Affix<String> affix) {
        super(affix);
    }

    public Suffix(Suffix other) { super(other);}

    /**
     * @param action a string under analysis
     * @param suffix a suffix
     * @param actions the sequence of actions
     * @return true if given action is coherent with the given suffix and the sequence, false
     * otherwise
     */
    public static boolean checkActionCoherency(String action,  List<String> actions,
                                                  Suffix suffix) {
        STDOut.dbugLog("Checking. action is "+ action+ " Suffix is: "+suffix+" Actions are: " +
                ""+actions);
        // FIXME: do not use sting comparison with join!
        //return (suffix!=null) && StringUtils.join(actions, "").endsWith(
        //        action + StringUtils.join(suffix.toList(), ""));


        Suffix tmpSuff = new Suffix(suffix);
        tmpSuff.addOnTop(action);
        //STDOut.dbugLog("indexOfSublist: "+ Collections.indexOfSubList(actions, tmpSuff.toList
        // ()));
        return (suffix!=null) &&
                Collections.indexOfSubList(actions, tmpSuff.toList())!=-1 &&
                Collections.indexOfSubList(actions, tmpSuff.toList())==(actions.size()-tmpSuff
                        .toList().size());
    }
}
