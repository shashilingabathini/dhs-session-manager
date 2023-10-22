package org.dhs.plugin.util;

import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.admin.LocalizedString;
import com.filenet.api.admin.PropertyDefinitionString;
import com.filenet.api.admin.PropertyTemplateString;
import com.filenet.api.collection.LocalizedStringList;
import com.filenet.api.collection.PropertyDefinitionList;
import com.filenet.api.constants.Cardinality;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.*;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.util.UserContext;
import org.apache.log4j.Logger;
import org.dhs.plugin.exception.PluginException;

import javax.security.auth.Subject;

public class APIUtil {

    private String objectStoreName;
    private String url;
    private String userName;
    private String password;
    private String stanza;
    private ObjectStore objectStore;
    private Logger logger = Logger.getLogger(APIUtil.class);
    private String SESSION_MANAGER_NAME = "SessionManager";
    private String PT_USERID = "UserId";
    private String PT_IS_LOGGED = "IsLogged";

    /**
     *
     * @param url
     * @param userName
     * @param password
     * @param stanza
     * @param objectStoreName
     */
    public APIUtil(String url , String userName , String password , String stanza,String objectStoreName) {
        this.userName = userName;
        this.url  =  url;
        this.password =  password;
        this.stanza =  stanza;
        this.objectStoreName = objectStoreName;
        if(logger.isDebugEnabled())
            logger.debug(" url : "+url +" , userName : "+userName+" , password : "+password+" , stanza :"+stanza+" , objectStoreName :"+objectStoreName);
    }

    public APIUtil(ObjectStore objectStore) {
        this.objectStore = objectStore;
    }

    /**
     *
     */
    public void getObjectStore() {
        this.objectStore =  Factory.ObjectStore.fetchInstance(getDomain() ,objectStoreName , null );
    }

    private Domain getDomain() {
        return Factory.Domain.fetchInstance(getConnection(), null, null);
    }

    private Connection getConnection() {
        Connection connection = Factory.Connection.getConnection(url);
        Subject subject =  UserContext.createSubject(connection,userName,password,stanza);
        UserContext.get().pushSubject(subject);
        return connection;
    }

    /**
     *
     * @throws PluginException
     */
    public void addSessionManager() throws PluginException {
        String methodName = "addSessionManager";
        if(logger.isInfoEnabled())
            logger.info(" Entry "+methodName);
        try {
            // get userid & isLogged property templates
            ClassDefinition customObjectDefinition = Factory.ClassDefinition.fetchInstance(objectStore, "CustomObject", null);
            logger.info(" definition name is "+customObjectDefinition.get_SymbolicName());
            ClassDefinition sessionManager = customObjectDefinition.createSubclass();
            sessionManager.set_SymbolicName(SESSION_MANAGER_NAME);
            LocalizedString localizedString =  Factory.LocalizedString.createInstance();
            localizedString.set_LocaleName(objectStore.get_LocaleName());
            localizedString.set_LocalizedText(SESSION_MANAGER_NAME);
            LocalizedStringList localizedStringList = Factory.LocalizedString.createList();
            localizedStringList.add(localizedString);
            sessionManager.set_DisplayNames(localizedStringList);
            sessionManager.save(RefreshMode.REFRESH);
            Thread.sleep(3000);
            PropertyTemplateString userIdTemplate = Factory.PropertyTemplateString.createInstance(objectStore);
            // create userId template
            localizedString =  Factory.LocalizedString.createInstance();
            localizedString.set_LocaleName(objectStore.get_LocaleName());
            localizedString.set_LocalizedText(PT_USERID);
            localizedStringList = Factory.LocalizedString.createList();
            localizedStringList.add(localizedString);
            userIdTemplate.set_DisplayNames(localizedStringList);
            userIdTemplate.set_SymbolicName(PT_USERID);
            userIdTemplate.set_Cardinality(Cardinality.SINGLE);
            userIdTemplate.save(RefreshMode.REFRESH);
            // create is logged in template
            PropertyTemplateString loggedInTemplate = Factory.PropertyTemplateString.createInstance(objectStore);
            localizedString =  Factory.LocalizedString.createInstance();
            localizedString.set_LocaleName(objectStore.get_LocaleName());
            localizedString.set_LocalizedText(PT_IS_LOGGED);
            localizedStringList = Factory.LocalizedString.createList();
            localizedStringList.add(localizedString);
            loggedInTemplate.set_DisplayNames(localizedStringList);
            loggedInTemplate.set_SymbolicName(PT_IS_LOGGED);
            loggedInTemplate.set_Cardinality(Cardinality.SINGLE);
            loggedInTemplate.save(RefreshMode.REFRESH);

            // now associate with custom object
            ClassDefinition classDefinition = Factory.ClassDefinition.fetchInstance(objectStore,sessionManager.get_Id(), null);
            PropertyDefinitionList propertyDefinitionList =  classDefinition.get_PropertyDefinitions();
            propertyDefinitionList.add((PropertyDefinitionString)userIdTemplate.createClassProperty());
            propertyDefinitionList.add((PropertyDefinitionString)loggedInTemplate.createClassProperty());
            classDefinition.save(RefreshMode.REFRESH); // this will create a property and saves
        } catch (Exception exception) {
            throw new PluginException(exception);
        }
        if(logger.isInfoEnabled())
            logger.info(" Exit "+methodName);
    }

    /**
     *
     * @return
     * @throws PluginException
     */
    public boolean getSessionManager() throws PluginException {
        String methodName = "getSessionManager";
        if(logger.isInfoEnabled())
            logger.info(" Entry "+methodName);
        boolean object = false;
        try {
            ClassDefinition classDefinition =  Factory.ClassDefinition.fetchInstance(this.objectStore, SESSION_MANAGER_NAME, null);
            if(classDefinition != null) {
                String classSymbolicName  = classDefinition.get_SymbolicName();
                if(logger.isInfoEnabled())
                    logger.info(" class symbolic name :"+classSymbolicName);

                if(classSymbolicName != null)
                    object = true;
            }
        } catch (EngineRuntimeException engineRuntimeException ) {
            if(engineRuntimeException.getExceptionCode().equals(ExceptionCode.E_OBJECT_NOT_FOUND)) {
                logger.error(" "+SESSION_MANAGER_NAME+" is not found & needs to create a new object");
                logger.error(engineRuntimeException);
            }
        } catch (Exception exception) {
            logger.error(exception); // this is a normal exception to stack trace (only once will be called by applicationInit)
        }
        if(logger.isInfoEnabled())
            logger.info(" Exit "+methodName);
        return object;
    }
}
