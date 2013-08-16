package yabby.app.draw;

import java.util.ArrayList;
import java.util.List;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.YABBYObject;


@Description("Set of plugins to represent partially finished models in GUIs")
public class PluginSet extends YABBYObject {
    public Input<List<YABBYObject>> m_plugins = new Input<List<YABBYObject>>("plugin", "set of the plugins in this collection", new ArrayList<YABBYObject>());

    @Override
    public void initAndValidate() {
    }
}
