package hu.unimiskolc.iit.distsys;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.Job;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.unimiskolc.iit.distsys.interfaces.BasicJobScheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JobtoStaticVMsetRR implements BasicJobScheduler {

	private final int defaultFallback = 100;
	// how much time to wait if there were no VMs to send the current job
	private int fallback = defaultFallback;
	// what is the index of the vm in the vmset that we are using next time for
	// job submission
	private int currentId = 0;
	List<VirtualMachine> vmset;

	public void handleJobRequestArrival(final Job j) {
		final BasicJobScheduler bjs = this;
		int firstId = currentId;
		boolean unscheduled = true;
		do {
			VirtualMachine vm = vmset.get(currentId++);
			currentId = currentId % vmset.size();
			// determining if the VM is actually doing something or not
			if (vm.underProcessing.isEmpty() && vm.toBeAdded.isEmpty()) {
				try {
					// sending the task
					vm.newComputeTask(
							// the task will last exactly the amount of secs
							// specified in the job independently from the
							// actual resource requirements of the job
							j.getExectimeSecs()
									* vm.getPerTickProcessingPower() * 1000,
							ResourceConsumption.unlimitedProcessing,
							new ConsumptionEventAdapter() {
								@Override
								public void conComplete() {
									// marking the end of the job so the final
									// checks in TestRoundRobinJobSched will see
									// we completed the job on time
									j.completed();
								}
							});
					// Marking the start time of the job (again for the test
					// case)
					j.started();
					// resetting the wait time to its minimum
					fallback = defaultFallback;
					unscheduled = false;
				} catch (NetworkException ne) {
					throw new RuntimeException("Cannot start new task", ne);
				}
			}
			// determine if we should look for other VMs to host our current job
		} while (firstId != currentId && unscheduled);
		if (unscheduled) {
			// if we were not able to schedule the job right now, then let's
			// wait a little and try again
			new DeferredEvent(fallback) {
				@Override
				protected void eventAction() {
					bjs.handleJobRequestArrival(j);
				}
			};
			fallback *= 1.2;
		}
	}

	public void setupVMset(Collection<VirtualMachine> vms) {
		vmset = new ArrayList<VirtualMachine>(vms);
	}

	public void setupIaaS(IaaSService iaas) {
		// ignore
	}
}