package clear_analyser.affix;

import java.util.Collections;
import java.util.List;

/**
 * Created by gbarbon.
 */
public class Prefix extends Affix<String> {

    public Prefix() {
        super();
    }

    public Prefix(List<String> actions) {
        super(actions);
    }

    public Prefix(Affix<String> affix) {
        super(affix);
    }

    public Prefix(Prefix other) { super(other);}

    /**
     * @param action a string under analysis
     * @param prefix a prefix
     * @param actions the sequence of actions
     * @return true if given action is coherent with the given prefix and the sequence, false
     * otherwise
     */
    public static boolean checkActionCoherency(String action, List<String> actions,
                                                  Prefix prefix) {
        // FIXME: do not use sting comparison with join!
        //return (prefix!=null) && StringUtils.join(actions, "").startsWith(
        //        StringUtils.join(prefix.toList(), "") + action);

        Prefix tmpPref = new Prefix(prefix);
        tmpPref.add(action);
        return (prefix!=null) && Collections.indexOfSubList(actions, tmpPref.toList())==0;



    }
    // TODO: produce new methods which is not static (I think Prefix can be this, instead of a
    // parameter)
}
