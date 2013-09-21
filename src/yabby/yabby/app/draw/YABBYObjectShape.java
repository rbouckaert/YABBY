/*
* File PluginShape.java
*
* Copyright (C) 2010 Remco Bouckaert remco@cs.auckland.ac.nz
*
* This file is part of BEAST2.
* See the NOTICE file distributed with this work for additional
* information regarding copyright ownership and licensing.
*
* BEAST is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
*  BEAST is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with BEAST; if not, write to the
* Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
* Boston, MA  02110-1301  USA
*/
package yabby.app.draw;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.w3c.dom.Node;

import yabby.core.YABBYObject;
import yabby.core.Input;
import yabby.util.Randomizer;






public class YABBYObjectShape extends Shape {
    static Font g_PluginFont = new Font("arial", Font.PLAIN, 11);
    public yabby.core.YABBYObject m_plugin;
    List<InputShape> m_inputs;


    public YABBYObjectShape() {
        super();
        m_fillcolor = new Color(Randomizer.nextInt(256), 128 + Randomizer.nextInt(128), Randomizer.nextInt(128));
    }

    public YABBYObjectShape(YABBYObject plugin, Document doc) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        super();
        m_plugin = plugin;
        m_fillcolor = new Color(Randomizer.nextInt(256), 128 + Randomizer.nextInt(128), Randomizer.nextInt(128));
        init(plugin.getClass().getName(), doc);
    }

    public YABBYObjectShape(Node node, Document doc, boolean bReconstructPlugins) {
        parse(node, doc, bReconstructPlugins);
    }

    public void init(String sClassName, Document doc) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    	m_doc = doc;
        if (m_plugin == null) {
            m_plugin = (yabby.core.YABBYObject) Class.forName(sClassName).newInstance();
        }
        m_inputs = new ArrayList<InputShape>();
        if (m_plugin.getID() == null) {
        	String sID = m_plugin.getClass().getName();
        	sID = sID.substring(sID.lastIndexOf('.') + 1);
        	m_plugin.setID(sID);
        }
        //System.err.println("\n>>>>" + m_plugin.getID());        
        if (m_plugin.getID().equals("mcmc")) {
        	int h = 3;
        }
        List<Input<?>> sInputs = m_plugin.listInputs();
        for (Input<?> input_ : sInputs) {
			String longInputName = m_plugin.getClass().getName() + "." + input_.getName(); 
			//System.err.print(longInputName);
        	if (doc.showAllInputs() ||
        			!doc.tabulist.contains(longInputName) && 
        			input_.get() != null && (
        			(input_.get() instanceof List && ((List<?>)input_.get()).size()>0) ||  
        			!input_.get().equals(input_.defaultValue))) {
	            InputShape input = new InputShape(input_);
	            input.setPluginShape(this);
	            input.m_fillcolor = m_fillcolor;
	            input.m_w = 10;
	            doc.addNewShape(input);
	            m_inputs.add(input);
        		//System.err.println(" shown");
        	} else {
        		//System.err.println(" skipped");
        	}
        }
        m_h = Math.max(40, m_inputs.size() * 12);
        adjustInputs();
    } // setClassName

    // find input shape associated with input with name sLabel
    InputShape getInputShape(String sLabel) {
        for (InputShape shape : m_inputs) {
            String sLabel2 = shape.getLabel();
            if (sLabel2 != null) {
                if (sLabel2.contains("=")) {
                    sLabel2 = sLabel2.substring(0, sLabel2.indexOf('='));
                }
                if (sLabel2.equals(sLabel)) {
                    return shape;
                }
            }
        }
        return null;
    }

    /**
     * set coordinates of inputs based on location of this PluginShape
     */
    void adjustInputs() {
        if (m_plugin != null) {
            try {
                List<Input<?>> inputs = m_plugin.listInputs();
                for (int i = 0; i < m_inputs.size(); i++) {
                    InputShape input = m_inputs.get(i);
                    //input.m_input = inputs.get(i);
                    int nOffset = i * m_h / (m_inputs.size()) + m_h / (2 * (m_inputs.size()));
                    input.m_x = m_x - input.m_w;
                    input.m_y = m_y + nOffset;
                    //input.m_w = 10;
                    input.m_h = 10;
                    input.m_fillcolor = m_fillcolor;
                    input.m_nPenWidth = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void draw(Graphics2D g, JPanel panel) {
        if (m_bFilled) {
            GradientPaint m_gradientPaint = new GradientPaint(new Point(m_x, m_y), Color.WHITE, new Point(m_x + m_w, m_y + m_h), m_fillcolor);
            g.setPaint(m_gradientPaint);
            g.fillOval(m_x, m_y, m_w, m_h);
            g.fillRect(m_x, m_y, m_w / 2, m_h);
        } else {
            g.setColor(m_fillcolor);
            g.drawLine(m_x, m_y, m_x, m_y + m_h);
            g.drawLine(m_x, m_y, m_x + m_w / 2, m_y);
            g.drawLine(m_x, m_y + m_h, m_x + m_w / 2, m_y + m_h);
            g.drawArc(m_x, m_y, m_w, m_h, 0, 90);
            g.drawArc(m_x, m_y, m_w, m_h, 0, -90);
        }
        g.setStroke(new BasicStroke(m_nPenWidth));
        g.setColor(m_pencolor);
        g.setFont(g_PluginFont);
        drawLabel(g);
        adjustInputs();
    }

    @Override
    void parse(Node node, Document doc, boolean bReconstructPlugins) {
        super.parse(node, doc, bReconstructPlugins);
        if (bReconstructPlugins) {
            if (node.getAttributes().getNamedItem("class") != null) {
                String sClassName = node.getAttributes().getNamedItem("class").getNodeValue();
                try {
                    m_plugin = (yabby.core.YABBYObject) Class.forName(sClassName).newInstance();
                    m_plugin.setID(m_sID);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
            if (node.getAttributes().getNamedItem("inputids") != null) {
                String sInputIDs = node.getAttributes().getNamedItem("inputids").getNodeValue();
                String[] sInputID = sInputIDs.split(" ");
                m_inputs = new ArrayList<InputShape>();
                try {
                    List<Input<?>> inputs = m_plugin.listInputs();
                    for (int i = 0; i < sInputID.length; i++) {
                        InputShape ellipse = (InputShape) doc.findObjectWithID(sInputID[i]);
                        m_inputs.add(ellipse);
                        ellipse.setPluginShape(this);
                        //ellipse.m_input = inputs.get(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String getXML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<" + Document.PLUGIN_SHAPE_ELEMENT);
        buf.append(" class='");
        buf.append(m_plugin.getClass().getName());
        buf.append("'");
        buf.append(" inputids='");
        for (int i = 0; i < m_inputs.size(); i++) {
            buf.append(m_inputs.get(i).getID());
            buf.append(' ');
        }
        buf.append("'");

        buf.append(getAtts());
        buf.append(">\n");
        buf.append("</" + Document.PLUGIN_SHAPE_ELEMENT + ">");
        return buf.toString();
    }

    @Override
    void assignFrom(Shape other) {
        super.assignFrom(other);
        m_plugin.setID(other.m_sID);
    }

    @Override
    boolean intersects(int nX, int nY) {
        return super.intersects(nX, nY);
    }

    @Override
    String getLabel() {
        return getID();
    }

    @Override
    String getID() {
        if (m_plugin == null) {
            return null;
        }
        return m_plugin.getID();
    }

    @Override
    void toSVG(PrintStream out) {
        out.println("<defs>");
        out.println("  <linearGradient id='grad" + getID() + "' x1='0%' y1='0%' x2='100%' y2='100%'>");
        out.println("    <stop offset='0%' style='stop-color:rgb(255,255,255);stop-opacity:1' />");
        out.println("    <stop offset='100%' style='stop-color:rgb(" + m_fillcolor.getRed() + "," + m_fillcolor.getGreen() + "," + m_fillcolor.getBlue() + ");stop-opacity:1' />");
        out.println("  </linearGradient>");
        out.println("</defs>");
        out.print("<path id='" + getID() + "' d='M " + m_x + " " + (m_y + m_h) + " l " + m_w / 2 + " 0 ");
        out.print(" a " + m_w / 2 + " " + (-m_h / 2) + " 0 0,0 0," + (-m_h) + " l " + (-m_w / 2) + " 0 z'");
        out.println(" fill='url(#grad" + getID() + ")' />");
        drawSVGString(out, g_PluginFont, m_pencolor, "middle");
    }
} // class Function
