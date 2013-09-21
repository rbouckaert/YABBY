package yabby.app.beauti;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JTextField;

import yabby.app.draw.InputEditor;
import yabby.app.draw.ListInputEditor;
import yabby.app.draw.StringInputEditor;
import yabby.core.YABBYObject;
import yabby.core.Input;
import yabby.core.Logger;




public class LoggerListInputEditor extends ListInputEditor {
    private static final long serialVersionUID = 1L;

	public LoggerListInputEditor(BeautiDoc doc) {
		super(doc);
	}

    @Override
    public Class<?> type() {
        return List.class;
    }

    @Override
    public Class<?> baseType() {
        return Logger.class;
    }
    

    @Override
    public void init(Input<?> input, YABBYObject plugin, int itemNr, ExpandOption bExpandOption, boolean bAddButtons) {
    	super.init(input, plugin, itemNr, bExpandOption, bAddButtons);
    }
    
    @Override
    protected void addSingleItem(YABBYObject plugin) {
    	currentLogger = (Logger) plugin;
    	super.addSingleItem(plugin);
    }
    
    public InputEditor createFileNameEditor() throws Exception {
        Input<?> input = currentLogger.fileNameInput;
        StringInputEditor fileNameEditor = new StringInputEditor(doc);
        fileNameEditor.init(input, currentLogger, -1, ExpandOption.FALSE, true);

        // ensure file name entry has larger size than the standard size
        JTextField fileNameEntry = fileNameEditor.getEntry();
        Dimension size = new Dimension(400, 25);
        fileNameEntry.setMinimumSize(size);
        fileNameEntry.setPreferredSize(size);
        return fileNameEditor;
    }
    
    Logger currentLogger;
}
