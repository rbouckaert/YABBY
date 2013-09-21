package yabby.app.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import yabby.app.beauti.BeautiPanel;
import yabby.util.AddOnManager;




/**
 * launch applications specific to add-ons installed, for example utilities for
 * post-processing add-on specific data.
 */
public class AddOnAppLauncher extends JDialog {
	private static final long serialVersionUID = 1L;

	JPanel panel;
	DefaultListModel model = new DefaultListModel();
	JList list;

	public AddOnAppLauncher() {
		try {
			AddOnManager.loadExternalJars();
		} catch (Exception e) {
			// ignore
		}
		panel = new JPanel();
		add(BorderLayout.CENTER, panel);
		setTitle("YABBY Add-On Application Launcher");
		Component pluginListBox = createList();
		panel.add(pluginListBox);
		Box buttonBox = createButtonBox();
		add(buttonBox, BorderLayout.SOUTH);

		Dimension dim = panel.getPreferredSize();
		Dimension dim2 = buttonBox.getPreferredSize();
		setSize(dim.width + 10, dim.height + dim2.height + 30);
	}

	private Component createList() {
		Box box = Box.createVerticalBox();
		box.add(new JLabel("List of available Add-on applications"));
		list = new JList(model);
		list.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel label = (JLabel) super
						.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				label.setIcon(((AddOnApp) value).icon);
				return label;
			}
		});
		resetList();

		JScrollPane pane = new JScrollPane(list);
		box.add(pane);
		return box;
	}

	private void resetList() {
		model.clear();
		try {
			List<AddOnApp> addOns = getAddOnApps();
			for (AddOnApp addOnApp : addOns) {
				model.addElement(addOnApp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		list.setSelectedIndex(0);
	}

	private Box createButtonBox() {
		Box box = Box.createHorizontalBox();
		box.add(Box.createGlue());
		JButton launchButton = new JButton("Launch");
		launchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AddOnApp addOnApp = (AddOnApp) list.getSelectedValue();
				if (addOnApp != null) {
					try {
						new AddOnAppThread(addOnApp).start();

					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, "Launch failed because: " + ex.getMessage());
					}
				}
			}
		});
		box.add(launchButton);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		box.add(Box.createGlue());
		box.add(closeButton);
		box.add(Box.createGlue());
		return box;
	}

	List<AddOnApp> getAddOnApps() {
		List<AddOnApp> addOnApps = new ArrayList<AddOnApp>();
		List<String> dirs = AddOnManager.getBeastDirectories();
		for (String sJarDir : dirs) {
			File versionFile = new File(sJarDir + "/version.xml");
			if (versionFile.exists() && versionFile.isFile()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				Document doc;
				try {
					doc = factory.newDocumentBuilder().parse(versionFile);
					doc.normalize();
					// get addonapp info from version.xml
					Element addon = doc.getDocumentElement();
					NodeList nodes = doc.getElementsByTagName("addonapp");
					for (int j = 0; j < nodes.getLength(); j++) {
						Element addOnAppElement = (Element) nodes.item(j);
						AddOnApp addOnApp = new AddOnApp();
						addOnApp.className = addOnAppElement.getAttribute("class");
						addOnApp.description = addOnAppElement.getAttribute("description");
						addOnApp.defaultArguments = addOnAppElement.getAttribute("args");
						String iconLocation = addOnAppElement.getAttribute("icon");
						addOnApp.icon = BeautiPanel.getIcon(iconLocation);
						addOnApps.add(addOnApp);
					}
				} catch (Exception e) {
					// ignore
					System.err.println(e.getMessage());
				}
			}
		}
		return addOnApps;
	}

	/**
	 * add on application information reguired for launching the app and
	 * displaying in list box
	 **/
	class AddOnApp {
		String description;
		String className;
		String defaultArguments;
		ImageIcon icon;

		@Override
		public String toString() {
			return description;
		}
	}

	/** thread for launching add on application **/
	class AddOnAppThread extends Thread {
		AddOnApp addOnApp;

		AddOnAppThread(AddOnApp addOnApp) {
			this.addOnApp = addOnApp;
		}

		@Override
		public void run() {
			try {
				String command = "java -cp " + System.getProperty("java.class.path") +
							" beast.app.tools.AddOnAppLauncher " +
						addOnApp.className + " " + 
						addOnApp.defaultArguments;
				System.out.println(command);
				Process p = Runtime.getRuntime().exec(command);
				BufferedReader pout = new BufferedReader((new InputStreamReader(p.getInputStream())));
				BufferedReader perr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String line;
				while ((line = pout.readLine()) != null) {
					System.out.println(line);
				}
				pout.close();
				while ((line = perr.readLine()) != null) {
					System.err.println(line);
				}
				perr.close();
				p.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			AddOnAppLauncher dlg = new AddOnAppLauncher();
			dlg.setVisible(true);
		} else {
			// invoke add on application
			try {
				AddOnManager.loadExternalJars();

				// call main method through reflection
				// with default arguments
				String className = args[0];
				Class<?> c = Class.forName(className);
				Class<?>[] argTypes = new Class[] { String[].class };
				Method main = c.getDeclaredMethod("main", argTypes);
				String[] args2 = new String[args.length-1];
				for (int i = 1; i < args.length; i++) {
					args2[i-1] = args[i];
				}
				main.invoke(null, (Object) args2);
			} catch (Exception err) {
				err.printStackTrace();
			}

		}
	}
}
