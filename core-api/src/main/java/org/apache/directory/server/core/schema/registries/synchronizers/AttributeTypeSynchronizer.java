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
package org.apache.directory.server.core.schema.registries.synchronizers;


import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.interceptor.context.ModifyOperationContext;
import org.apache.directory.shared.ldap.constants.MetaSchemaConstants;
import org.apache.directory.shared.ldap.constants.SchemaConstants;
import org.apache.directory.shared.ldap.exception.LdapOperationNotSupportedException;
import org.apache.directory.shared.ldap.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.apache.directory.shared.ldap.name.Rdn;
import org.apache.directory.shared.ldap.schema.AttributeType;
import org.apache.directory.shared.ldap.schema.registries.AttributeTypeRegistry;
import org.apache.directory.shared.ldap.schema.registries.Registries;
import org.apache.directory.shared.ldap.schema.registries.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A handler for operations performed to add, delete, modify, rename and 
 * move schema normalizers.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class AttributeTypeSynchronizer extends AbstractRegistrySynchronizer
{
    /** A logger for this class */
    private static final Logger LOG = LoggerFactory.getLogger( AttributeTypeSynchronizer.class );

    /** A reference to the AttributeType registry */
    private final AttributeTypeRegistry atRegistry;

    /**
     * Creates a new instance of AttributeTypeSynchronizer.
     *
     * @param registries The global registries
     * @throws Exception If the initialization failed
     */
    public AttributeTypeSynchronizer( Registries registries ) throws Exception
    {
        super( registries );
        this.atRegistry = registries.getAttributeTypeRegistry();
    }

    
    /**
     * {@inheritDoc}
     */
    public void add( ServerEntry entry ) throws Exception
    {
        LdapDN dn = entry.getDn();
        LdapDN parentDn = ( LdapDN ) dn.clone();
        parentDn.remove( parentDn.size() - 1 );
        
        // The parent DN must be ou=attributetypes,cn=<schemaName>,ou=schema
        checkParent( parentDn, atRegistry, SchemaConstants.ATTRIBUTE_TYPE );
        
        // The new schemaObject's OID must not already exist
        checkOidIsUnique( entry );
        
        // Build the new AttributeType from the given entry
        String schemaName = getSchemaName( dn );
        AttributeType at = factory.getAttributeType( entry, registries, schemaName );
        
        // At this point, the constructed AttributeType has not been checked against the 
        // existing Registries. It may be broken (missing SUP, or such), it will be checked
        // there, if the schema and the AttributeType are both enabled.
        Schema schema = registries.getLoadedSchema( schemaName );
        
        if ( schema.isEnabled() && at.isEnabled() )
        {
            at.applyRegistries( registries );
        }
        
        // Associates this AttributeType with the schema
        addToSchema( at, schemaName );
        
        // Don't inject the modified element if the schema is disabled
        if ( isSchemaEnabled( schemaName ) )
        {
            atRegistry.register( at );
            
            // Update the referenced objects
            // The Syntax,
            registries.addReference( at, at.getSyntax() );

            // The Superior if any
            registries.addReference( at, at.getSuperior() );

            // The MatchingRules
            registries.addReference( at, at.getEquality() );
            registries.addReference( at, at.getOrdering() );
            registries.addReference( at, at.getSubstring() );

            LOG.debug( "Added {} into the enabled schema {}", dn.getUpName(), schemaName );
        }
        else
        {
            registerOids( at );
            LOG.debug( "Added {} into the disabled schema {}", dn.getUpName(), schemaName );
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean modify( ModifyOperationContext opContext, ServerEntry targetEntry, boolean cascade ) 
        throws Exception
    {
        LdapDN name = opContext.getDn();
        ServerEntry entry = opContext.getEntry();
        String schemaName = getSchemaName( name );
        String oid = getOid( entry );
        AttributeType at = factory.getAttributeType( targetEntry, registries, schemaName );
        
        if ( isSchemaEnabled( schemaName ) )
        {
            if ( atRegistry.contains( oid ) )
            {
                atRegistry.unregister( oid );
            }
            
            atRegistry.register( at );
            
            return SCHEMA_MODIFIED;
        }
        
        return SCHEMA_UNCHANGED;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void delete( ServerEntry entry, boolean cascade ) throws Exception
    {
        LdapDN dn = entry.getDn();
        LdapDN parentDn = ( LdapDN ) dn.clone();
        parentDn.remove( parentDn.size() - 1 );
        
        // The parent DN must be ou=attributetypes,cn=<schemaName>,ou=schema
        checkParent( parentDn, atRegistry, SchemaConstants.ATTRIBUTE_TYPE );
        
        // Get the AttributeType from the given entry ( it has been grabbed from the server earlier)
        String schemaName = getSchemaName( entry.getDn() );
        AttributeType attributeType = factory.getAttributeType( entry, registries, schemaName );
        
        // Applies the Registries to this AttributeType 
        Schema schema = registries.getLoadedSchema( schemaName );
        
        if ( schema.isEnabled() && attributeType.isEnabled() )
        {
            attributeType.applyRegistries( registries );
        }
        
        String oid = attributeType.getOid();
        
        // Depending on the fact that the schema is enabled or disabled, we will have
        // to unregister the AttributeType from the registries or not. In any case,
        // we have to remove it from the schemaPartition.
        // We also have to check that the removal will let the server Registries in a 
        // stable state, which means in this case that we don't have any AttributeType
        // directly inherit from the removed AttributeType, and that no ObjectClass
        // has this AttributeType in its MAY or MUST...
        // We will also have to remove an index that has been set on this AttributeType.
        if ( isSchemaEnabled( schemaName ) )
        {
            if ( registries.isReferenced( attributeType ) )
            {
                String msg = "Cannot delete " + entry.getDn().getUpName() + ", as there are some " +
                    " dependant SchemaObjects :\n" + getReferenced( attributeType );
                LOG.warn( msg );
                throw new LdapOperationNotSupportedException( msg, ResultCodeEnum.UNWILLING_TO_PERFORM );
            }
        }

        // Remove the AttributeType from the schema content
        deleteFromSchema( attributeType, schemaName );

        // Update the Registries now
        if ( atRegistry.contains( oid ) )
        {
            // Update the references.
            // The Syntax
            registries.delReference( attributeType, attributeType.getSyntax() );
            
            // The Superior
            registries.delReference( attributeType, attributeType.getSuperior() );
            
            // The MatchingRules
            registries.delReference( attributeType, attributeType.getEquality() );
            registries.delReference( attributeType, attributeType.getOrdering() );
            registries.delReference( attributeType, attributeType.getSubstring() );
            
            // Update the Registry
            atRegistry.unregister( attributeType.getOid() );
            
            LOG.debug( "Removed {} from the enabled schema {}", attributeType, schemaName );
        }
        else
        {
            unregisterOids( attributeType );
            LOG.debug( "Removed {} from the disabled schema {}", attributeType, schemaName );
        }
    }


    /**
     * {@inheritDoc}
     */
    public void rename( ServerEntry entry, Rdn newRdn, boolean cascade ) throws Exception
    {
        String schemaName = getSchemaName( entry.getDn() );
        AttributeType oldAt = factory.getAttributeType( entry, registries, schemaName );

        // Inject the new OID
        ServerEntry targetEntry = ( ServerEntry ) entry.clone();
        String newOid = ( String ) newRdn.getValue();
        checkOidIsUnique( newOid );
        targetEntry.put( MetaSchemaConstants.M_OID_AT, newOid );

        // Inject the new DN
        LdapDN newDn = new LdapDN( targetEntry.getDn() );
        newDn.remove( newDn.size() - 1 );
        newDn.add( newRdn );
        targetEntry.setDn( newDn );
        
        AttributeType at = factory.getAttributeType( targetEntry, registries, schemaName );

        if ( isSchemaEnabled( schemaName ) )
        {
            // Check that the entry has no descendant
            if ( atRegistry.hasDescendants( oldAt.getOid() ) )
            {
                String msg = "Cannot rename " + entry.getDn().getUpName() + " to " + newDn + 
                    " as the later has descendants' AttributeTypes";
                
                throw new LdapOperationNotSupportedException( msg, ResultCodeEnum.UNWILLING_TO_PERFORM );
            }
            
            atRegistry.unregister( oldAt.getOid() );
            atRegistry.register( at );
        }
        else
        {
            unregisterOids( oldAt );
            registerOids( at );
        }
    }


    public void moveAndRename( LdapDN oriChildName, LdapDN newParentName, Rdn newRn, boolean deleteOldRn,
        ServerEntry entry, boolean cascade ) throws Exception
    {
        checkParent( newParentName, atRegistry, SchemaConstants.ATTRIBUTE_TYPE );
        String oldSchemaName = getSchemaName( oriChildName );
        String newSchemaName = getSchemaName( newParentName );
        AttributeType oldAt = factory.getAttributeType( entry, registries, oldSchemaName );
        ServerEntry targetEntry = ( ServerEntry ) entry.clone();
        String newOid = ( String ) newRn.getValue();
        targetEntry.put( MetaSchemaConstants.M_OID_AT, newOid );
        checkOidIsUnique( newOid );
        AttributeType newAt = factory.getAttributeType( targetEntry, registries, newSchemaName );

        
        if ( !isSchemaLoaded( oldSchemaName ) )
        {
            String msg = "Cannot move a schemaObject from a not loaded schema " + oldSchemaName;
            LOG.warn( msg );
            throw new LdapOperationNotSupportedException( msg, ResultCodeEnum.UNWILLING_TO_PERFORM );
        }
        
        if ( !isSchemaLoaded( newSchemaName ) )
        {
            String msg = "Cannot move a schemaObject to a not loaded schema " + newSchemaName;
            LOG.warn( msg );
            throw new LdapOperationNotSupportedException( msg, ResultCodeEnum.UNWILLING_TO_PERFORM );
        }
        
        deleteFromSchema( oldAt, oldSchemaName );
        addToSchema( newAt, newSchemaName );

        if ( isSchemaEnabled( oldSchemaName ) )
        {
            atRegistry.unregister( oldAt.getOid() );
        }
        else
        {
            unregisterOids( oldAt );
        }

        if ( isSchemaEnabled( newSchemaName ) )
        {
            atRegistry.register( newAt );
        }
        else
        {
            registerOids( newAt );
        }
    }


    public void move( LdapDN oriChildName, LdapDN newParentName, ServerEntry entry, boolean cascade ) 
        throws Exception
    {
        checkParent( newParentName, atRegistry, SchemaConstants.ATTRIBUTE_TYPE );
        String oldSchemaName = getSchemaName( oriChildName );
        String newSchemaName = getSchemaName( newParentName );
        AttributeType oldAt = factory.getAttributeType( entry, registries, oldSchemaName );
        AttributeType newAt = factory.getAttributeType( entry, registries, newSchemaName );
        
        if ( !isSchemaLoaded( oldSchemaName ) )
        {
            String msg = "Cannot move a schemaObject from a not loaded schema " + oldSchemaName;
            LOG.warn( msg );
            throw new LdapOperationNotSupportedException( msg, ResultCodeEnum.UNWILLING_TO_PERFORM );
        }
        
        if ( !isSchemaLoaded( newSchemaName ) )
        {
            String msg = "Cannot move a schemaObject to a not loaded schema " + newSchemaName;
            LOG.warn( msg );
            throw new LdapOperationNotSupportedException( msg, ResultCodeEnum.UNWILLING_TO_PERFORM );
        }
        
        deleteFromSchema( oldAt, oldSchemaName );
        addToSchema( newAt, newSchemaName );
        
        if ( isSchemaEnabled( oldSchemaName ) )
        {
            atRegistry.unregister( oldAt.getOid() );
        }
        else
        {
            unregisterOids( oldAt );
        }
        
        if ( isSchemaEnabled( newSchemaName ) )
        {
            atRegistry.register( newAt );
        }
        else
        {
            registerOids( newAt );
        }
    }
}
