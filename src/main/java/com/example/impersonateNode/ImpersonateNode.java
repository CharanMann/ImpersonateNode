/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2018 ForgeRock AS.
 */


package com.example.impersonateNode;

import com.google.inject.assistedinject.Assisted;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;

import javax.inject.Inject;

import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;

/**
 * A node that checks to see if zero-page login headers have specified username and shared key
 * for this request.
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = ImpersonateNode.Config.class)
public class ImpersonateNode extends AbstractDecisionNode {

    private final static String DEBUG_FILE = "ImpersonateNode";
    private static final String DEFAULT_IMP_USER_SHARED_STATE = "Impersonator";
    private final Config config;
    protected Debug debug = Debug.getInstance(DEBUG_FILE);

    /**
     * Create the node.
     *
     * @param config The service config.
     * @throws NodeProcessException If the configuration was not valid.
     */
    @Inject
    public ImpersonateNode(@Assisted Config config) throws NodeProcessException {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {

        String impersonateUsername;

        // Get impersonator from shared state
        if (context.sharedState.isDefined(DEFAULT_IMP_USER_SHARED_STATE)) {
            impersonateUsername = context.sharedState.get(DEFAULT_IMP_USER_SHARED_STATE).asString();
        } else {
            // Get impersonator from node config
            impersonateUsername = config.impersonator();
        }

        debug.message("Impersonate node using impersonator:" + impersonateUsername);

        // Changing the shared state "username" to impersonator
        return goTo(true).replaceSharedState(context.sharedState.copy().put(USERNAME, impersonateUsername)).build();
    }

    /**
     * Configuration for the node.
     */
    public interface Config {
        @Attribute(order = 100)
        default String impersonator() {
            return "Impersonator UserID";
        }
    }
}