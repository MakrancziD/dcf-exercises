package hu.unimiskolc.iit.distsys;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;

public class VMC extends ExercisesBase implements VMCreationApproaches {

	public void directVMCreation() throws Exception {
		PhysicalMachine phMach = getNewPhysicalMachine();
		phMach.turnon();
		Timed.simulateUntilLastEvent();
		
		VirtualAppliance va = new VirtualAppliance("1", 1, 0 );
		
		phMach.localDisk.registerObject(va);
		
		ResourceConstraints rc = new ConstantConstraints(0.1, 0.1, false, 16);
		 
		phMach.allocateResources(rc, false, 100);
		
		phMach.requestVM(va, rc, phMach.localDisk, 2);
		Timed.simulateUntilLastEvent();
	}

	public void twoPhaseVMCreation() throws Exception {

		PhysicalMachine phMach = getNewPhysicalMachine();
		phMach.turnon();
		Timed.simulateUntilLastEvent();
		
		VirtualAppliance va = new VirtualAppliance("1", 1, 0 );
		
		phMach.localDisk.registerObject(va);
		
		ResourceConstraints rc = new ConstantConstraints(0.1, 0.1, false, 16);
		 
		phMach.allocateResources(rc, false, 100);
		
		VirtualMachine vm = new VirtualMachine(va);
		//phMach.deployVM(vm, rc, phMach.localDisk);
		
		Timed.simulateUntilLastEvent();
	}

	public void indirectVMCreation() throws Exception {

	}

	public void migratedVMCreation() throws Exception {

	}

}
