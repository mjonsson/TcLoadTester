package com.siemens.tcloadtester.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.siemens.tcloadtester.core.IModule;
import com.siemens.tcloadtester.core.Module;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.query.SavedQueryService;
import com.teamcenter.services.strong.query._2008_06.SavedQuery.QueryInput;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.QueryResults;
import com.teamcenter.services.strong.query._2007_09.SavedQuery.SavedQueriesResponse;
import com.teamcenter.services.strong.query._2010_04.SavedQuery.FindSavedQueriesCriteriaInput;
import com.teamcenter.services.strong.query._2010_04.SavedQuery.FindSavedQueriesResponse;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.ImanQuery;

/**
 * This class is superclass for all modules that need some kind of input data to
 * perform an action on. This module executes a defined saved query and returns
 * all result in the form of generic ModelObjects.
 */
public class QueryModule extends Module implements IModule {
	/**
	 * Teamcenter saved query object.
	 */
	private ImanQuery query = null;
	private String strQuery;
	private String strEntries;
	private String strValues;
	/**
	 * The result array of Teamcenter generic modelobjects.
	 */
	protected ModelObject[] queryObjects;

	public void validate() {
		validate(new String[] { "saved_query", "query_entries", "query_values", "query_batch", "max_return" } );
	}

	/**
	 * Finds the saved query object.
	 * 
	 * @throws Exception
	 */
	public void initialize(Connection connection) throws Exception {
		SavedQueryService sq = SavedQueryService.getService(connection);

		strQuery = getParsedSetting("saved_query");
		FindSavedQueriesCriteriaInput[] findInput = { new FindSavedQueriesCriteriaInput() };
		findInput[0].queryNames = new String[] { strQuery };
		FindSavedQueriesResponse response1 = sq.findSavedQueries(findInput);

		if (response1.savedQueries.length == 1)
			query = response1.savedQueries[0];
		else
			throw new Exception(String.format(
					"Teamcenter query \"%s\" was not found.",
					strQuery));
	}

	/**
	 * Executes the saved query and returns all modelobjects found.
	 * 
	 * @throws Exception
	 */
	public void execute(Connection connection) throws Exception {
		SavedQueryService sq = SavedQueryService.getService(connection);

		int batch = getParsedSettingAsInt("query_batch");
		QueryInput queryInput[] = new QueryInput[batch];

		strEntries = getParsedSetting("query_entries");
		for (int i = 0; i < batch; i++) {
			strValues = getParsedSetting("query_values");
			queryInput[i] = new QueryInput();
			queryInput[i].query = query;
			queryInput[i].entries = strEntries.split(",");
			queryInput[i].values = strValues.split(",");
			queryInput[i].maxNumToReturn = getParsedSettingAsInt("max_return");
		}

		timer.start();
		SavedQueriesResponse response1 = sq.executeSavedQueries(queryInput);

		List<String> queryObjectsList = new ArrayList<String>();
		if (response1.arrayOfResults != null)
			for (QueryResults result : response1.arrayOfResults)
				Collections.addAll(queryObjectsList, result.objectUIDS);

		DataManagementService dm = DataManagementService.getService(connection);
		ServiceData sd = dm.loadObjects(queryObjectsList
				.toArray(new String[queryObjectsList.size()]));
		setTime(timer.stop());

		queryObjects = new ModelObject[sd.sizeOfPlainObjects()];
		for (int i = 0; i < sd.sizeOfPlainObjects(); i++) {
			queryObjects[i] = sd.getPlainObject(i);
		}

		miscInfo = String
				.format("Query: %s, Entries: %s, Value: %s, Batch: %d, Property Policy: %s, Returned uids: %d",
						strQuery, strEntries, strValues, batch, strPropertyPolicy,
						queryObjects.length);
	}
}
