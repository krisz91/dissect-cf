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

package at.ac.uibk.dps.cloud.simulator.test.simple.cloud;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.util.SeedSyncer;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.ac.uibk.dps.cloud.simulator.test.ConsumptionEventAssert;
import at.ac.uibk.dps.cloud.simulator.test.ConsumptionEventFoundation;

public class RepositoryTest extends ConsumptionEventFoundation {
	public final static long storageCapacity = 5000000000000L;
	public final static String storageObjectID = "SOID";
	Repository source, target;
	StorageObject so = new StorageObject(storageObjectID);

	@Before
	public void repoSetup() {
		HashMap<String, Integer> lm = NetworkNodeTest.setupALatencyMap();
		source = new Repository(storageCapacity, NetworkNodeTest.sourceName,
				NetworkNodeTest.inBW, NetworkNodeTest.outBW,
				NetworkNodeTest.diskBW, lm);
		target = new Repository(storageCapacity, NetworkNodeTest.targetName,
				NetworkNodeTest.inBW, NetworkNodeTest.outBW,
				NetworkNodeTest.diskBW, lm);
	}

	@Test(timeout = 100)
	public void checkConstruction() {
		Assert.assertTrue(
				"The toString of Repository should contain the node name",
				source.toString().contains(NetworkNodeTest.sourceName));
		Assert.assertEquals("Repositry size mismatch", storageCapacity,
				source.getMaxStorageCapacity());
	}

	private void registerWithCheck(StorageObject toRegister) {
		source.registerObject(toRegister);
		Assert.assertTrue("Storage object is not amongst contents", source
				.contents().contains(toRegister));
		Assert.assertEquals(
				"Returned with different storage object than expected",
				toRegister, source.lookup(toRegister.id));
	}

	private void emptyCheck() {
		Assert.assertFalse("Storage object is still amongst contents", source
				.contents().contains(so));
		Assert.assertTrue(
				"After deletion the storage object can still be reclaimed",
				source.lookup(storageObjectID) == null);
		Assert.assertEquals("The repository should be empty by now!", 0, source
				.contents().size());
	}
	
	@Test(timeout = 100)
	public void registrationTest() {
		registerWithCheck(so);
		source.registerObject(so);
		Assert.assertEquals(
				"Multiple registrations result in multiple storage objects in the repository!",
				1, source.contents().size());
		Assert.assertEquals(
				"The source repository is not reporting accurate size",
				source.getMaxStorageCapacity() - so.size,
				source.getFreeStorageCapacity());
		source.deregisterObject(so);
		emptyCheck();
		source.deregisterObject((StorageObject) null);
		source.deregisterObject((String) null);
		registerWithCheck(so);
		source.deregisterObject(storageObjectID);
		emptyCheck();
		Assert.assertFalse("It was possible to remove a non stored object!",
				source.deregisterObject(so));
	}
	
	@Test(timeout = 100)
	public void simpleTransferTest() throws NetworkException {
		final String extendedID = storageObjectID + ".ext";
		registerWithCheck(so);
		source.requestContentDelivery(storageObjectID, target,
				new ConsumptionEventAssert() {
					@Override
					public void conComplete() {
						super.conComplete();
						Assert.assertTrue(
								"Transferred stroage object has not got the same name",
								target.lookup(storageObjectID) != null);
					}

				});
		source.requestContentDelivery(storageObjectID, extendedID, target,
				new ConsumptionEventAssert() {
					@Override
					public void conComplete() {
						super.conComplete();
						Assert.assertTrue(
								"Transferred stroage object has not got the new name",
								target.lookup(extendedID) != null);
					}
				});
		source.duplicateContent(storageObjectID, extendedID,
				new ConsumptionEventAssert() {
					@Override
					public void conComplete() {
						super.conComplete();
						Assert.assertTrue(
								"Duplicated stroage object has not got the new name",
								source.lookup(extendedID) != null);
					}
				});
		source.requestContentDelivery(null, null, null, null);
		source.requestContentDelivery(null, null, null,
				new ConsumptionEventAssert());
		source.requestContentDelivery(null, null, target, null);
		source.requestContentDelivery(null, null, target,
				new ConsumptionEventAssert());
		source.requestContentDelivery(null, null, source, null);
		source.requestContentDelivery(null, null, source,
				new ConsumptionEventAssert());
		source.requestContentDelivery(null, extendedID, null, null);
		source.requestContentDelivery(null, extendedID, null,
				new ConsumptionEventAssert());
		source.requestContentDelivery(null, extendedID, target, null);
		source.requestContentDelivery(null, extendedID, target,
				new ConsumptionEventAssert());
		source.requestContentDelivery(null, extendedID, source, null);
		source.requestContentDelivery(null, extendedID, source,
				new ConsumptionEventAssert());
		source.requestContentDelivery(storageObjectID, null, null, null);
		source.requestContentDelivery(storageObjectID, null, null,
				new ConsumptionEventAssert());
		source.requestContentDelivery(storageObjectID, null, target, null);
		source.requestContentDelivery(storageObjectID, null, target,
				new ConsumptionEventAssert());
		source.requestContentDelivery(storageObjectID, null, source, null);
		source.requestContentDelivery(storageObjectID, null, source,
				new ConsumptionEventAssert());
		source.requestContentDelivery(storageObjectID, extendedID, null, null);
		source.requestContentDelivery(storageObjectID, extendedID, null,
				new ConsumptionEventAssert());
		source.requestContentDelivery(storageObjectID, extendedID, target, null);
		source.requestContentDelivery(storageObjectID, extendedID, target,
				new ConsumptionEventAssert());
		source.requestContentDelivery(storageObjectID, extendedID, source, null);
		source.requestContentDelivery(storageObjectID, extendedID, source,
				new ConsumptionEventAssert());
		Timed.simulateUntilLastEvent();
		Assert.assertEquals("Not all events finished", 6,
				ConsumptionEventAssert.hits.size());
	}

	@Test(timeout = 100)
	public void obeseTransferTest() throws NetworkException {
		final String obeseID = "Obese";
		final StorageObject obeseObject = new StorageObject(obeseID,
				storageCapacity, false);
		registerWithCheck(obeseObject);
		Assert.assertEquals("Source repository should be full", 0,
				source.getFreeStorageCapacity());
		Assert.assertFalse(
				"It was possible to register a storage object to a full repository!",
				source.registerObject(so));
		source.requestContentDelivery(obeseID, target,
				new ConsumptionEventAssert());
		Assert.assertFalse(
				"It was possible to deregister an under transfer storage object",
				source.deregisterObject(obeseObject));
		Assert.assertFalse(
				"It was possible to initiate a new transfer to an already promised full repository",
				source.requestContentDelivery(obeseID, target,
						new ConsumptionEventAssert()));
		Assert.assertFalse(
				"It was possible to register a storage object to a promised full repository!",
				target.registerObject(so));
		Timed.simulateUntilLastEvent();
		Assert.assertEquals("Target repository should be full", 0,
				target.getFreeStorageCapacity());
	}

	@Test(timeout = 200)
	public void registrationPerformance() {
		int vmNum = 10000;
		final long bandwidth = 111111111;
		Repository testRepo = new Repository(111111111111111111L, "Repo",
				bandwidth, bandwidth, bandwidth, new HashMap<String, Integer>());
		VirtualAppliance testVA = new VirtualAppliance("Test", 111, 0);
		VirtualAppliance[] vaBase = new VirtualAppliance[vmNum];
		for (int i = 0; i < vmNum; i++) {
			vaBase[i] = testVA.newCopy("AutoCreate" + i);
			testRepo.registerObject(vaBase[i]);
		}
		for (int i = 0; i < vmNum; i++) {
			testRepo.deregisterObject(vaBase[i]);
		}
	}

	private void genericCircularTest(int repoCount, long size, int[] fromIndex,
			int[] toIndex, long[] expectedTimings) throws NetworkException {
		SeedSyncer.resetCentral();
		final long bandwidth = 111111111;
		HashMap<String, Integer> latencyMap = new HashMap<String, Integer>();
		for (int i = 0; i < repoCount; i++) {
			latencyMap.put("Repo" + i, 6);
		}
		Repository[] repos = new Repository[repoCount];
		for (int i = 0; i < repoCount; i++) {
			repos[i] = new Repository(111111111111111111L, "Repo" + i,
					bandwidth, bandwidth, bandwidth, latencyMap);
		}
		StorageObject[] sos = new StorageObject[fromIndex.length];
		final long startTiming = Timed.getFireCount();
		for (int i = 0; i < fromIndex.length; i++) {
			sos[i] = new StorageObject("Test" + i, size, true);
			repos[fromIndex[i]].registerObject(sos[i]);
			repos[fromIndex[i]].requestContentDelivery(sos[i].id,
					repos[toIndex[i]], new ConsumptionEventAssert(
							expectedTimings[i] + startTiming));
		}
		Timed.simulateUntilLastEvent();
		Assert.assertEquals("Not enough transfers arrived", fromIndex.length,
				ConsumptionEventAssert.hits.size());
	}
	
	@Test(timeout=100)
	public void circularTransferTest() throws NetworkException {
		int trNum = 9;
		int repoCount = 3;
		int[] fromindex = new int[trNum];
		int[] toindex = new int[trNum];
		for (int i = 0; i < trNum; i++) {
			fromindex[i] = i % repoCount;
			toindex[i] = (i + 1) % repoCount;
		}
		genericCircularTest(repoCount, 1000000L, fromindex, toindex,
				new long[] { 17, 19, 36, 24, 8, 40, 8, 9, 9 });
	}

	@Test(timeout=100)
	public void secondCircularTransferTest() throws NetworkException {
		genericCircularTest(3, 10000000L, new int[] { 0, 1, 2, 2 }, new int[] {
				1, 0, 1, 0 }, new long[] { 103, 219, 270, 247 });
	}
}
