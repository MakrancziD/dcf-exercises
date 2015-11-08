package hu.unimiskolc.iit.distsys;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.Job;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.unimiskolc.iit.distsys.ComplexDCFJob;
import hu.unimiskolc.iit.distsys.ExercisesBase;
import hu.unimiskolc.iit.distsys.interfaces.BasicJobScheduler;

import java.util.Collection;
import java.util.HashMap;

public class BasicScaler implements BasicJobScheduler, VirtualMachine.StateChange 
{
	private IaaSService iaas;
	private Repository r;
	private VirtualAppliance va;
	private HashMap<VirtualMachine, Job> vmsWithPurpose = new HashMap<VirtualMachine, Job>();
	private HashMap<VirtualMachine, DeferredEvent> vmPool = new HashMap<VirtualMachine, DeferredEvent>();

	public void setupVMset(Collection<VirtualMachine> vms) {}

	public void setupIaaS(IaaSService iaas) 
	{
		this.iaas = iaas;
		r = iaas.repositories.get(0);
		va = (VirtualAppliance) r.contents().iterator().next();
	}

	public void handleJobRequestArrival(Job j) 
	{
		try 
		{
			ConstantConstraints cc = new ConstantConstraints(j.nprocs,
					ExercisesBase.minProcessingCap, ExercisesBase.minMem
							/ j.nprocs);
			for (VirtualMachine vm : vmPool.keySet()) 
			{
				if (vm.getResourceAllocation().allocated.getRequiredCPUs() >= j.nprocs) 
				{
					vmPool.remove(vm).cancel();
					allocateVMforJob(vm, j);
					return;
				}
			}
			VirtualMachine vm = iaas.requestVM(va, cc, r, 1)[0];
			vm.subscribeStateChange(this);
			vmsWithPurpose.put(vm, j);
		} 
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
	}

	private void allocateVMforJob(final VirtualMachine vm, Job j) 
	{
		try 
		{
			((ComplexDCFJob) j).startNowOnVM(vm, new ConsumptionEventAdapter() 
			{
				@Override
				public void conComplete() 
				{
					super.conComplete();
					vmPool.put(vm, new DeferredEvent(ComplexDCFJob.noJobVMMaxLife - 1000) 
					{
						protected void eventAction() 
						{
							try 
							{
								vmPool.remove(vm);
								vm.destroy(false);
							} 
							catch (Exception e) 
							{
								throw new RuntimeException(e);
							}
						}
					});
				}
			});
		} catch (Exception e) 
		{
			throw new RuntimeException(e);
		}

	}

	public void stateChanged(final VirtualMachine vm, State oldState, State newState) 
	{
		if (newState.equals(VirtualMachine.State.RUNNING)) 
		{
			allocateVMforJob(vm, vmsWithPurpose.remove(vm));
			vm.unsubscribeStateChange(this);
		}
	}
}