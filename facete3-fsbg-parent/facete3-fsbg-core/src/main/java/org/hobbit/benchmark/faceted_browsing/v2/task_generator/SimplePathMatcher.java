package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.commons.jena.jgrapht.LabeledEdge;
import org.aksw.jena_sparql_api.sparql_path2.JGraphTUtils;
import org.aksw.jena_sparql_api.sparql_path2.Nfa;
import org.aksw.jena_sparql_api.sparql_path2.PathCompiler;
import org.aksw.jena_sparql_api.sparql_path2.PredicateClass;
import org.aksw.jenax.sparql.path.SimplePath;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

public class SimplePathMatcher {
    public interface NfaMatcher<X> {
        boolean canMatchInput(X input);
        void advance(X input);
        boolean isAccepted();
        boolean isInDeadEnd();
    }

    public static class NfaMatcherImpl<S, T, X>
        implements NfaMatcher<X>
    {
        protected Nfa<S, T> nfa;
        protected Set<S> currentStates;
        protected Predicate<T> isEpsilonTransition;
        protected BiPredicate<T, X> doesTransitionMatchInput;

        public NfaMatcherImpl(Nfa<S, T> nfa, Set<S> initStates, Predicate<T> isEpsilonTransition,
                BiPredicate<T, X> doesTransitionMatchInput) {
            super();
            this.nfa = nfa;
            this.currentStates = JGraphTUtils.transitiveGet(nfa.getGraph(), initStates, 1, isEpsilonTransition);
            this.isEpsilonTransition = isEpsilonTransition;
            this.doesTransitionMatchInput = doesTransitionMatchInput;


        }

        public Set<S> getReachableStates(X input) {
            // Note: Here we require epsilon transitions to already be resolved
            // in order for isAccepted() to work on set intersection

            // Get transitions
            Set<T> effectiveTransitions = JGraphTUtils.resolveTransitions(
                    nfa.getGraph(),
                    isEpsilonTransition,
                    currentStates,
                    false);

            Set<S> tmp = effectiveTransitions.stream()
                .filter(t -> doesTransitionMatchInput.test(t, input))
                .map(nfa.getGraph()::getEdgeTarget)
                .collect(Collectors.toCollection(LinkedHashSet::new));


            // Resolve epsilon transitions for the reached states
            Set<S> result = JGraphTUtils.transitiveGet(nfa.getGraph(), tmp, 1, isEpsilonTransition);
            return result;
        }

        public boolean canMatchInput(X input) {
            Set<S> targetStates = getReachableStates(input);
            boolean result = !targetStates.isEmpty();
            return result;
        }

        public void advance(X input) {
            currentStates = getReachableStates(input);
        }

        /**
         * Whether an accepted state has been reached
         *
         * @return
         */
        public boolean isAccepted() {

            Set<S> tmp = Sets.intersection(currentStates, nfa.getEndStates());
            boolean result = !tmp.isEmpty();
            return result;
        }

        /**
         * Whether the set of states in empty; so no more transitions can be followed
         *
         * @return
         */
        public boolean isInDeadEnd() {
            boolean result = currentStates.isEmpty();
            return result;
        }
    }

    public static NfaMatcher<P_Path0> createStepMatcher(Path path) {
        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa = PathCompiler.compileToNfa(path);
        //Integer, LabeledEdge<Integer, PredicateClass>, P_Path0
        NfaMatcher<P_Path0> result = new NfaMatcherImpl<>(nfa, nfa.getStartStates(), t -> t.getLabel() == null, (t, i) -> PredicateClass.matchesStep(t.getLabel(), i));

        return result;
    }

    public static Predicate<SimplePath> createPathMatcher(Path path) {
        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa = PathCompiler.compileToNfa(path);

//		System.out.println(nfa);
        return input -> {
            NfaMatcher<P_Path0> stepMatcher = new NfaMatcherImpl<>(nfa, nfa.getStartStates(), t -> t.getLabel() == null, (t, i) -> PredicateClass.matchesStep(t.getLabel(), i));

            for(P_Path0 step : input.getSteps()) {
                stepMatcher.advance(step);

                if(stepMatcher.isInDeadEnd()) {
                    break;
                }
            }

            boolean r = stepMatcher.isAccepted();
            return r;
        };
    }
}
