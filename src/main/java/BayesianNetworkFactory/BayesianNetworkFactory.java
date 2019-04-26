package BayesianNetworkFactory;

import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.FiniteNode;
import aima.core.probability.bayes.Node;
import aima.core.probability.bayes.impl.BayesNet;
import aima.core.probability.domain.ArbitraryTokenDomain;
import aima.core.probability.domain.BooleanDomain;
import aima.core.probability.domain.Domain;
import aima.core.probability.util.RandVar;
import parser.BifBNReader;
import structures.impl.CNode;

import java.util.HashMap;
import java.util.Map;

public class BayesianNetworkFactory {


    private static String[] bns = new String[]{"child", "survey", "earthquake", "asia", "cancer" +
            ""};

    public static final Map<String, BayesianNetwork> map = new HashMap<>();

    static {
        for (String bnName : bns) {
            try {
                map.put(bnName, createJTNetwork(bnName + ".bif"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static BayesianNetwork createJTNetwork(String file) throws Exception {
        return new BifBNReader(file) {
            protected Node nodeCreation(RandomVariable var, double[] probs, Node... parents) {
                return new CNode(var, probs, parents);
            }
        }.getBayesianNetwork();
    }

}