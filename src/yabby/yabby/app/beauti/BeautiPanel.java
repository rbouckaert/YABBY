package yabby.app.beauti;


import java.awt.*;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import yabby.app.beauti.BeautiPanelConfig.Partition;
import yabby.app.draw.InputEditor;
import yabby.app.draw.InputEditor.ExpandOption;
import yabby.core.YABBYObject;
import yabby.core.Input;




/**
 * panel making up each of the tabs in Beauti *
 */
public class BeautiPanel extends JPanel implements ListSelectionListener {
    private static final long serialVersionUID = 1L;
    public final static String ICONPATH = "beast/app/beauti/";

    static int partitionListPreferredWidth = 120;

    private JSplitPane splitPane;

    /**
     * document that this panel applies to *
     */
    BeautiDoc doc;

    public BeautiDoc getDoc() {
        return doc;
    }

    /**
     * configuration for this panel *
     */
    public BeautiPanelConfig config;

    /**
     * panel number *
     */
    int iPanel;

    /**
     * partition currently on display *
     */
    public int iPartition = 0;

    /**
     * box containing the list of partitions, to make (in)visible on update *
     */
    JComponent partitionComponent;
    /**
     * list of partitions in m_listBox *
     */
    JList listOfPartitions;
    /**
     * model for m_listOfPartitions *
     */
    DefaultListModel listModel;


    /**
     * component containing main input editor *
     */
    Component centralComponent = null;

    public BeautiPanel() {
    }

    public BeautiPanel(int iPanel, BeautiDoc doc, BeautiPanelConfig config) throws Exception {
        this.doc = doc;
        this.iPanel = iPanel;

//        SmallButton helpButton2 = new SmallButton("?", true);
//        helpButton2.setToolTipText("Show help for this plugin");
//        helpButton2.addActionListener(new ActionListener() {
//            // implementation ActionListener
//            public void actionPerformed(ActionEvent e) {
//                setCursor(new Cursor(Cursor.WAIT_CURSOR));
//                HelpBrowser b = new HelpBrowser(m_config.getType());
//                b.setSize(800, 800);
//                b.setVisible(true);
//                b.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//            }
//        });
//    	add(helpButton2);


        setLayout(new BorderLayout());

        this.config = config;
        if (this.config.hasPartition() != Partition.none && 
        		doc.getPartitions(config.bHasPartitionsInput.get().toString()).size() > 1) {
        	splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        	add(splitPane,BorderLayout.CENTER);
        } else {
        	splitPane = null;
        }

//        PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
//            public void propertyChange(PropertyChangeEvent changeEvent) {
//                JSplitPane sourceSplitPane = (JSplitPane) changeEvent.getSource();
//                String propertyName = changeEvent.getPropertyName();
//                if (propertyName.equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY)) {
//                    partitionListPreferredWidth = sourceSplitPane.getDividerLocation();
//
//                    Integer priorLast = (Integer) changeEvent.getOldValue();
//                    System.out.println("Prior last: " + priorLast);
//                    System.out.println("new: " + partitionListPreferredWidth);
//
//                }
//            }
//        };
//        splitPane.addPropertyChangeListener(propertyChangeListener);

        refreshPanel();
        addPartitionPanel(this.config.hasPartition(), iPanel);


        setOpaque(false);
    } // c'tor

    void addPartitionPanel(Partition bHasPartition, int iPanel) {
        Box box = Box.createVerticalBox();
        if (splitPane != null && bHasPartition != Partition.none) {
            box.add(createList());
        } else {
        	return;
        }
        box.add(Box.createVerticalGlue());
        box.add(new JLabel(getIcon(iPanel, config)));

        //if (splitPane.getLeftComponent() != null) {
        //    Dimension d = splitPane.getLeftComponent().getSize();
        //}

        splitPane.add(box, JSplitPane.LEFT);
        if (listOfPartitions != null) {
            listOfPartitions.setSelectedIndex(iPartition);
        }
    }

    JComponent createList() {
        partitionComponent = new JPanel();
        partitionComponent.setLayout(new BorderLayout());
        JLabel partitionLabel = new JLabel("Partition");
        partitionLabel.setHorizontalAlignment(JLabel.CENTER);
        partitionComponent.add(partitionLabel, BorderLayout.NORTH);
        listModel = new DefaultListModel();
        listOfPartitions = new JList(listModel);
        listOfPartitions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        Dimension size = new Dimension(partitionListPreferredWidth, 300);
        //listOfPartitions.setFixedCellWidth(120);
//    	m_listOfPartitions.setSize(size);
        listOfPartitions.setPreferredSize(size);
//    	m_listOfPartitions.setMinimumSize(size);
//    	m_listOfPartitions.setBounds(0, 0, 100, 100);

        listOfPartitions.addListSelectionListener(this);
        updateList();
        listOfPartitions.setBorder(new BevelBorder(BevelBorder.RAISED));
        partitionComponent.add(listOfPartitions, BorderLayout.CENTER);
        partitionComponent.setBorder(new EtchedBorder());
        return partitionComponent;
    }

    public void updateList() {
        if (listModel == null) {
            return;
        }
        listModel.clear();
        if (listModel.size() > 0) {
        	// this is a weird bit of code, since listModel.clear should ensure that size()==0, but it doesn't
        	return;
        }
        for (YABBYObject partition : doc.getPartitions(config.bHasPartitionsInput.get().toString())) {
            String sPartition = partition.getID();
            sPartition = sPartition.substring(sPartition.lastIndexOf('.') + 1);
            listModel.addElement(sPartition);
        }
        if (iPartition >= 0 && listModel.size() > 0)
            listOfPartitions.setSelectedIndex(iPartition);
    }

    public static ImageIcon getIcon(int iPanel, BeautiPanelConfig config) {
        String sIconLocation = ICONPATH + iPanel + ".png";
        if (config != null) {
            sIconLocation = ICONPATH + config.getIcon();
        }
        return getIcon(sIconLocation);
    }
    
    public static ImageIcon getIcon(String sIconLocation) {
        try {
            URL url = (URL) ClassLoader.getSystemResource(sIconLocation);
            if (url == null) {
                System.err.println("Cannot find icon " + sIconLocation);
                return null;
            }
            ImageIcon icon = new ImageIcon(url);
            return icon;
        } catch (Exception e) {
            System.err.println("Cannot load icon " + sIconLocation + " " + e.getMessage());
            return null;
        }

    }

    // AR remove globals (doesn't seem to be used anywhere)...
//	static BeautiPanel g_currentPanel = null;

    public void refreshPanel() throws Exception {
        if (doc.alignments.size() == 0) {
            refreshInputPanel();
            return;
        }
        doc.scrubAll(true, false);

        // toggle splitpane
        if (splitPane == null && config.hasPartition() != Partition.none && 
        		doc.getPartitions(config.bHasPartitionsInput.get().toString()).size() > 1) {        	
        	splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        	add(splitPane,BorderLayout.CENTER);
        	addPartitionPanel(config.hasPartition(), iPanel);
        }
        if (splitPane != null && (config.hasPartition() == Partition.none || 
            		doc.getPartitions(config.bHasPartitionsInput.get().toString()).size() <= 1)) {
        	remove(splitPane);
        	splitPane = null;
        }

        refreshInputPanel();
        if (partitionComponent != null && config.getType() != null) {
            partitionComponent.setVisible(doc.getPartitions(config.getType()).size() > 1);
        }
        

        
//		g_currentPanel = this;
    }

    void refreshInputPanel(YABBYObject plugin, Input<?> input, boolean bAddButtons, InputEditor.ExpandOption bForceExpansion) throws Exception {
        if (centralComponent != null) {
            remove(centralComponent);
        }
        if (input != null && input.get() != null && input.getType() != null) {
            InputEditor.ButtonStatus bs = config.buttonStatusInput.get();
            InputEditor inputEditor = doc.getInpuEditorFactory().createInputEditor(input, plugin, bAddButtons, bForceExpansion, bs, null, doc);
            
            //Box box = Box.createVerticalBox();
            //box.add(inputEditor.getComponent());
            // RRB: is there a better way than just pooring in glue at the bottom?
            //for (int i = 0; i < 30; i++) {

            //box.add(Box.createVerticalStrut(1024 - ((Component)inputEditor).getPreferredSize().height));
            //}
            //JScrollPane scroller = new JScrollPane(box);
            JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.add(inputEditor.getComponent(), BorderLayout.NORTH);
            //p.add(Box.createVerticalStrut(1024 - inputEditor.getComponent().getPreferredSize().height), BorderLayout.SOUTH);

            
            //p.setPreferredSize(new Dimension(1024,1024));
            JScrollPane scroller = new JScrollPane(p);
            centralComponent = scroller;
        } else {
            centralComponent = new JLabel("Nothing to be specified");
        }
        if (splitPane != null) {
        	splitPane.add(centralComponent, JSplitPane.RIGHT);
        } else {
        	add(centralComponent);
        }
    }

    void refreshInputPanel() throws Exception {
    	doc.currentInputEditors.clear();
        InputEditor.Base.g_nLabelWidth = config.nLabelWidthInput.get();
        YABBYObject plugin = config;
        Input<?> input = config.resolveInput(doc, iPartition);

        boolean bAddButtons = config.addButtons();
        ExpandOption bForceExpansion = config.forceExpansion();
        refreshInputPanel(plugin, input, bAddButtons, bForceExpansion);
    }


//    public static boolean soundIsPlaying = false;
//
//    public static synchronized void playSound(final String url) {
//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    synchronized (this) {
//                        if (soundIsPlaying) {
//                            return;
//                        }
//                        soundIsPlaying = true;
//                    }
//                    Clip clip = AudioSystem.getClip();
//                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream("/beast/app/beauti/" + url));
//                    clip.open(inputStream);
//                    clip.start();
//                    Thread.sleep(500);
//                    synchronized (this) {
//                        soundIsPlaying = false;
//                    }
//                } catch (Exception e) {
//                    soundIsPlaying = false;
//                    System.err.println(e.getMessage());
//                }
//            }
//        }).start();
//    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        //System.err.print("BeautiPanel::valueChanged " + m_iPartition + " => ");
        if (e != null) {
            config.sync(iPartition);
            if (listOfPartitions != null) {
                iPartition = Math.max(0, listOfPartitions.getSelectedIndex());
            }
        }
//        BeautiPanel.playSound("woosh.wav");
        //System.err.println(m_iPartition);
        try {
            refreshPanel();

            centralComponent.repaint();
            repaint();

            // hack to ensure m_centralComponent is repainted RRB: is there a better way???
            if (Frame.getFrames().length == 0) {
                // happens at startup
                return;
            }
            Frame frame = Frame.getFrames()[Frame.getFrames().length - 1];
            frame.setSize(frame.getSize());
            //Frame frame = frames[frames.length - 1];
//			Dimension size = frames[frames.length-1].getSize();
//			frames[frames.length-1].setSize(size);

//			m_centralComponent.repaint();
//			m_centralComponent.requestFocusInWindow();
            centralComponent.requestFocus();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

} // class BeautiPanel
