/*******************************************************************************
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 *******************************************************************************/
package com.liferay.ide.portlet.ui.util;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.PropertiesUtil;
import com.liferay.ide.properties.core.PropertiesFileLookup;
import com.liferay.ide.properties.core.PropertiesFileLookup.KeyInfo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.search.core.util.DOMUtils;
import org.w3c.dom.Node;


/**
 * @author Gregory Amerson
 */
@SuppressWarnings( "restriction" )
public class NodeUtils
{
    public static Node getMessageKey( IDOMNode currentNode )
    {
        Node retval = null;

        final boolean messageNode = currentNode != null &&
                        currentNode.getNodeName() != null &&
                        currentNode.getNodeType() == Node.ELEMENT_NODE &&
                        ( currentNode.getNodeName().endsWith( "message" ) ||
                          currentNode.getNodeName().endsWith( "error" ) );

        if( messageNode )
        {
            final Node key = currentNode.getAttributes().getNamedItem( "key" );

            if( key != null && ( ! CoreUtil.isNullOrEmpty( key.getNodeValue() ) ) )
            {
                retval = key;
            }
        }

        return retval;
    }

    public static MessageKey[] findMessageKeys( IDocument document, String key, boolean loadValues  )
    {
        MessageKey[] retval = null;

        final IFile file = DOMUtils.getFile( document );

        if( file != null && file.exists() )
        {
            final IJavaProject project = JavaCore.create( file.getProject() );

            if( project != null && project.exists() )
            {
                final List<MessageKey> keys = new ArrayList<MessageKey>();

                for( final IFolder src : CoreUtil.getSrcFolders( project.getProject() ) )
                {
                    final IFile[] props = PropertiesUtil.visitPropertiesFiles( src, ".*" );

                    for( final IFile prop : props )
                    {
                        try
                        {
                            final KeyInfo info = new PropertiesFileLookup( prop.getContents(), key, loadValues ).getKeyInfo( key );

                            if( info != null && info.offset >= 0 )
                            {
                                keys.add( new MessageKey( prop, key, info.offset, info.length, info.value ) );
                            }
                        }
                        catch( CoreException e )
                        {
                        }
                    }
                }

                retval = keys.toArray( new MessageKey[0] );
            }
        }

        return retval;
    }
}