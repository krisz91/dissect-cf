/*
 *  ========================================================================
 *  DIScrete event baSed Energy Consumption simulaTor 
 *    					             for Clouds and Federations (DISSECT-CF)
 *  ========================================================================
 *  
 *  This file is part of DISSECT-CF.
 *  
 *  DISSECT-CF is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at
 *  your option) any later version.
 *  
 *  DISSECT-CF is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 *  General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with DISSECT-CF.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  (C) Copyright 2014, Gabor Kecskemeti (gkecskem@dps.uibk.ac.at,
 *   									  kecskemeti.gabor@sztaki.mta.hu)
 */

package hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;

import java.util.Collections;
import java.util.Comparator;

public class SmallestFirstScheduler extends FirstFitScheduler {
	public static final Comparator<QueueingData> vmQueueSmallestFirstComparator = new Comparator<QueueingData>() {
		@Override
		public int compare(QueueingData o1, QueueingData o2) {
			final int compRC = o1.cumulativeRC.compareTo(o2.cumulativeRC);
			return compRC == 0 ? Long.signum(o1.receivedTime - o2.receivedTime)
					: compRC;
		}
	};

	public SmallestFirstScheduler(final IaaSService parent) {
		super(parent);
	}

	@Override
	protected void scheduleQueued() {
		Collections.sort(queue, vmQueueSmallestFirstComparator);
		super.scheduleQueued();
	}
}
