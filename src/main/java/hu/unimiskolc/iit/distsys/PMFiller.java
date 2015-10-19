package hu.unimiskolc.iit.distsys;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.unimiskolc.iit.distsys.interfaces.FillInAllPMs;

public class PMFiller implements FillInAllPMs 
{

	@Override
	public void filler(IaaSService iaas, int vmCount)
	{
		ResourceConstraints rcAll = iaas.getCapacities();
		long minMemory = Long.MAX_VALUE;
		double minProcessing = Double.MAX_VALUE;
		double minCores = Double.MAX_VALUE;

		Repository repo = iaas.repositories.get(0);
		VirtualAppliance va = (VirtualAppliance) repo.contents().iterator().next();
		
		for (PhysicalMachine pm : iaas.machines) {
			ResourceConstraints pmCaps = pm.getCapacities();
			minMemory = Math.min(minMemory, pmCaps.getRequiredMemory());
			minProcessing = Math.min(minProcessing, pmCaps.getRequiredProcessingPower());
			minCores = Math.min(minCores, pmCaps.getRequiredCPUs());

		}
		
		ConstantConstraints cc = new ConstantConstraints(rcAll.getRequiredCPUs() / vmCount, minProcessing, minMemory/ vmCount);

		try
		{
			iaas.requestVM(va, cc, repo, vmCount - iaas.machines.size());
			Timed.simulateUntilLastEvent();
			
			ArrayList<PhysicalMachine> sortedPMs = new ArrayList<PhysicalMachine>(iaas.machines);
			Comparator<PhysicalMachine> freeComp = new Comparator<PhysicalMachine>() {
				public int compare(PhysicalMachine o1, PhysicalMachine o2) {
					return (int) Math.signum(o2.freeCapacities
							.getTotalProcessingPower()
							- o1.freeCapacities.getTotalProcessingPower());
				}
			};
			Collections.sort(sortedPMs, freeComp);

			for (PhysicalMachine pm : sortedPMs) {

				iaas.requestVM(
						va,
						new ConstantConstraints(
								pm.freeCapacities.getRequiredCPUs() * pm.getCapacities() .getRequiredProcessingPower() / pm.freeCapacities.getRequiredProcessingPower(),
								pm.freeCapacities.getRequiredProcessingPower(),
								pm.freeCapacities.getRequiredMemory()), repo, 1);
				Timed.simulateUntilLastEvent();
		}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
