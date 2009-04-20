/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.appserv.connectors.internal.api;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.logging.LogDomains;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolManager;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPool;
import com.sun.corba.se.spi.orbutil.threadpool.NoSuchThreadPoolException;
import com.sun.corba.se.impl.orbutil.threadpool.ThreadPoolManagerImpl;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Constructor;

/**
 * Util class for connector related classes
 */
public class ConnectorsUtil {

        private static Logger _logger= LogDomains.getLogger(ConnectorsUtil.class, LogDomains.RSR_LOGGER);

    /**
     * determine whether the RAR in question is a System RAR
     * @param raName RarName
     * @return boolean
     */
    public static boolean belongsToSystemRA(String raName) {
        boolean result = false;

        for (String systemRarName : ConnectorConstants.systemRarNames) {
            if (systemRarName.equals(raName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * get the installation directory of System RARs
     * @param moduleName RARName
     * @return directory location
     */
    public static String getSystemModuleLocation(String moduleName) {
        String j2eeModuleDirName = System.getProperty(ConnectorConstants.INSTALL_ROOT) +
                File.separator + "lib" +
                File.separator + "install" +
                File.separator + "applications" +
                File.separator + moduleName;

        return j2eeModuleDirName;
    }

    public static String getLocation(String moduleName) {
        return ConfigBeansUtilities.getLocation(moduleName);
        /* TODO V3

            if(moduleName == null) {
                return null;
            }
            String location  = null;
            ConnectorModule connectorModule =
                    dom.getApplications().getConnectorModuleByName(moduleName);
            if(connectorModule != null) {
                location = RelativePathResolver.
                        resolvePath(connectorModule.getLocation());
            }
            return location;
        */


    }
    /**
     *  Return the system PM name for the JNDI name
     * @param  jndiName jndi name
     * @return String jndi name for PM resource
     **/
    public  static String getPMJndiName( String jndiName )  {
        return jndiName + ConnectorConstants.PM_JNDI_SUFFIX;
    }

    /**
     * check whether the jndi Name has connector related suffix and return if any.
     * @param name jndi name
     * @return suffix, if found
     */
    public static String getValidSuffix(String name) {
        if (name != null) {
            for (String validSuffix : ConnectorConstants.JNDI_SUFFIX_VALUES) {
                if (name.endsWith(validSuffix)) {
                    return validSuffix;
                }
            }
        }
        return null;
    }

    /**
     * If the suffix is one of the valid context return true.
     * Return false, if that is not the case.
     *
     * @param suffix __nontx / __pm
     * @return boolean whether the suffix is valid or not
     */
    public static boolean isValidJndiSuffix(String suffix) {
        if (suffix != null) {
            for (String validSuffix : ConnectorConstants.JNDI_SUFFIX_VALUES) {
                if (validSuffix.equals(suffix)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Given the name of the resource and its jndi env, derive the complete jndi name. (eg; with __PM / __nontx)
     * @param name name of the resource
     * @param env env
     * @return derived name
     */
    public static String deriveJndiName(String name, Hashtable env) {
        String suffix = (String) env.get(ConnectorConstants.JNDI_SUFFIX_PROPERTY);
        if (ConnectorsUtil.isValidJndiSuffix(suffix)) {
            return name + suffix;
        }
        return name;
    }

    public static boolean isValidEventType(Object instance) {
        return (instance instanceof JdbcConnectionPool || 
                instance instanceof JdbcResource ||
                instance instanceof ConnectorConnectionPool ||
                instance instanceof ConnectorResource ||
                instance instanceof MailResource ||
                instance instanceof ExternalJndiResource ||
                instance instanceof CustomResource ||
                instance instanceof AdminObjectResource ||
                instance instanceof WorkSecurityMap ||
                instance instanceof ResourceAdapterConfig ) ;
    }

    /**
     * given a jdbc-resource, get associated jdbc-connection-pool
     * @param resource jdbc-resource
     * @return jdbc-connection-pool
     */
    public static JdbcConnectionPool getAssociatedJdbcConnectionPool(JdbcResource resource, Resources allResources) {
        //TODO V3 need to find a generic way (instead of separate methods for jdbc/connector)
        for(Resource configuredResource : allResources.getResources()){
            if(configuredResource instanceof JdbcConnectionPool){
                JdbcConnectionPool pool = (JdbcConnectionPool)configuredResource;
                if(resource.getPoolName().equalsIgnoreCase(pool.getName())){
                    return pool;
                }
            }
        }
        return null;  //TODO V3 cannot happen ?
    }

    /**
     * given a connector-resource, get associated connector-connection-pool
     * @param resource connector-resource
     * @return connector-connection-pool
     */
    public static ConnectorConnectionPool getAssociatedConnectorConnectionPool(ConnectorResource resource, Resources allResources) {
        for(Resource configuredResource : allResources.getResources()){
            if(configuredResource instanceof ConnectorConnectionPool){
                ConnectorConnectionPool pool = (ConnectorConnectionPool)configuredResource;
                if(resource.getPoolName().equalsIgnoreCase(pool.getName())){
                    return pool;
                }
            }
        }
        return null;  //TODO V3 cannot happen ?
    }

    public static ResourcePool getConnectionPoolConfig(String poolName, Resources allResources){
        for(Resource configuredResource : allResources.getResources()){
            if(configuredResource instanceof ResourcePool){
                ResourcePool pool = (ResourcePool)configuredResource;
                if(pool.getName().equalsIgnoreCase(poolName)){
                    return pool;
                }
            }
        }
        return null; //TODO V3 cannot happen ?
    }

    public static Collection<Resource> getAllResources(Collection<String> poolNames, Resources allResources) {
        List<Resource> connectorResources = new ArrayList<Resource>();
        for(Resource resource : allResources.getResources()){
            if(resource instanceof ConnectorResource){
                ConnectorResource connectorResource = (ConnectorResource)resource;
                if(poolNames.contains(connectorResource.getPoolName())){
                    connectorResources.add(connectorResource);
                }
            }
        }
        return connectorResources;
    }

    /**
     * get the list of pool names
     * @param connectionPools list of pools
     * @return list of pol names
     */
    public static Collection<String> getAllPoolNames(Collection<ConnectorConnectionPool> connectionPools) {
        Set<String> poolNames = new HashSet<String>();
        for(ConnectorConnectionPool pool : connectionPools){
            poolNames.add(pool.getName());
        }
        return poolNames;
    }

    /**
     * get the pools for a particular resource-adapter
     * @param moduleName resource-adapter name
     * @return collection of connectorConnectionPool
     */
    public static Collection<ConnectorConnectionPool> getAllPoolsOfModule(String moduleName, Resources allResources) {
        List<ConnectorConnectionPool> connectorConnectionPools = new ArrayList<ConnectorConnectionPool>();
        for(Resource resource : allResources.getResources()){
            if(resource instanceof ConnectorConnectionPool){
                ConnectorConnectionPool connectorConnectionPool = (ConnectorConnectionPool)resource;
                if(connectorConnectionPool.getResourceAdapterName().equals(moduleName)){
                    connectorConnectionPools.add(connectorConnectionPool);
                }
            }
        }
        return connectorConnectionPools;
    }

    /**
     * Get all System RAR pools and resources
     * @return Collection of system RAR pools
     */
    public static Collection<Resource> getAllSystemRAResourcesAndPools(Resources allResources) {
        //Make sure that resources are added first and then pools.
        List<Resource> resources = new ArrayList<Resource>();
        List<Resource> pools = new ArrayList<Resource>();
        for(Resource resource : allResources.getResources()){
            if(resource instanceof JdbcConnectionPool ){
                pools.add(resource);
            } else if( resource instanceof ConnectorConnectionPool){
                String raName = ((ConnectorConnectionPool)resource).getResourceAdapterName();
                if( ConnectorsUtil.belongsToSystemRA(raName) ){
                    pools.add(resource);
                }
            } else if(resource instanceof JdbcResource){
                resources.add(resource);
            } else if( resource instanceof ConnectorResource){
                String poolName = ((ConnectorResource)resource).getPoolName();
                String raName = getResourceAdapterNameOfPool(poolName, allResources);
                if( ConnectorsUtil.belongsToSystemRA(raName) ){
                    resources.add(resource);
                }
            }
        }
        resources.addAll(pools);
        return resources;
    }

    /**
     * Given the poolname, retrieve the resourceadapter name
     * @param poolName
     * @return resource-adaapter name
     */
    public static String getResourceAdapterNameOfPool(String poolName, Resources allResources) {
        String raName = ""; //TODO V3 this need not be initialized to ""
        for(Resource resource : allResources.getResources()){
            if(resource instanceof ConnectorConnectionPool){
                ConnectorConnectionPool ccp = (ConnectorConnectionPool)resource;
                String name = ccp.getName();
                if(name.equalsIgnoreCase(poolName)){
                    raName = ccp.getResourceAdapterName();
                }
            }
        }
        return raName;
    }

    public static List<WorkSecurityMap> getWorkSecurityMaps(String raName, Resources allResources){
        List<Resource> resourcesList = allResources.getResources();
        List<WorkSecurityMap> workSecurityMaps = new ArrayList<WorkSecurityMap>();
        for(Resource resource : resourcesList){
            if(resource instanceof WorkSecurityMap){
                WorkSecurityMap wsm = (WorkSecurityMap)resource;
                if(wsm.getResourceAdapterName().equals(raName)){
                    workSecurityMaps.add(wsm);
                }
            }
        }
        return workSecurityMaps;
    }
    public static AdminObjectResource[] getEnabledAdminObjectResources(String raName, Resources allResources,
                                                                       Server server)  {
        List resourcesList = allResources.getResources();
        int resourceCount = resourcesList.size();      //sizeAdminObjectResource();
        if(resourceCount == 0) {
            return null;
        }
        List<AdminObjectResource> adminObjectResources = new ArrayList<AdminObjectResource>();
        for(int i=0; i< resourceCount; ++i) {
             Resource resource = (Resource)resourcesList.get(i);

            if(resource == null || !(resource instanceof AdminObjectResource))
                continue;
            AdminObjectResource adminObjectResource = (AdminObjectResource)resource;
            String resourceAdapterName = adminObjectResource.getResAdapter();

            if(resourceAdapterName == null)
                continue;
            if(raName!= null && !raName.equals(resourceAdapterName))
                continue;


            // skips the admin resource if it is not referenced by the server
            if(!isEnabled(adminObjectResource, server))
                continue;
            adminObjectResources.add(adminObjectResource);
        }
        AdminObjectResource[] allAdminObjectResources =
                    new AdminObjectResource[adminObjectResources.size()];
        return adminObjectResources.toArray(allAdminObjectResources);
    }
    
     public static boolean isEnabled(AdminObjectResource aot, Server server) {
        if(aot == null || !Boolean.parseBoolean(aot.getEnabled()))
            return false;
        if(!isResourceReferenceEnabled(aot.getJndiName(), server))
            return false;

        String raName = aot.getResAdapter();
        return isRarEnabled(raName);
    }

     /**
     * Checks if a resource reference is enabled
     * @since SJSAS 8.1 PE/SE/EE
     */
    private static boolean isResourceReferenceEnabled(String resourceName, Server server) {

        ResourceRef ref  = null;
        //TODO V3 server should not be null.
        if(server != null){
            ref = server.getResourceRef(resourceName);
        }

        if (ref == null) {
            _logger.fine("ResourcesUtil :: isResourceReferenceEnabled null ref");
            //todo for V3
            // if(isADeployEvent())
                return true;
            //else
              //  return false;
        }
        _logger.fine("ResourcesUtil :: isResourceReferenceEnabled ref enabled ?" + Boolean.parseBoolean(ref.getEnabled()));
        return  Boolean.parseBoolean(ref.getEnabled());
    }

     private static boolean isRarEnabled(String raName) {
       //todo: for v3
       return true;
       /* if(raName == null || raName.length() == 0)
            return false;
        ConnectorModule module = dom.getApplications().getConnectorModuleByName(raName);
        if(module != null) {
            if(!module.isEnabled())
                return false;
            return isApplicationReferenceEnabled(raName);
        } else if(belongToSystemRar(raName)) {
            return true;
        } else {
            return belongToEmbeddedRarAndEnabled(raName);
        }  */
    }

    public static String getResourceType(Resource resource){
        if(resource instanceof JdbcResource){
            return ConnectorConstants.RES_TYPE_JDBC;
        } else if(resource instanceof JdbcConnectionPool){
            return ConnectorConstants.RES_TYPE_JCP;
        } else if (resource instanceof ConnectorResource){
            return ConnectorConstants.RES_TYPE_CR;
        } else if (resource instanceof ConnectorConnectionPool){
            return ConnectorConstants.RES_TYPE_CCP;
        } else if (resource instanceof MailResource){
            return ConnectorConstants.RES_TYPE_MAIL;
        } else if( resource instanceof ExternalJndiResource){
            return ConnectorConstants.RES_TYPE_EXTERNAL_JNDI;
        } else if (resource instanceof CustomResource){
            return ConnectorConstants.RES_TYPE_CUSTOM;
        } else if (resource instanceof AdminObjectResource){
            return ConnectorConstants.RES_TYPE_AOR;
        } else if (resource instanceof ResourceAdapterConfig){
            return ConnectorConstants.RES_TYPE_RAC;
        } else if (resource instanceof WorkSecurityMap){
            return ConnectorConstants.RES_TYPE_CWSM;
        } else {
            return null;
            //TODO V3 log and throw exception
        }
    }

    /**
     * load and create an object instance
     */
    public static Object loadObject(String className) {
        Object obj = null;
        Class c;

        try {
            //TODO V3 correct approach ?
            obj = Class.forName(className).newInstance();
        } catch (Exception cnf) {
            try {
                //TODO V3 not needed ?
                // c = ClassLoader.getSystemClassLoader().loadClass(className);
                //TODO V3 correct approach ?
                c = Thread.currentThread().getContextClassLoader().loadClass(className);
                obj = c.newInstance();
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, "classloader.load_class_fail", className);
                _logger.log(Level.SEVERE, "classloader.load_class_fail_excp", ex.getMessage());

            }
        }
        return obj;
    }

    /**
     * Prepares the name/value pairs for ActivationSpec. <p>
     * Rule: <p>
     * 1. The name/value pairs are the union of activation-config on
     * standard DD (message-driven) and runtime DD (mdb-resource-adapter)
     * 2. If there are duplicate property settings, the value in runtime
     * activation-config will overwrite the one in the standard
     * activation-config.
     */
    public static Set getMergedActivationConfigProperties(EjbMessageBeanDescriptor msgDesc) {

        Set mergedProps = new HashSet();
        Set runtimePropNames = new HashSet();

        Set runtimeProps = msgDesc.getRuntimeActivationConfigProperties();
        if (runtimeProps != null) {
            Iterator iter = runtimeProps.iterator();
            while (iter.hasNext()) {
                EnvironmentProperty entry = (EnvironmentProperty) iter.next();
                mergedProps.add(entry);
                String propName = (String) entry.getName();
                runtimePropNames.add(propName);
            }
        }

        Set standardProps = msgDesc.getActivationConfigProperties();
        if (standardProps != null) {
            Iterator iter = standardProps.iterator();
            while (iter.hasNext()) {
                EnvironmentProperty entry = (EnvironmentProperty) iter.next();
                String propName = (String) entry.getName();
                if (runtimePropNames.contains(propName))
                    continue;
                mergedProps.add(entry);
            }
        }

        return mergedProps;
    }
    
    public static boolean isJMSRA(String moduleName) {
        if(ConnectorConstants.DEFAULT_JMS_ADAPTER.equals(moduleName)){
            return true;
        }
        return false;
    }

    public static boolean parseBoolean(String enabled) {
        return Boolean.parseBoolean(enabled.toString());
    }

    /**
     * given a resource config bean, returns the resource name / jndi-name
     * @param resource
     * @return resource name / jndi-name
     */
    public static String getResourceName(Resource resource){
        if(resource instanceof BindableResource){
            return ((BindableResource)resource).getJndiName();
        }else if (resource instanceof ResourcePool){
            return ((ResourcePool)resource).getName();
        }else if (resource instanceof ResourceAdapterConfig){
            return ((ResourceAdapterConfig)resource).getName();
        }else if (resource instanceof WorkSecurityMap){
            //TODO V3 toString duckType for WorkSecurityMap config bean ?
            WorkSecurityMap wsm = (WorkSecurityMap)resource;
            return ("resource-adapter name : " + wsm.getResourceAdapterName()
                    + " : security map name : " +  wsm.getName());
        }
        return null;
    }

    /**
     * get the thread pool, given the thread pool id, if availalbe.
     * @param threadPoolId thread pool id
     * @return thread pool
     * @throws NoSuchThreadPoolException when no such thread pool is present
     * @throws ConnectorRuntimeException when unable to get the thread pool
     */
    public static ThreadPool getThreadPool(String threadPoolId)
            throws NoSuchThreadPoolException, ConnectorRuntimeException {

        ThreadPoolManager tpm = getThreadPoolManager();

        if (threadPoolId != null) {
            return tpm.getThreadPool(threadPoolId);
        } else {
            return tpm.getDefaultThreadPool();
        }
    }

    /**
     * JDK 1.6.0_12 & JDK 1.6.0_14 has changes in SE thread pool api.
     * Later we will be using appserver's thread pool.
     * Using the workaround to check the constructor availability and act accordingly.
     * @return thread pool manager
     * @throws ConnectorRuntimeException when unable to provide thread pool manager
     */
    private static ThreadPoolManager getThreadPoolManager() throws ConnectorRuntimeException {
        Constructor defaultConstructor;
        Constructor threadGroupParamConstructor;
        try {
            defaultConstructor = ThreadPoolManagerImpl.class.getConstructor();
            defaultConstructor.setAccessible(true);

            return (ThreadPoolManager)defaultConstructor.newInstance();

        } catch(NoSuchMethodException e) {
            //do nothing. Second trial with a ThreadGroup parameter constructor will be done.
        } catch(Exception e){
            //do nothing.  Second trial with a ThreadGroup parameter constructor will be done.
        }

        try {
            threadGroupParamConstructor = ThreadPoolManagerImpl.class.getConstructor(ThreadGroup.class);
            threadGroupParamConstructor.setAccessible(true);

            ThreadGroup tg = null;
            return (ThreadPoolManager)threadGroupParamConstructor.newInstance(tg);

        } catch(Exception e){
            throw new ConnectorRuntimeException("unable to provide thread pool manager");
        }
    }
}
