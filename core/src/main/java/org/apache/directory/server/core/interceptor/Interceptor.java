/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.server.core.interceptor;


import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.apache.directory.server.core.DirectoryServiceConfiguration;
import org.apache.directory.server.core.configuration.PartitionConfiguration;
import org.apache.directory.server.core.configuration.InterceptorConfiguration;
import org.apache.directory.server.core.interceptor.context.ServiceContext;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.PartitionNexus;
import org.apache.directory.shared.ldap.filter.ExprNode;
import org.apache.directory.shared.ldap.message.ModificationItemImpl;
import org.apache.directory.shared.ldap.name.LdapDN;


/**
 * Filters invocations on {@link PartitionNexus}.  {@link Interceptor}
 * filters most method calls performed on {@link PartitionNexus} just
 * like Servlet filters do.
 * <p/>
 * <h2>Interceptor Chaining</h2>
 * 
 * Interceptors should usually pass the control
 * of current invocation to the next interceptor by calling an appropriate method
 * on {@link NextInterceptor}.  The flow control is returned when the next 
 * interceptor's filter method returns. You can therefore implement pre-, post-,
 * around- invocation handler by how you place the statement.  Otherwise, you
 * can transform the invocation into other(s).
 * <p/>
 * <h3>Pre-invocation Filtering</h3>
 * <pre>
 * public void delete( NextInterceptor nextInterceptor, Name name )
 * {
 *     System.out.println( "Starting invocation." );
 *     nextInterceptor.delete( name );
 * }
 * </pre>
 * <p/>
 * <h3>Post-invocation Filtering</h3>
 * <pre>
 * public void delete( NextInterceptor nextInterceptor, Name name )
 * {
 *     nextInterceptor.delete( name );
 *     System.out.println( "Invocation ended." );
 * }
 * </pre>
 * <p/>
 * <h3>Around-invocation Filtering</h3>
 * <pre>
 * public void delete( NextInterceptor nextInterceptor, Name name )
 * {
 *     long startTime = System.currentTimeMillis();
 *     try
 *     {
 *         nextInterceptor.delete( name );
 *     }
 *     finally
 *     {
 *         long endTime = System.currentTimeMillis();
 *         System.out.println( ( endTime - startTime ) + "ms elapsed." );
 *     }
 * }
 * </pre>
 * <p/>
 * <h3>Transforming invocations</h3>
 * <pre>
 * public void delete( NextInterceptor nextInterceptor, Name name )
 * {
 *     // transform deletion into modification.
 *     Attribute mark = new AttributeImpl( "entryDeleted", "true" );
 *     nextInterceptor.modify( name, DirContext.REPLACE_ATTRIBUTE, mark );
 * }
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 * @see NextInterceptor
 */
public interface Interceptor
{
    /**
     * Intializes this interceptor.  This is invoked by {@link InterceptorChain}
     * when this intercepter is loaded into interceptor chain.
     */
    void init( DirectoryServiceConfiguration factoryCfg, InterceptorConfiguration cfg ) throws NamingException;


    /**
     * Deinitializes this interceptor.  This is invoked by {@link InterceptorChain}
     * when this intercepter is unloaded from interceptor chain.
     */
    void destroy();


    /**
     * Filters {@link PartitionNexus#getRootDSE()} call.
     */
    Attributes getRootDSE( NextInterceptor next ) throws NamingException;


    /**
     * Filters {@link PartitionNexus#getMatchedName(org.apache.directory.shared.ldap.name.LdapDN)} call.
     */
    LdapDN getMatchedName ( NextInterceptor next, LdapDN name ) throws NamingException;


    /**
     * Filters {@link PartitionNexus#getSuffix(org.apache.directory.shared.ldap.name.LdapDN)} call.
     */
    LdapDN getSuffix ( NextInterceptor next, LdapDN name ) throws NamingException;


    /**
     * Filters {@link PartitionNexus#listSuffixes()} call.
     */
    Iterator listSuffixes ( NextInterceptor next ) throws NamingException;


    /**
     * Filters {@link PartitionNexus#addContextPartition(PartitionConfiguration)} call.
     */
    void addContextPartition( NextInterceptor next, PartitionConfiguration cfg ) throws NamingException;


    /**
     * Filters {@link PartitionNexus#removeContextPartition(org.apache.directory.shared.ldap.name.LdapDN)} call.
     */
    void removeContextPartition( NextInterceptor next, LdapDN suffix ) throws NamingException;


    /**
     * Filters {@link PartitionNexus#compare( ServiceContext )} call.
     */
    boolean compare( NextInterceptor next, ServiceContext  compareContext) throws NamingException;


    /**
     * Filters {@link Partition#delete(ServiceContext)} call.
     */
    void delete( NextInterceptor next, ServiceContext deleteContext ) throws NamingException;


    /**
     * Filters {@link Partition#add(ServiceContext)} call.
     */
    void add( NextInterceptor next, ServiceContext addContext ) throws NamingException;


    /**
     * Filters {@link Partition#modify(ServiceContext)} call.
     */
    void modify( NextInterceptor next, ServiceContext modifyContext ) throws NamingException;


    /**
     * Filters {@link Partition#modify(org.apache.directory.shared.ldap.name.LdapDN,javax.naming.directory.ModificationItem[])} call.
     */
    void modify( NextInterceptor next, LdapDN name, ModificationItemImpl[] items ) throws NamingException;


    /**
     * Filters {@link Partition#list(org.apache.directory.shared.ldap.name.LdapDN)} call.
     */
    NamingEnumeration list( NextInterceptor next, LdapDN baseName ) throws NamingException;


    /**
     * Filters {@link Partition#search(org.apache.directory.shared.ldap.name.LdapDN,java.util.Map,org.apache.directory.shared.ldap.filter.ExprNode,javax.naming.directory.SearchControls)} call.
     */
    NamingEnumeration search( NextInterceptor next, LdapDN baseName, Map environment, ExprNode filter,
                              SearchControls searchControls ) throws NamingException;


    /**
     * Filters {@link Partition#lookup(ServiceContext)} call.
     */
    Attributes lookup( NextInterceptor next, ServiceContext lookupContext ) throws NamingException;


    /**
     * Filters {@link Partition#hasEntry(ServiceContext)} call.
     */
    boolean hasEntry( NextInterceptor next, ServiceContext entryContext ) throws NamingException;


    /**
     * Filters {@link Partition#modifyRn(ServiceContext)} call.
     */
    void modifyRn( NextInterceptor next, ServiceContext modifyDnContext ) throws NamingException;


    /**
     * Filters {@link Partition#replace(ServiceContext)} call.
     */
    void replace( NextInterceptor next, ServiceContext replaceContext ) throws NamingException;


    /**
     * Filters {@link Partition#move(ServiceContext)} call.
     */
    void move( NextInterceptor next, ServiceContext moveContext )
        throws NamingException;

    /**
     * Filters {@link Partition#bind(ServiceContext)} call.
     */
    void bind( NextInterceptor next, ServiceContext bindContext )
        throws NamingException;

    /**
     * Filters {@link Partition#unbind(ServiceContext)} call.
     */
    void unbind( NextInterceptor next, ServiceContext unbindContext ) throws NamingException;
}
