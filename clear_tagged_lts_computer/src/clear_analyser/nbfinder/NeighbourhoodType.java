package clear_analyser.nbfinder;

/**
 * Created by gbarbon.
 */

/**
 * GREEN: one (or more) Correct (green) out-transitions, one (or more) Neutral (black) out-trans, no Incorrect (red) out-trans
 * RED: one (or more) Incorrect (red) out-transitions, one (or more) Neutral (black) out-trans, no Correct (green) out-trans
 * GREENRED: one (or more) Correct (green) out-trans, one (or more) Incorrect (red) out-trans, no Neutral (black) out-trans
 * GREENREDBLACK: all kind of out-trans
 * UNKNOWN: a neighbourhood has been detected, but the type is still unknown
 */
public enum NeighbourhoodType{GREEN, RED, GREENRED, GREENREDBLACK, UNKNOWN}
