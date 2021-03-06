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

package at.ac.uibk.dps.cloud.simulator.test;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;

import java.util.ArrayList;

import org.junit.Assert;

public class ConsumptionEventAssert extends ConsumptionEventAdapter {
	public static ArrayList<Long> hits = new ArrayList<Long>();
	long arrivedAt = -1;
	final long expectedTime;
	final boolean failOnCancel;

	public ConsumptionEventAssert(final long expectedTime,
			final boolean failOnCancel) {
		this.expectedTime = expectedTime;
		this.failOnCancel = failOnCancel;
	}

	public ConsumptionEventAssert(final boolean failOnCancel) {
		this(-1L, failOnCancel);
	}

	public ConsumptionEventAssert(final long expectedTime) {
		this(expectedTime, false);
	}

	public ConsumptionEventAssert() {
		this(-1L, false);
	}

	private void updateArrivedAt() {
		arrivedAt = Timed.getFireCount();
	}

	public long getArrivedAt() {
		return arrivedAt;
	}

	@Override
	public void conComplete() {
		if (expectedTime > 0) {
			Assert.assertEquals("Network transfer completed at wrong time",
					expectedTime, Timed.getFireCount());
		}
		Assert.assertFalse(
				"Should not receive consumption events multiple times",
				isCancelled() || isCompleted());
		super.conComplete();
		updateArrivedAt();
		hits.add(arrivedAt);
	}

	@Override
	public void conCancelled(ResourceConsumption problematic) {
		if (failOnCancel) {
			Assert.fail("Consumption got cancelled before transfer completed");
		}
		Assert.assertFalse(
				"Should not receive consumption events multiple times",
				isCancelled() || isCompleted());
		super.conCancelled(problematic);
		updateArrivedAt();
	}
}
