package yabby.app.draw;

import yabby.app.beauti.BeautiDoc;

public class DoubleInputEditor extends InputEditor.Base {
    private static final long serialVersionUID = 1L;

    public DoubleInputEditor(BeautiDoc doc) {
        super(doc);
    }
    //public DoubleInputEditor() {}

    @Override
    public Class<?> type() {
        return Double.class;
    }
} // class DoubleInputEditor
