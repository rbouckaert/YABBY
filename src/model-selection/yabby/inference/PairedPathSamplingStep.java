package yabby.inference;

import yabby.core.Description;
import yabby.core.Distribution;
import yabby.core.util.Evaluator;
import yabby.core.Input;
import yabby.core.Operator;
import yabby.core.Input.Validate;
import yabby.core.util.CompoundDistribution;
import yabby.util.Randomizer;

@Description("Calculate marginal likelihood through path sampling for a single step when comparing two models")
public class PairedPathSamplingStep extends PathSamplingStep {
	public Input<Distribution> posterior2Input = new Input<Distribution>("posterior2", "posterior of the second model, the " +
			"first one is represented by 'posterior'.", Validate.REQUIRED);
	
	Distribution model1;
	Distribution model2;
	
	
	@Override
	public void initAndValidate() throws Exception {
		CompoundDistribution distribution;
		distribution = new CompoundDistribution();
		distribution.pDistributions.get().add(posteriorInput.get());
		distribution.pDistributions.get().add(posterior2Input.get());
		distribution.initAndValidate();
		
		super.initAndValidate();
		
		posterior = distribution;
		model1 = posteriorInput.get();
		model2 = posterior2Input.get();
	}
	
	
	   /**
     * main MCMC loop *
     */
    protected void doLoop() throws Exception {
    	
        double logModel1Prob = model1.calculateLogP();
        double logModel2Prob = model2.calculateLogP();        
        oldLogLikelihood = logModel1Prob * (1.0-beta) + logModel2Prob * beta; 
    	
    	
        for (int iSample = -burnIn; iSample <= chainLength; iSample++) {
            final int currentState = iSample;

            state.store(currentState);
            if (storeEvery > 0 && iSample % storeEvery == 0 && iSample > 0) {
                state.storeToFile(iSample);
                // Do not store operator optimisation information
                // since this may not be valid for the next step
                // especially when sampling from the prior only
            	//operatorSchedule.storeToFile();
            }

            Operator operator = operatorSchedule.selectOperator();
            //System.out.print("\n" + iSample + " " + operator.getName()+ ":");

            final Distribution evaluatorDistribution = operator.getEvaluatorDistribution();
            Evaluator evaluator = null;

            if (evaluatorDistribution != null) {
                evaluator = new Evaluator() {
                    @Override
                    public double evaluate() {
                        double logP = 0.0;

                        state.storeCalculationNodes();
                        state.checkCalculationNodesDirtiness();

                        try {
                            logP = evaluatorDistribution.calculateLogP();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(1);
                        }

                        state.restore();
                        state.store(currentState);

                        return logP;
                    }
                };
            }

            double fLogHastingsRatio = operator.proposal(evaluator);

            if (fLogHastingsRatio != Double.NEGATIVE_INFINITY) {

                state.storeCalculationNodes();
                state.checkCalculationNodesDirtiness();

                
                posterior.calculateLogP();
                logModel1Prob = model1.getArrayValue();
                logModel2Prob = model2.getArrayValue();
                
                newLogLikelihood = logModel1Prob * (1.0 - beta) + logModel2Prob * beta; 

                logAlpha = newLogLikelihood - oldLogLikelihood + fLogHastingsRatio; //CHECK HASTINGS
                //System.out.println(logAlpha + " " + fNewLogLikelihood + " " + fOldLogLikelihood);
                if (logAlpha >= 0 || Randomizer.nextDouble() < Math.exp(logAlpha)) {
                    // accept
                    oldLogLikelihood = newLogLikelihood;
                    state.acceptCalculationNodes();

                    if (iSample >= 0) {
                        operator.accept();
                    }
                    //System.out.print(" accept");
                } else {
                    // reject
                    if (iSample >= 0) {
                        operator.reject();
                    }
                    state.restore();
                    state.restoreCalculationNodes();
                    //System.out.print(" reject");
                }
                state.setEverythingDirty(false);
            } else {
                // operation failed
                if (iSample >= 0) {
                    operator.reject();
                }
                state.restore();
                //System.out.print(" direct reject");
            }
            log(iSample);

            operator.optimize(logAlpha);
            callUserFunction(iSample);
        }
    }

}
