/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.server.core.api.administrative;


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.subtree.AdministrativeRole;


/**
 * An class used to create an AccessControl AAP
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AccessControlAAP extends AccessControlAdministrativePoint
{
    /**
     * Create an instance of AccessControl AdministrativePoint
     *
     * @param dn The AdministrativePoint Dn
     * @param uuid The AdministrativePoint UUID
     */
    public AccessControlAAP( Dn dn, String uuid )
    {
        super( dn, uuid, AdministrativeRole.AccessControlSpecificArea );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AdministrativePoint getParent()
    {
        return parent;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSpecific()
    {
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInner()
    {
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutonomous()
    {
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "AccessControl AAP : " + super.toString();
    }
}
