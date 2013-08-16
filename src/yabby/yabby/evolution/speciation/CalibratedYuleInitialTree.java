/*
 * CoalescentSimulator.java
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

package yabby.evolution.speciation;


import java.util.ArrayList;
import java.util.List;

import yabby.core.Description;
import yabby.core.Input;
import yabby.core.StateNode;
import yabby.core.StateNodeInitialiser;
import yabby.evolution.tree.Tree;


@Description("This class provides the basic engine for coalescent simulation of a given demographic model over a given time period.")
public class CalibratedYuleInitialTree extends Tree implements StateNodeInitialiser {

    public Input<List<CalibrationPoint>> calibrations =
            new Input<List<CalibrationPoint>>("calibrations", "Set of calibrated nodes", new ArrayList<CalibrationPoint>(),
                    Input.Validate.REQUIRED);

    @Override
    public void initAndValidate() throws Exception {
        super.initAndValidate();
        initStateNodes();
    }

    @Override
    public void initStateNodes() throws Exception {
        // Would have been nice to use CalibratedYuleModel plugin directly, but at this point
        // it does not exist since the tree being initialized is one of its arguments. So, build a temporary
        // one using the initializer tree.

        final List<CalibrationPoint> cals = calibrations.get();

        final CalibratedYuleModel cym = new CalibratedYuleModel();
        for( final CalibrationPoint cal : cals ) {
          cym.setInputValue("calibrations", cal);
        }
        cym.setInputValue("tree", this);
        cym.setInputValue("type", CalibratedYuleModel.Type.NONE) ; //"none");
        cym.initAndValidate();

        final Tree t = cym.compatibleInitialTree();
        m_initial.get().assignFromWithoutID(t);
    }

    @Override
    public List<StateNode> getInitialisedStateNodes() {
        final List<StateNode> stateNodes = new ArrayList<StateNode>();
        stateNodes.add(m_initial.get());
        return stateNodes;
    }
}