package com.siemens.tcloadtester.modules;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.siemens.tcloadtester.core.IModule;
import com.siemens.tcloadtester.core.Module;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core.FileManagementService;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.services.strong.core._2006_03.FileManagement.FileTicketsResponse;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.ImanFile;
import com.teamcenter.soa.common.ObjectPropertyPolicy;
import com.teamcenter.soa.common.PolicyProperty;
import com.teamcenter.soa.common.PolicyType;

/**
 * A module that download datasets from a FMS server.
 * 
 */
public final class FMSDownload extends Module implements IModule {
	List<URL> downloadURLs; 

	public void validate() {
		validate(new String[] { "dataset_uid", "fms_url" });
	}

	/**
	 * 
	 * 
	 * @throws Exception
	 */
	public void initialize(Connection connection) throws Exception {
		SessionService ss = SessionService.getService(connection);

		ObjectPropertyPolicy opp = new ObjectPropertyPolicy();
		opp.addType(new PolicyType("Dataset", new String[] { "ref_list" }, new String[] { PolicyProperty.WITH_PROPERTIES }));
		opp.addType(new PolicyType("ImanFile", new String[] { "original_file_name" }));
		ss.setObjectPropertyPolicy(opp);
		
		downloadURLs = new ArrayList<URL>();
		
		DataManagementService dm = DataManagementService.getService(connection);

		ServiceData data = dm.loadObjects(new String[] { getParsedSetting("dataset_uid") });

		if (data.sizeOfPlainObjects() == 0)
			throw new Exception("Cannot find dataset.");

		Dataset dataSet = (Dataset) data.getPlainObject(0);

		ImanFile[] namedRefs = Arrays.copyOf(dataSet.get_ref_list(), dataSet.get_ref_list().length, ImanFile[].class);

		FileManagementService fm = FileManagementService.getService(connection);

		FileTicketsResponse fileTickets = fm.getFileReadTickets(namedRefs);

		String fmsUrl = getParsedSetting("fms_url");
		for (ImanFile nr : namedRefs) {
			downloadURLs.add(new URL(String.format("%s//%s?ticket=%s",
					fmsUrl,
					nr.get_original_file_name(),
					(String)fileTickets.tickets.get(nr))));
		}

		if (downloadURLs.size() == 0)
			throw new Exception("No files to download.");
	}

	/**
	 * 
	 * 
	 * @throws Exception
	 */
	public void execute(Connection connection) throws Exception {
		InputStream is = null;
		DataInputStream dis;
		URLConnection conn = null;

		try {
			long totalSize = 0;
			timer.start();
			for (URL url : downloadURLs) {
				conn = url.openConnection();
				conn.setConnectTimeout(30000);
				conn.setReadTimeout(30000);
				conn.connect();
				is = conn.getInputStream();
				dis = new DataInputStream(new BufferedInputStream(is));

				int size = 0;
				byte[] input = new byte[1024];
				while ((size = dis.read(input)) > 0) {
					totalSize += size;
				}
			}
			setTime(timer.stop());
			setSize(totalSize);
			miscInfo = String.format("Size of download is %dkb", totalSize / 1024);
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				is.close();
			}
			catch (Exception e) { }
		}
	}
}
