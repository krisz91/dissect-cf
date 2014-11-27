package hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling.newsvendor.NewsVendor;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.Scheduler;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.HashedMap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Krisztián Varga
 */
public class NewsVendorController extends SchedulingDependentMachines {

	public static List<Long> receivedTimes = new ArrayList<Long>();
	public static HashedMap<Integer,Integer> pmCounts = new HashedMap<Integer, Integer>();
	public static int currentDay = 0;

	public NewsVendorController(final IaaSService parent) {
		super(parent);
	}

	@Override
	protected Scheduler.QueueingEvent getQueueingEvent() {
		return new Scheduler.QueueingEvent() {
			@Override
			public void queueingStarted() {
				if (noMachineTurningOn) {
					turnOnAMachine();
				}
			}
		};
	}

	protected void turnOnAMachine() {
		int numberOfRequestedPM = parent.machines.size();
		if(pmCounts.size()>0) {
			numberOfRequestedPM = calculateNextSeasonPMNumber() - parent.runningMachines.size();
		}

		if (parent.runningMachines.size() != numberOfRequestedPM) {
			for (int i = 0; i < numberOfRequestedPM; i++) {
				final PhysicalMachine n = parent.machines.get(i);
				if (PhysicalMachine.ToOfforOff.contains(n.getState())) {
					Integer maxPm = pmCounts.get(currentDay);
					if (maxPm == null) {
						pmCounts.put(currentDay, parent.runningMachines.size());
					} else {
						if (maxPm.intValue() < parent.runningMachines.size()) {
							pmCounts.put(currentDay, parent.runningMachines.size());
						}
					}
					n.turnon();
					break;
				}
			}
		}

	}


	private int calculateNextSeasonPMNumber() {

		HashedMap<Integer,Integer> periods = new HashedMap<Integer,Integer>();

		MapIterator<Integer,Integer> mapIterator = pmCounts.mapIterator();

		while(mapIterator.hasNext()) {
			mapIterator.next();
			Integer pmCount = mapIterator.getValue();

			periods.put(pmCount,3);
		}

		NewsVendor newsVendor = new NewsVendor(periods,1,1,1,1);
		int numberOfPM  = newsVendor.calculate();
		return numberOfPM;
	}
}
