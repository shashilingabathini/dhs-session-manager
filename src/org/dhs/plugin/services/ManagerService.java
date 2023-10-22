package org.dhs.plugin.services;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.CustomObject;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;
import com.ibm.ecm.extension.PluginResponseUtil;
import com.ibm.ecm.extension.PluginService;
import com.ibm.ecm.extension.PluginServiceCallbacks;
import com.ibm.ecm.json.JSONResultSetResponse;
import org.apache.log4j.Logger;
import org.dhs.plugin.exception.PluginException;
import org.dhs.plugin.util.APIUtil;
import org.dhs.plugin.util.Configuration;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ManagerService extends PluginService {

    private ObjectStore objectStore;
    private APIUtil apiUtil;
    private Logger logger = Logger.getLogger(ManagerService.class);
    private static String TYPE_GET_SESSION = "GetSession";
    private static String TYPE_DISABLE_SESSION = "DisableSession";
    private static String TYPE_ADD_SESSION  = "AddSession";

    /**
     *
     * @param objectStore
     */
    public ManagerService(ObjectStore objectStore) {
        this.objectStore = objectStore;
        if(logger.isInfoEnabled())
            logger.info(" ObjectStore Name {} :" + objectStore.get_Name());
    }

    public ManagerService() {}

    @Override
    public String getId() {
        return "ManagerService";
    }

    /**
     *
     * @param callbacks
     * @param request
     * @param response
     * @throws Exception
     */
    @Override
    public void execute(PluginServiceCallbacks callbacks, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if(logger.isDebugEnabled())
            logger.debug("Entry - execute");
        JSONResultSetResponse jsonResults = new JSONResultSetResponse();
        try {
            String userId = request.getParameter("userId");
            String type = request.getParameter("type");
            initializeObjects(callbacks);
            if(logger.isInfoEnabled())
                logger.info("userId is "+userId);
            if(type.equals(TYPE_GET_SESSION)) {
                boolean isEnabled = isSessionEnabled(userId);
                if(logger.isInfoEnabled())
                    logger.info("isSessionEnabled : "+isEnabled);
                System.out.println("isSessionEnabled : "+isEnabled);
                jsonResults.put("status","Success");
                jsonResults.put("isLogged", isEnabled);
            } else if(type.equals(TYPE_DISABLE_SESSION)) {
                updateLoginSession(userId);
                jsonResults.put("status","Success");
            } else if(type.equals(TYPE_ADD_SESSION)) {
                addNewSession(userId);
                jsonResults.put("status","Success");
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

    /**
     *
     * @param callbacks
     */
    private void initializeObjects(PluginServiceCallbacks callbacks) {
        try {
            this.objectStore  =   fetchObjectStore(callbacks);
            apiUtil =  new APIUtil(objectStore);
            boolean sessionManager  =  apiUtil.getSessionManager();
            logger.info(" session manager object check "+sessionManager);
            if(!sessionManager)
                apiUtil.addSessionManager();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
    }

    /**
     *
     * @param callbacks
     * @return
     */
    private ObjectStore  fetchObjectStore(PluginServiceCallbacks callbacks) {
        if(logger.isDebugEnabled())
            logger.debug("Entry - fetchObjectStore");
        String repoId  = callbacks.getRepositoryId();
        logger.info("repoId : "+repoId);
        System.out.println("repoId :" +repoId);
        if(repoId ==  null) {
            repoId = Configuration.getConfigProp("org.dhs.plugin.repo.id");
            Subject subject = callbacks.getP8Subject(repoId);
            UserContext.get().pushSubject(subject);
        }
        if(logger.isDebugEnabled())
            logger.debug("Exit - fetchObjectStore");
        return callbacks.getP8ObjectStore(repoId);
    }

    /**
     *
     * @param userId
     */
    private void updateLoginSession(String userId) { // this will be called during logout , window close or session timeout from navigator
        try {
            // update custom object
            String sqlQuery = "SELECT UserId FROM SessionManager WHERE UserId = '"+userId+"'";
            SearchSQL searchSQL = new SearchSQL(sqlQuery);
            SearchScope searchScope  = new SearchScope(objectStore);
            IndependentObjectSet independentObjectSet  = searchScope.fetchObjects(searchSQL,Integer.MAX_VALUE,null, false);
            CustomObject sessionObject = (CustomObject) independentObjectSet.iterator().next();
            sessionObject.getProperties().putValue("IsLogged","F");
            sessionObject.save(RefreshMode.NO_REFRESH);
        } catch (Exception exception) {
            throw  exception;
        }
    }

    /**
     *
     * @param userId
     * @return
     */
    private boolean isSessionEnabled(String userId) {
        boolean isEnabled = false;
        try {
            String sqlQuery = "SELECT UserId FROM SessionManager WHERE UserId = '"+userId+"' AND IsLogged = 'T'";
            SearchSQL searchSQL = new SearchSQL(sqlQuery);
            SearchScope searchScope  = new SearchScope(objectStore);
            RepositoryRowSet repositoryRowSet = searchScope.fetchRows(searchSQL,Integer.MAX_VALUE,null, false);
            isEnabled = !repositoryRowSet.isEmpty(); // if logged in non-empty results
            if(isEnabled)
                return isEnabled;
            // also check if user is available
            String[] users = Configuration.getConfigProp("org.dhs.plugin.repo.unallowed.users").split(",");
            for(String u :users) {
                if(u.equals(userId)) {
                    isEnabled  = true;
                    break;
                }
            }
        } catch (Exception exception) {
            throw exception;
        }
        return isEnabled;
    }

    /**
     *
     * @param userId
     * @return
     * @throws PluginException
     */
    private boolean addNewSession(String userId) throws PluginException {
        boolean isAdded = false;
        try {
           // before adding check with same user any custom object is available
            String sqlQuery = "SELECT UserId FROM SessionManager WHERE UserId = '"+userId+"'";
            SearchSQL searchSQL = new SearchSQL(sqlQuery);
            SearchScope searchScope  = new SearchScope(objectStore);
            IndependentObjectSet independentObjectSet = searchScope.fetchObjects(searchSQL,Integer.MAX_VALUE,null,true);
            if(!independentObjectSet.isEmpty()) {
               CustomObject sessionObject =  (CustomObject) independentObjectSet.iterator().next();
               sessionObject.getProperties().putObjectValue("IsLogged","T");
               sessionObject.save(RefreshMode.NO_REFRESH);
            } else {
                CustomObject sessionObject =  Factory.CustomObject.createInstance(this.objectStore,"SessionManager");
                sessionObject.getProperties().putObjectValue("UserId", userId);
                sessionObject.getProperties().putObjectValue("IsLogged","T");
                sessionObject.save(RefreshMode.NO_REFRESH);
            }
        } catch (Exception exception) {
            throw new PluginException(exception);
        }
        return isAdded;
    }
}
