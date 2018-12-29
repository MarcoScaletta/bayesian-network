import aima.core.probability.CategoricalDistribution;
import aima.core.probability.example.HMMExampleFactory;
import aima.core.probability.hmm.HiddenMarkovModel;
import aima.core.probability.hmm.exact.HMMForwardBackward;
import aima.core.util.math.Matrix;

import java.util.ArrayList;

public class HMMPredict {

    private HMMForwardBackward h = null;
    private HiddenMarkovModel hmm;

    public HMMPredict(HiddenMarkovModel hmm){
        this.hmm = hmm;
        this.h = new HMMForwardBackward(hmm);
    }



    public CategoricalDistribution predict(CategoricalDistribution f1_t) {
        return hmm.convert(predict(hmm.convert(f1_t)));
    }

    public Matrix predict(Matrix f1_t) {
        return hmm.normalize(hmm.getTransitionModel().transpose().times(f1_t));
    }

}
