package org.dhs.plugin.services;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.CustomObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.ibm.ecm.extension.PluginResponseUtil;
import com.ibm.ecm.extension.PluginService;
import com.ibm.ecm.extension.PluginServiceCallbacks;
import com.ibm.ecm.json.JSONResultSetResponse;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ManagerService extends PluginService {

    private ObjectStore objectStore;

    private Logger logger = Logger.getLogger(ManagerService.class);
    private static String TYPE_GET_SESSION = "GetSession";
    private static String TYPE_DISABLE_SESSION = "DisableSession";

    public ManagerService(ObjectStore objectStore) {
        this.objectStore = objectStore;
        if(logger.isInfoEnabled())
            logger.info(" ObjectStore Name {} :" + objectStore.get_Name());
    }

    @Override
    public String getId() {
        return "ManagerService";
    }

    @Override
    public void execute(PluginServiceCallbacks callbacks, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if(logger.isDebugEnabled())
            logger.debug("Entry - execute");
        JSONResultSetResponse jsonResults = new JSONResultSetResponse();
        try {
            String userId = request.getParameter("userId");
            String type = request.getParameter("type");
            if(logger.isInfoEnabled())
                logger.info("userId is "+userId);
            if(type.equals(TYPE_GET_SESSION)) {
                boolean isEnabled = isSessionEnabled(userId);
                if(logger.isInfoEnabled())
                    logger.info("isSessionEnabled : "+isEnabled);
                jsonResults.put("status","Success");
                jsonResults.put("isLogged", isEnabled);
            } else if(type.equals(TYPE_DISABLE_SESSION)) {
                updateSession(userId);
            }
        } catch (Exception exception) {
            jsonResults.put("status","Failed");
            jsonResults.put("error", exception.getLocalizedMessage());
            jsonResults.put("errorCode", "ERR_SRV_MANAGE_SESSION_SERVICE");
        }
        PluginResponseUtil.writeJSONResponse(request,response,jsonResults,callbacks,"ManagerService");
        if(logger.isDebugEnabled())
            logger.debug("Exit - execute");
    }

    private void updateSession(String userId) {
        try {
            // update custom object
            String sqlQuery = "SELECT userId FROM SessionManager WHERE userId = '"+userId+"'";
            SearchSQL searchSQL = new SearchSQL(sqlQuery);
            SearchScope searchScope  = new SearchScope(objectStore);
            IndependentObjectSet independentObjectSet  = searchScope.fetchObjects(searchSQL,Integer.MAX_VALUE,null, false);
            CustomObject sessionObject = (CustomObject) independentObjectSet.iterator().next();
            sessionObject.getProperties().putValue("isLogged","F");
            sessionObject.save(RefreshMode.NO_REFRESH);
        } catch (Exception exception) {
            throw  exception;
        }
    }

    private boolean isSessionEnabled(String userId) {
        boolean isEnabled = false;
        try {
            String sqlQuery = "SELECT userId FROM SessionManager WHERE userId = '"+userId+"' AND isLogged = 'T'";
            SearchSQL searchSQL = new SearchSQL(sqlQuery);
            SearchScope searchScope  = new SearchScope(objectStore);
            RepositoryRowSet repositoryRowSet = searchScope.fetchRows(searchSQL,Integer.MAX_VALUE,null, false);
            isEnabled = !repositoryRowSet.isEmpty(); // if logged in non-empty results
        } catch (Exception exception) {
            throw exception;
        }
        return isEnabled;
    }
}
