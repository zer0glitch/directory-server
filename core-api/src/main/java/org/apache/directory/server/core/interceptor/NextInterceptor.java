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


import java.util.Set;

import org.apache.directory.server.core.entry.ClonedServerEntry;
import org.apache.directory.server.core.filtering.EntryFilteringCursor;
import org.apache.directory.server.core.interceptor.context.AddOperationContext;
import org.apache.directory.server.core.interceptor.context.BindOperationContext;
import org.apache.directory.server.core.interceptor.context.CompareOperationContext;
import org.apache.directory.server.core.interceptor.context.DeleteOperationContext;
import org.apache.directory.server.core.interceptor.context.EntryOperationContext;
import org.apache.directory.server.core.interceptor.context.GetMatchedNameOperationContext;
import org.apache.directory.server.core.interceptor.context.GetRootDSEOperationContext;
import org.apache.directory.server.core.interceptor.context.GetSuffixOperationContext;
import org.apache.directory.server.core.interceptor.context.ListOperationContext;
import org.apache.directory.server.core.interceptor.context.ListSuffixOperationContext;
import org.apache.directory.server.core.interceptor.context.LookupOperationContext;
import org.apache.directory.server.core.interceptor.context.ModifyOperationContext;
import org.apache.directory.server.core.interceptor.context.MoveAndRenameOperationContext;
import org.apache.directory.server.core.interceptor.context.MoveOperationContext;
import org.apache.directory.server.core.interceptor.context.RenameOperationContext;
import org.apache.directory.server.core.interceptor.context.SearchOperationContext;
import org.apache.directory.server.core.interceptor.context.UnbindOperationContext;
import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.exception.LdapException;
import org.apache.directory.shared.ldap.name.DN;


/**
 * Represents the next {@link Interceptor} in the interceptor chain.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 * @see Interceptor
 * @see InterceptorChain
 */
public interface NextInterceptor
{
    /**
     * Calls the next interceptor's {@link Interceptor#compare( NextInterceptor, CompareOperationContext )}.
     */
    boolean compare( CompareOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#getRootDSE( NextInterceptor, GetRootDSEOperationContext )}.
     */
    ClonedServerEntry getRootDSE( GetRootDSEOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#getMatchedName( NextInterceptor, GetMatchedNameOperationContext )}.
     */
    DN getMatchedName( GetMatchedNameOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#getSuffix( NextInterceptor, GetSuffixOperationContext )}.
     */
    DN getSuffix( GetSuffixOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#listSuffixes( NextInterceptor, ListSuffixOperationContext )}.
     */
    Set<String> listSuffixes( ListSuffixOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#delete(NextInterceptor, DeleteOperationContext )}.
     */
    void delete( DeleteOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#add( NextInterceptor, AddOperationContext )}.
     */
    void add( AddOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#modify( NextInterceptor, ModifyOperationContext )}.
     */
    void modify( ModifyOperationContext opContext ) throws LdapException;

    /**
     * Calls the next interceptor's {@link Interceptor#list( NextInterceptor, ListOperationContext )}.
     */
    EntryFilteringCursor list( ListOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#search( NextInterceptor, SearchOperationContext opContext )}.
     */
    EntryFilteringCursor search( SearchOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#lookup( NextInterceptor, LookupOperationContext )}.
     */
    Entry lookup( LookupOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#hasEntry( NextInterceptor, EntryOperationContext )}.
     */
    boolean hasEntry( EntryOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#rename( NextInterceptor, RenameOperationContext )}.
     */
    void rename( RenameOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#move( NextInterceptor, MoveOperationContext )}.
     */
    void move( MoveOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#moveAndRename( NextInterceptor, MoveAndRenameOperationContext )}.
     */
    void moveAndRename( MoveAndRenameOperationContext opContext ) throws LdapException;


    /**
     * Calls the next interceptor's {@link Interceptor#bind( NextInterceptor, BindOperationContext )}
     */
    void bind( BindOperationContext opContext ) throws LdapException;

    /**
     * Calls the next interceptor's {@link Interceptor#unbind( NextInterceptor, UnbindOperationContext )}
     */
    void unbind( UnbindOperationContext opContext ) throws LdapException;
}
