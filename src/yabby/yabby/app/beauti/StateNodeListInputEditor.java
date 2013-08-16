package yabby.app.beauti;

import java.util.List;

import yabby.app.draw.ListInputEditor;
import yabby.core.Input;
import yabby.core.YABBYObject;
import yabby.core.StateNode;


public class StateNodeListInputEditor extends ListInputEditor {
	private static final long serialVersionUID = 1L;

	public StateNodeListInputEditor(BeautiDoc doc) {
		super(doc);
	}
	
	@Override
	public Class<?> type() {
		return List.class;
	}
	
	@Override
	public Class<?> baseType() {
		return StateNode.class;
	}
	
	@Override
	public void init(Input<?> input, YABBYObject plugin, int itemNr, ExpandOption bExpandOption, boolean bAddButtons) {
		m_buttonStatus = ButtonStatus.NONE;
		super.init(input, plugin, itemNr, bExpandOption, bAddButtons);
	}

}
