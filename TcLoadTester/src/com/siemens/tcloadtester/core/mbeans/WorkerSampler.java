package com.siemens.tcloadtester.core.mbeans;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MXBean;
import javax.management.ObjectName;

import com.siemens.tcloadtester.TcLoadTester;
import com.siemens.tcloadtester.core.ModuleOcc;
import com.siemens.tcloadtester.core.Status;
import com.siemens.tcloadtester.core.Worker;
import com.siemens.tcloadtester.core.events.Event;
import com.siemens.tcloadtester.core.events.WorkerEventListener;

@MXBean
public class WorkerSampler implements WorkerSamplerMXBean, WorkerEventListener {

	private static List<WorkerSampler> workerSamplers = new ArrayList<WorkerSampler>();
	private ModuleOcc moduleOcc = null;
	private long time = 0;
	private long size = 0;
	private static MBeanServer mbs = null;
	private ObjectName objectName = null;
//	private Worker worker = null;
	private final Object lock = new Object();

	public WorkerSampler(ModuleOcc moduleOcc) {
		mbs = ManagementFactory.getPlatformMBeanServer();
//		this.worker = worker;
		this.moduleOcc = moduleOcc;
		TcLoadTester.addWorkerEventListener(this);
	}

	public static void unregister() {
		try {
			for (WorkerSampler s : workerSamplers) {
				mbs.unregisterMBean(s.objectName);
				s.objectName = null;
			}
			workerSamplers = new ArrayList<WorkerSampler>();
		} catch (MBeanRegistrationException | InstanceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void handleWorkerEvent(Event e) {
		if (e.getSource() instanceof Worker) {
			Worker w = (Worker) e.getSource();

			if (w.moduleOcc == moduleOcc &&
					w.status == Status.SLEEPING) {
				if (objectName == null) {
					synchronized (lock) {
						try {
							objectName = new ObjectName("com.siemens.tcloadtester:type=WorkerSampler,worker=" + w.getWorkerId() + ",id=" + moduleOcc.getId());
							mbs.registerMBean(this, objectName);
							workerSamplers.add(this);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
				synchronized (lock) {
					time = moduleOcc.moduleObj.getTime();
					size = moduleOcc.moduleObj.getSize();
				}
			}

//			if (w.status == Status.FINISHED ||
//					w.status == Status.ERROR) {
//				synchronized (lock) {
//					try {
//						if (w == worker && objectName != null) {
//							mbs.unregisterMBean(objectName);
//							objectName = null;
//						}
//					} catch (Exception e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//				}
//			}
		}
	}


	@Override
	public long getTime() {
		synchronized (lock) {
			return time;
		}
	}

	@Override
	public long getSize() {
		synchronized (lock) {
			return size;
		}
	}
}
