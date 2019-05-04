package BayesianNetworkFactory;

import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.Node;
import parser.BifBNReader;
import structures.jointree.CNode;

public class BayesianNetworkFactory {

    /**
     * Creation of a BayesianNetwork object read from file
     * @param file from which read BN
     * @return BayesianNetwork
     * @throws Exception if there are problem with reading or building BN
     */
    public static BayesianNetwork createJTNetwork(String file) throws Exception {
        return new BifBNReader(file) {
            protected Node nodeCreation(RandomVariable var, double[] probs, Node... parents) {
                return new CNode(var, probs, parents);
            }
        }.getBayesianNetwork();
    }

}