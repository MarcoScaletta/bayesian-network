package BayesianNetworkFactory;

import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.FiniteNode;
import aima.core.probability.bayes.impl.BayesNet;
import aima.core.probability.domain.ArbitraryTokenDomain;
import aima.core.probability.domain.BooleanDomain;
import aima.core.probability.domain.Domain;
import aima.core.probability.util.RandVar;
import structures.impl.CNode;

public class BayesianNetworkFactory {


    public static final BayesianNetwork[] bayesianNetworks =
            {survery(),earthquake(),asia()};

    public static BayesianNetwork survery(){

        Domain yad = new ArbitraryTokenDomain("young","adult", "old");

        Domain mf = new ArbitraryTokenDomain("M","F");
        Domain hu = new ArbitraryTokenDomain("high","uni");
        Domain es = new ArbitraryTokenDomain("emp","self");
        Domain sb = new ArbitraryTokenDomain("small","big");
        Domain cto = new ArbitraryTokenDomain("car","train","other");

        RandomVariable A = new RandVar("A", yad);
        RandomVariable S = new RandVar("S", mf);
        RandomVariable E = new RandVar("E", hu);
        RandomVariable O = new RandVar("O", es);
        RandomVariable R = new RandVar("R", sb);
        RandomVariable T = new RandVar("T", cto);

        FiniteNode nodeA = new CNode(A,new double[]{0.3, 0.5, 0.2});

        FiniteNode nodeS = new CNode(S,new double[]{0.6, 0.4});
        FiniteNode nodeE = new CNode(E,
                new double[]{
                        0.75, 0.25,
                        0.64, 0.36,
                        0.72, 0.28,
                        0.7, 0.3,
                        0.88, 0.12,
                        0.9, 0.1

                },nodeA,nodeS);

        FiniteNode nodeO = new CNode(O,
                new double[]{
                        0.96, 0.04,
                        0.92, 0.08
                },nodeE);

        FiniteNode nodeR = new CNode(R,
                new double[]{
                        0.25, 0.75,
                        0.2, 0.8
                },nodeE);

        FiniteNode nodeT = new CNode(T,
                new double[]{
                        0.48, 0.42, 0.10,
                        0.58, 0.24, 0.18,
                        0.56, 0.36, 0.08,
                        0.70, 0.21, 0.09

                },nodeO,nodeR);

        return new BayesNet(nodeA,nodeS);

    }

    public static BayesianNetwork earthquake(){

        RandomVariable B = new RandVar("Buglary", new BooleanDomain());
        RandomVariable E = new RandVar("Earthquake", new BooleanDomain());
        RandomVariable A = new RandVar("Alarm", new BooleanDomain());
        RandomVariable J = new RandVar("JohnCalls", new BooleanDomain());
        RandomVariable M = new RandVar("MaryCalls", new BooleanDomain());


        FiniteNode nodeB = new CNode(B,new double[]{0.01, 0.99});

        FiniteNode nodeE = new CNode(E,new double[]{0.02, 0.98});

        FiniteNode nodeA = new CNode(A,
                new double[]{
                        0.95, 0.05,
                        0.94, 0.06,
                        0.29, 0.71,
                        0.001, 0.999
                },nodeB,nodeE);

        FiniteNode nodeJ = new CNode(J,
                new double[]{
                        0.9, 0.1,
                        0.05, 0.95
                },nodeA);

        FiniteNode nodeM = new CNode(M,
                new double[]{
                        0.7, 0.3,
                        0.01, 0.99
                },nodeA);


        return new BayesNet(nodeB,nodeE);

    }

    public static BayesianNetwork asia(){

        RandomVariable A = new RandVar("Asia", new BooleanDomain());
        RandomVariable T = new RandVar("Tub", new BooleanDomain());
        RandomVariable S = new RandVar("Smoke", new BooleanDomain());
        RandomVariable L = new RandVar("Lung", new BooleanDomain());
        RandomVariable B = new RandVar("Bronc", new BooleanDomain());
        RandomVariable E = new RandVar("Either", new BooleanDomain());
        RandomVariable X = new RandVar("XRay", new BooleanDomain());
        RandomVariable D = new RandVar("Dysp", new BooleanDomain());


        FiniteNode nodeA = new CNode(A,new double[]{0.01, 0.99});


        FiniteNode nodeS = new CNode(S,new double[]{0.5, 0.5});


        FiniteNode nodeT = new CNode(T,
                new double[]{
                        0.05, 0.95,
                        0.01, 0.99
                },nodeA);

        FiniteNode nodeL = new CNode(L,
                new double[]{
                        0.1, 0.9,
                        0.01, 0.99
                },nodeS);

        FiniteNode nodeB = new CNode(B,
                new double[]{
                        0.6, 0.4,
                        0.3, 0.7
                },nodeS);


        FiniteNode nodeE = new CNode(E,
                new double[]{
                        1.0, 0.0,
                        1.0, 0.0,
                        1.0, 0.0,
                        0.0, 1.0
                },nodeL,nodeT);


        FiniteNode nodeX= new CNode(X,
                new double[]{
                        0.98, 0.02,
                        0.05, 0.95
                },nodeE);

        FiniteNode nodeD = new CNode(D,
                new double[]{
                        0.9, 0.1,
                        0.8, 0.2,
                        0.7, 0.3,
                        0.1, 0.9
                },nodeB,nodeE);

        return new BayesNet(nodeA,nodeS);

    }
}
