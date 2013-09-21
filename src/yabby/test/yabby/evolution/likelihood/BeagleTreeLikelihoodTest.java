package test.yabby.evolution.likelihood;


import org.junit.Test;

import yabby.evolution.likelihood.BeagleTreeLikelihood;
import yabby.evolution.likelihood.TreeLikelihood;




/** Same as TreeLikelihoodTest, but for Beagle Tree Likelihood. 
 * **/
public class BeagleTreeLikelihoodTest extends TreeLikelihoodTest {

	public BeagleTreeLikelihoodTest() {
		super();
		//System.setProperty("java.only", "true");
	} // c'tor
	
	@Override
	protected TreeLikelihood newTreeLikelihood() {
		return new BeagleTreeLikelihood();
	}

	@Test
    public void testSDolloLikelihood() throws Exception {
		// beagle and SDollo do not mix
    }
	
//	@Test
//	public void testAscertainedJC69Likelihood() throws Exception {
//		// fails
//	}
	
//	@Test
//	public void testHKY85GLikelihood() throws Exception {
//		// fails
//	}
	
//	@Test
//	public void testGTRGLikelihood() throws Exception {
//		// fails
//	}

} // class BeagleTreeLikelihoodTest
