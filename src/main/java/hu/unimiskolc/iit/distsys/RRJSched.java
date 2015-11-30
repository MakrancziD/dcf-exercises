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

public class RRJSched implements BasicJobScheduler 
{
	private final int defaultFallback = 100;
	private int fallback = defaultFallback;
	
	private int currentId = 0;
	List<VirtualMachine> vmset;
	
	public void handleJobRequestArrival(final Job j) 
	{
		final BasicJobScheduler bjs = this;
		int firstId = currentId;
		boolean unscheduled = true;
		
		do 
		{
			VirtualMachine vm = vmset.get(currentId++);
			currentId = currentId % vmset.size();

			if (vm.underProcessing.isEmpty() && vm.toBeAdded.isEmpty()) 
			{
				try 
				{
					vm.newComputeTask(
							j.getExectimeSecs() * vm.getPerTickProcessingPower() * 1000, 
							ResourceConsumption.unlimitedProcessing,
							new ConsumptionEventAdapter() 
							{
								@Override
								public void conComplete() 
								{
									j.completed();
								}
							});
					j.started();
					
					fallback = defaultFallback;
					unscheduled = false;
				}
				catch (NetworkException ne)
				{
					throw new RuntimeException("Cannot start new task", ne);
				}
			}

		}
		while (firstId != currentId && unscheduled);
		
		if (unscheduled) 
		{
			new DeferredEvent(fallback) 
			{
				@Override
				protected void eventAction() 
				{
					bjs.handleJobRequestArrival(j);
				}
			};
			fallback *= 1.2;
		}
	}

	public void setupVMset(Collection<VirtualMachine> vms) 
	{
		vmset = new ArrayList<VirtualMachine>(vms);
	}

	public void setupIaaS(IaaSService iaas) {}
}
