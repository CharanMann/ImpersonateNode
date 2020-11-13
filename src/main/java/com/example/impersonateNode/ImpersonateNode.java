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
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.forgerock.json.JsonValue.*;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;

/**
 * A node that checks to see if zero-page login headers have specified username and shared key
 * for this request.
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = ImpersonateNode.Config.class)
public class ImpersonateNode extends AbstractDecisionNode {

    private final Config config;
    private final Logger logger = LoggerFactory.getLogger(ImpersonateNode.class);
    private String impersonatedUserID;
    private String impersonatorUserID;

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

        logger.debug("[ImpersonateNode]: Using Impersonator: " + context.sharedState.get(USERNAME).asString());
        impersonatorUserID = context.sharedState.get(USERNAME).asString();
        impersonatedUserID = config.impersonatedUserID();

        // Get impersonatedUserID from shared state
        if ((impersonatedUserID.indexOf("{{") == 0) && (impersonatedUserID.indexOf("}}") == (impersonatedUserID.length() - 2))) {
            impersonatedUserID = context.sharedState.get(impersonatedUserID.substring(2, impersonatedUserID.length() - 2)).asString();
            logger.debug("[ImpersonateNode]:  Found existing shared state attribute {{impersonated}}: " + impersonatedUserID);
        }

        logger.debug("[ImpersonateNode]: Using Impersonated UserID:" + impersonatedUserID);

        // Changing the shared state "username" to impersonatedUserID
        return goTo(true).replaceSharedState(context.sharedState.copy().put(USERNAME, impersonatedUserID)).build();
    }

    @Override
    public JsonValue getAuditEntryDetail() {
        return json(object(field("impersonatorUserID", impersonatorUserID), field("impersonatedUserID", impersonatedUserID)));
    }

    /**
     * Configuration for the node.
     */
    public interface Config {
        @Attribute(order = 100)
        default String impersonatedUserID() {
            return "{{impersonated}}";
        }
    }
}