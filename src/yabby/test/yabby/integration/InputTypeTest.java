package test.yabby.integration;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import yabby.core.Input;
import yabby.core.YABBYObject;
import yabby.util.AddOnManager;

import junit.framework.TestCase;

public class InputTypeTest  extends TestCase {
	
	@Test
	public void testInputTypeCanBeSet() {
        List<String> sPluginNames = AddOnManager.find(yabby.core.YABBYObject.class, AddOnManager.IMPLEMENTATION_DIR);
        List<String> failingInputs = new ArrayList<String>();
        for (String sPlugin : sPluginNames) {
            try {
                YABBYObject plugin = (YABBYObject) Class.forName(sPlugin).newInstance();
                List<Input<?>> inputs = plugin.listInputs();
                for (Input<?> input : inputs) {
                	if (input.getType() == null) {
                		try {
                			input.determineClass(plugin);
                		} catch (Exception e2) {
                			failingInputs.add(sPlugin + ":" + input.getName());
                		}
                	}
                }
            } catch (Exception e) {
            	// ignore
            }
        }
        assertTrue("Type of input could not be set for these inputs (probably requires to be set by using the appropriate constructure of Input): "
                + failingInputs.toString(), failingInputs.size() == 0);
	}

}
