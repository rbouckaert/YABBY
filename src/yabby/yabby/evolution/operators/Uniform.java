/*
* File Uniform.java
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
/*
 * UniformOperator.java
 *
 * Copyright (C) 2002-2006 Alexei Drummond and Andrew Rambaut
 *
 * This file is part of BEAST.
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

package yabby.evolution.operators;

import yabby.core.Description;
import yabby.evolution.tree.Tree;
import yabby.evolution.tree.Node;
import yabby.util.Randomizer;


@Description("Randomly selects true internal tree node (i.e. not the root) and move node height uniformly in interval " +
        "restricted by the nodes parent and children.")
public class Uniform extends TreeOperator {

    @Override
    public void initAndValidate() {
    }

    /**
     * change the parameter and return the hastings ratio.
     *
     * @return log of Hastings Ratio, or Double.NEGATIVE_INFINITY if proposal should not be accepted *
     */
    @Override
    public double proposal() {
        final Tree tree = treeInput.get(this);

        // randomly select internal node
        final int nNodeCount = tree.getNodeCount();
        Node node;
        do {
            final int iNodeNr = nNodeCount / 2 + 1 + Randomizer.nextInt(nNodeCount / 2);
            node = tree.getNode(iNodeNr);
        } while (node.isRoot() || node.isLeaf());
        final double fUpper = node.getParent().getHeight();
        final double fLower = Math.max(node.getLeft().getHeight(), node.getRight().getHeight());
        final double newValue = (Randomizer.nextDouble() * (fUpper - fLower)) + fLower;
        node.setHeight(newValue);

        return 0.0;
    }

}
