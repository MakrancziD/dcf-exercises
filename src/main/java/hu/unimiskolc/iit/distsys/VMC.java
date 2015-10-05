package hu.unimiskolc.iit.distsys;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.unimiskolc.iit.distsys.interfaces.VMCreationApproaches;

public class VMC extends ExercisesBase implements VMCreationApproaches {

	public void directVMCreation() throws Exception {
		PhysicalMachine phMach = getNewPhysicalMachine();
		phMach.turnon();
		Timed.simulateUntilLastEvent();
		
		VirtualAppliance va = new VirtualAppliance("1", 1, 0 );
		
		phMach.localDisk.registerObject(va);
		
		ResourceConstraints rc = new AlterableResourceConstraints(0.1, 0.1, 16);

		phMach.requestVM(va, rc, phMach.localDisk, 2);
		Timed.simulateUntilLastEvent();
	}

	public void twoPhaseVMCreation() throws Exception {

		PhysicalMachine phMach = getNewPhysicalMachine();
		phMach.turnon();
		Timed.simulateUntilLastEvent();
		
		VirtualAppliance va = new VirtualAppliance("1", 1, 0 );
		
		phMach.localDisk.registerObject(va);
		
		ResourceConstraints rc = new AlterableResourceConstraints(0.1, 0.1, 16);
		 
		 ResourceAllocation res = phMach.allocateResources(rc, true, 100);
		
		VirtualMachine vm = new VirtualMachine(va);
		phMach.deployVM(vm, res, phMach.localDisk);
		
		ResourceAllocation res2 = phMach.allocateResources(rc, true, 100);
		
		VirtualMachine vm2 = new VirtualMachine(va);
		phMach.deployVM(vm2, res2, phMach.localDisk);
		
		Timed.simulateUntilLastEvent();
	}

	public void indirectVMCreation() throws Exception {

		PhysicalMachine phMach = getNewPhysicalMachine();
		phMach.turnon();
		
		IaaSService iaasSvc = getNewIaaSService();
		
		iaasSvc.registerHost(phMach);
		iaasSvc.registerRepository(phMach.localDisk);
		
		Timed.simulateUntilLastEvent();
		
		VirtualAppliance va = new VirtualAppliance("1", 1, 0 );
		
		phMach.localDisk.registerObject(va);
		
		ResourceConstraints rc = new AlterableResourceConstraints(0.1, 0.1, 16);
		
		iaasSvc.requestVM(va, rc, phMach.localDisk, 2);
		
		Timed.simulateUntilLastEvent();
	}

	public void migratedVMCreation() throws Exception {

		PhysicalMachine phMach = getNewPhysicalMachine();
		phMach.turnon();
		
		PhysicalMachine phMach2 = getNewPhysicalMachine();
		phMach2.turnon();
		
		Timed.simulateUntilLastEvent();
		
		VirtualAppliance va = new VirtualAppliance("1", 1, 0 );
		
		phMach.localDisk.registerObject(va);
		phMach2.localDisk.registerObject(va);
		
		ResourceConstraints rc = new AlterableResourceConstraints(0.1, 0.1, 16);
		 
		VirtualMachine vm = phMach.requestVM(va, rc, phMach.localDisk, 1)[0];
		 
		Timed.simulateUntilLastEvent();
		
		phMach.migrateVM(vm, phMach2);
		
		Timed.simulateUntilLastEvent();
	}

}
