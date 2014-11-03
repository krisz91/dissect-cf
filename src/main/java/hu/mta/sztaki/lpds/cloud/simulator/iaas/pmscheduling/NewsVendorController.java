package hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.Scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Krisztián Varga
 */
public class NewsVendorController extends SchedulingDependentMachines {

	public static List<Long> receivedTimes = new ArrayList<Long>();

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
		final int numberOfRequestedPM = calculateNextSeasonJobNumber() - parent.runningMachines.size();
		if (numberOfRequestedPM>0) {
			for (int i = 0; i < numberOfRequestedPM; i++) {
				final PhysicalMachine n = parent.machines.get(i);
				if (PhysicalMachine.ToOfforOff.contains(n.getState())) {
					n.turnon();
					break;
				}
			}
		}
	}


	private int calculateNextSeasonJobNumber() {
		return 5;
	}
}
