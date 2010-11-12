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
package org.apache.directory.shared.kerberos.codec.authorizationData.actions;


import org.apache.directory.shared.asn1.ber.Asn1Container;
import org.apache.directory.shared.asn1.ber.grammar.GrammarAction;
import org.apache.directory.shared.asn1.ber.tlv.TLV;
import org.apache.directory.shared.asn1.ber.tlv.Value;
import org.apache.directory.shared.asn1.codec.DecoderException;
import org.apache.directory.shared.asn1.util.IntegerDecoder;
import org.apache.directory.shared.asn1.util.IntegerDecoderException;
import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.kerberos.codec.authorizationData.AuthorizationDataContainer;
import org.apache.directory.shared.kerberos.components.AuthorizationData;
import org.apache.directory.shared.ldap.util.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to store the AuthorizationData adType
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AuthorizationDataAdType extends GrammarAction
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( AuthorizationDataAdType.class );

    /** Speedup for logs */
    private static final boolean IS_DEBUG = LOG.isDebugEnabled();


    /**
     * Instantiates a new AuthorizationDataAdType action.
     */
    public AuthorizationDataAdType()
    {
        super( "AuthorizationData adType" );
    }


    /**
     * {@inheritDoc}
     */
    public void action( Asn1Container container ) throws DecoderException
    {
        AuthorizationDataContainer authDataContainer = ( AuthorizationDataContainer ) container;

        TLV tlv = authDataContainer.getCurrentTLV();

        // The Length should not be null
        if ( tlv.getLength() == 0 )
        {
            LOG.error( I18n.err( I18n.ERR_04066 ) );

            // This will generate a PROTOCOL_ERROR
            throw new DecoderException( I18n.err( I18n.ERR_04067 ) );
        }
        
        AuthorizationData authData = authDataContainer.getAuthorizationData();

        // Creates a new AD
        authData.createNewAD();
        
        // The AuthorizationData data is an integer
        Value value = tlv.getValue();
        
        try
        {
            int adType = IntegerDecoder.parse( value );

            authData.setCurrentAdType( adType );

            if ( IS_DEBUG )
            {
                LOG.debug( "adType : " + adType );
            }
        }
        catch ( IntegerDecoderException ide )
        {
            LOG.error( I18n.err( I18n.ERR_04070, StringTools.dumpBytes( value.getData() ), ide
                .getLocalizedMessage() ) );

            // This will generate a PROTOCOL_ERROR
            throw new DecoderException( ide.getMessage() );
        }
    }
}