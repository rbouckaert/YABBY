package yabby.app.draw;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JOptionPane;

import yabby.app.beauti.BeautiConfig;
import yabby.app.beauti.BeautiDoc;
import yabby.app.beauti.BeautiSubTemplate;
import yabby.app.draw.InputEditor.ButtonStatus;
import yabby.app.draw.InputEditor.ExpandOption;
import yabby.core.YABBYObject;
import yabby.core.Input;
import yabby.core.Input.Validate;
import yabby.util.AddOnManager;




/** Can create InputEditors for inputs of Plugins 
 * and there are some associated utility methods **/
public class InputEditorFactory {
    /**
     * map that identifies the InputEditor to use for a particular type of Input *
     */
    HashMap<Class<?>, String> inputEditorMap;
    HashMap<Class<?>, String> listInputEditorMap;
    BeautiDoc doc;
    
	public InputEditorFactory(BeautiDoc doc) {
		this.doc = doc;
		init();
	}

    public void init() {
        // register input editors
        inputEditorMap = new HashMap<Class<?>, String>();
        listInputEditorMap = new HashMap<Class<?>, String>();

//        String [] sKnownEditors = new String [] {"beast.app.draw.DataInputEditor","beast.app.beauti.AlignmentListInputEditor", "beast.app.beauti.FrequenciesInputEditor", "beast.app.beauti.OperatorListInputEditor", "beast.app.beauti.ParametricDistributionInputEditor", "beast.app.beauti.PriorListInputEditor", "beast.app.beauti.SiteModelInputEditor", "beast.app.beauti.TaxonSetInputEditor", "beast.app.beauti.TipDatesInputEditor", "beast.app.draw.BooleanInputEditor", "beast.app.draw.DoubleInputEditor", "beast.app.draw.EnumInputEditor", "beast.app.draw.IntegerInputEditor", "beast.app.draw.ListInputEditor", 
//        		"beast.app.draw.ParameterInputEditor", "beast.app.draw.PluginInputEditor", "beast.app.draw.StringInputEditor"};
//        registerInputEditors(sKnownEditors);
        String[] PACKAGE_DIRS = {"beast.app",};
        for (String sPackage : PACKAGE_DIRS) {
            List<String> sInputEditors = AddOnManager.find("beast.app.draw.InputEditor", sPackage);
            registerInputEditors(sInputEditors.toArray(new String[0]));
        }
    }

    private void registerInputEditors(String[] sInputEditors) {
    	//BeautiDoc doc = new BeautiDoc();
        for (String sInputEditor : sInputEditors) {
            try {
                Class<?> _class = Class.forName(sInputEditor);
                
                
                Constructor<?> con = _class.getConstructor(BeautiDoc.class);
                InputEditor editor = (InputEditor) con.newInstance(doc);
                
                //InputEditor editor = (InputEditor) _class.newInstance();
                Class<?>[] types = editor.types();
                for (Class<?> type : types) {
                    inputEditorMap.put(type, sInputEditor);
                    if (editor instanceof ListInputEditor) {
                        Class<?> baseType = ((ListInputEditor) editor).baseType();
                        listInputEditorMap.put(baseType, sInputEditor);
                    }
                }
            } catch (java.lang.InstantiationException e) {
                // ingore input editors that are inner classes
            } catch (Exception e) {
                // print message
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * add all inputs of a plugin to a box *
     */
    public List<InputEditor> addInputs(Box box, YABBYObject plugin, InputEditor editor, InputEditor validateListener, BeautiDoc doc) {
        /* add individual inputs **/
        List<Input<?>> inputs = null;
        List<InputEditor> editors = new ArrayList<InputEditor>();
    	
        try {
            inputs = plugin.listInputs();
        } catch (Exception e) {
            // TODO: handle exception
        }
        for (Input<?> input : inputs) {
            try {
                String sFullInputName = plugin.getClass().getName() + "." + input.getName();
                if (!doc.beautiConfig.suppressPlugins.contains(sFullInputName)) {
                    InputEditor inputEditor = createInputEditor(input, plugin, true, ExpandOption.FALSE, ButtonStatus.ALL, editor, doc);
                    box.add(inputEditor.getComponent());
                    box.add(Box.createVerticalStrut(5));
                    //box.add(Box.createVerticalGlue());
                    if (validateListener != null) {
                        inputEditor.addValidationListener(validateListener);
                    }
                    editors.add(inputEditor);
                }
            } catch (Exception e) {
                // ignore
                System.err.println(e.getClass().getName() + ": " + e.getMessage() + "\n" +
                        "input " + input.getName() + " could not be added.");
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Could not add entry for " + input.getName());
            }
        }
        box.add(Box.createVerticalGlue());
        return editors;
    } // addInputs


    public InputEditor createInputEditor(Input<?> input, YABBYObject plugin, BeautiDoc doc) throws Exception {
        return createInputEditor(input, plugin, true, InputEditor.ExpandOption.FALSE, ButtonStatus.ALL, null, doc);
    }

    public InputEditor createInputEditor(Input<?> input, YABBYObject plugin, boolean bAddButtons,
                                                ExpandOption bForceExpansion, ButtonStatus buttonStatus,
                                                InputEditor editor, BeautiDoc doc) throws Exception {
    	return createInputEditor(input, -1, plugin, bAddButtons, bForceExpansion, buttonStatus, editor, doc);
    }
    
    public InputEditor createInputEditor(Input<?> input, int listItemNr, YABBYObject plugin, boolean bAddButtons,
                ExpandOption bForceExpansion, ButtonStatus buttonStatus,
                InputEditor editor, BeautiDoc doc) throws Exception {
        if (input.getType() == null) {
            input.determineClass(plugin);
        }
        //Class<?> inputClass = input.get() != null ? input.get().getClass(): input.getType();
        Class<?> inputClass = input.getType();
        if (inputClass == null) {
        	return null;
        }
        if (listItemNr >= 0) {
        	inputClass = ((List<?>)input.get()).get(listItemNr).getClass();
        } else {
        	if (input.get() != null && !input.get().getClass().equals(inputClass)
        			&& !(input.get() instanceof ArrayList)) {
        		System.err.println(input.get().getClass() + " != " + inputClass);
        		inputClass = input.get().getClass();
        	}
        }

System.err.print(inputClass.getName() + " => ");        
        InputEditor inputEditor;

        // check whether the super.editor has a custom method for creating an Editor
        if (editor != null) {        	
            try {
                String sName = input.getName();
                sName = new String(sName.charAt(0) + "").toUpperCase() + sName.substring(1);
                sName = "create" + sName + "Editor";
                Class<?> _class = editor.getClass();
                Method method = _class.getMethod(sName);
                inputEditor = (InputEditor) method.invoke(editor);
System.err.println(inputEditor.getClass().getName() + " (CUSTOM EDITOR)");        
                return inputEditor;
            } catch (Exception e) {
                // ignore
            }
        }
        if (listItemNr < 0 && (List.class.isAssignableFrom(inputClass) ||
                (input.get() != null && input.get() instanceof List<?>))) {
            // handle list inputs
            if (listInputEditorMap.containsKey(inputClass)) {
                // use custom list input editor
                String sInputEditor = listInputEditorMap.get(inputClass);
                Constructor<?> con = Class.forName(sInputEditor).getConstructor(BeautiDoc.class);
                inputEditor = (InputEditor) con.newInstance(doc);

                //inputEditor = (InputEditor) Class.forName(sInputEditor).newInstance();
            } else {
                // otherwise, use generic list editor
                inputEditor = new ListInputEditor(doc);
            }
            ((ListInputEditor) inputEditor).setButtonStatus(buttonStatus);
        } else if (input.possibleValues != null) {
            // handle enumeration inputs
            inputEditor = new EnumInputEditor(doc);
        } else {
        	Class<?> inputClass2 = inputClass;
        	while (inputClass2 != null && !inputEditorMap.containsKey(inputClass2)) {
        		inputClass2 = inputClass2.getSuperclass(); 
        	}
        	if (inputClass2 == null) {
        		inputEditor = new YABBYObjectInputEditor(doc);
        	} else {
	            // handle Plugin-input with custom input editors
	            String sInputEditor = inputEditorMap.get(inputClass2);
	            
	            Constructor<?> con = Class.forName(sInputEditor).getConstructor(BeautiDoc.class);
	            inputEditor = (InputEditor) con.newInstance(doc);
        	}
        }        	
//    	} else if (inputEditorMap.containsKey(inputClass)) {
//            // handle Plugin-input with custom input editors
//            String sInputEditor = inputEditorMap.get(inputClass);
//            
//            Constructor<?> con = Class.forName(sInputEditor).getConstructor(BeautiDoc.class);
//            inputEditor = (InputEditor) con.newInstance(doc);
//            //inputEditor = (InputEditor) Class.forName(sInputEditor).newInstance(doc);
//            //} else if (inputClass.isEnum()) {
//            //    inputEditor = new EnumInputEditor();
//        } else {
//            // assume it is a general Plugin, so create a default Plugin input editor
//            inputEditor = new PluginInputEditor(doc);
//        }
        String sFullInputName = plugin.getClass().getName() + "." + input.getName();
        //System.err.println(sFullInputName);
        ExpandOption expandOption = bForceExpansion;
        if (doc.beautiConfig.inlinePlugins.contains(sFullInputName) || bForceExpansion == ExpandOption.TRUE_START_COLLAPSED) {
            expandOption = ExpandOption.TRUE;
            // deal with initially collapsed plugins
            if (doc.beautiConfig.collapsedPlugins.contains(sFullInputName) || bForceExpansion == ExpandOption.TRUE_START_COLLAPSED) {
                if (input.get() != null) {
                    Object o = input.get();
                    if (o instanceof ArrayList) {
                        for (Object o2 : (ArrayList<?>) o) {
                            if (o2 instanceof YABBYObject) {
                                String sID = ((YABBYObject) o2).getID();
                                if (!ListInputEditor.g_initiallyCollapsedIDs.contains(sID)) {
                                    ListInputEditor.g_initiallyCollapsedIDs.add(sID);
                                    ListInputEditor.g_collapsedIDs.add(sID);
                                }
                            }
                        }
                    } else if (o instanceof YABBYObject) {
                        String sID = ((YABBYObject) o).getID();
                        if (!ListInputEditor.g_initiallyCollapsedIDs.contains(sID)) {
                            ListInputEditor.g_initiallyCollapsedIDs.add(sID);
                            ListInputEditor.g_collapsedIDs.add(sID);
                        }
                    }
                }

            }
        }
        inputEditor.setDoc(doc);
        inputEditor.init(input, plugin, listItemNr, expandOption, bAddButtons);
        inputEditor.setBorder(BorderFactory.createEmptyBorder());
        inputEditor.getComponent().setVisible(true);
System.err.println(inputEditor.getClass().getName());        
        return inputEditor;
    } // createInputEditor

    /**
     * find plugins that could fit the input
     * @param input
     * @param parent plugin containing the input
     * @param sTabuList list of ids that are not allowed
     * @param doc
     * @return
     */
    
    public List<String> getAvailablePlugins(Input<?> input, YABBYObject parent, List<String> sTabuList, BeautiDoc doc) {

        //List<String> sPlugins = BeautiConfig.getInputCandidates(parent, input);
        List<String> sPlugins = new ArrayList<String>();
        if (sPlugins != null) {
            return sPlugins;
        }


        /* add ascendants to tabu list */
        if (sTabuList == null) {
            sTabuList = new ArrayList<String>();
        }
        if (!doc.isExpertMode()) {
            for (YABBYObject plugin : YABBYObjectPanel.listAscendants(parent, doc.pluginmap.values())) {
                sTabuList.add(plugin.getID());
            }
        }
        //System.err.println(sTabuList);

        /* collect all plugins in the system, that are not in the tabu list*/
        sPlugins = new ArrayList<String>();
        for (YABBYObject plugin : doc.pluginmap.values()) {
            if (input.getType().isAssignableFrom(plugin.getClass())) {
                boolean bIsTabu = false;
                if (sTabuList != null) {
                    for (String sTabu : sTabuList) {
                        if (sTabu.equals(plugin.getID())) {
                            bIsTabu = true;
                        }
                    }
                }
                if (!bIsTabu) {
                    try {
                        if (input.canSetValue(plugin, parent)) {
                            sPlugins.add(plugin.getID());
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
        /* add all plugin-classes of type assignable to the input */
        if (doc.isExpertMode()) {
            List<String> sClasses = AddOnManager.find(input.getType(), "beast");
            for (String sClass : sClasses) {
                try {
                    Object o = Class.forName(sClass).newInstance();
                    if (input.canSetValue(o, parent)) {
                        sPlugins.add("new " + sClass);
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return sPlugins;
    } // getAvailablePlugins

    /**
     * finds beauti templates that can create subnets that fit an input
     * @param input
     * @param parent
     * @param sTabuList
     * @param doc
     * @return
     */

    public List<BeautiSubTemplate> getAvailableTemplates(Input<?> input, YABBYObject parent, List<String> sTabuList, BeautiDoc doc) {
        Class<?> type = input.getType();
        List<BeautiSubTemplate> candidates = doc.beautiConfig.getInputCandidates(parent, input, type);
        if (input.getRule().equals(Validate.OPTIONAL)) {
            candidates.add(BeautiConfig.getNullTemplate(doc));
        }
        return candidates;
    }

}
