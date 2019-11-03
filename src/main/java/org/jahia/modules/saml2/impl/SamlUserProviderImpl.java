package org.jahia.modules.saml2.impl;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.RandomStringUtils;
import org.jahia.modules.saml2.SAML2Constants;
import org.jahia.modules.saml2.utils.JCRConstants;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.pac4j.saml.profile.SAML2Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.modules.saml2.service.SamlUserProvider;
import org.jahia.modules.saml2.service.SamlUserProviderManager;

public class SamlUserProviderImpl implements SamlUserProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlUserProviderImpl.class);
    private static final String SERVICE_NAME = "jcrSamlUserProvider";
    private SamlUserProviderManager samlUserProviderManager;

    @Override
    public boolean createUser(String email, SAML2Profile saml2Profile, HttpServletRequest request, String siteKey) {

        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession((JCRSessionWrapper session) -> {
                final JahiaUserManagerService jahiaUserManagerService = JahiaUserManagerService.getInstance();
                jahiaUserManagerService.createUser(email, siteKey,
                        RandomStringUtils.randomAscii(18), initialProperties(saml2Profile), session);
                session.save();
                return true;
            });
        } catch (RepositoryException ex) {
            LOGGER.error(String.format("Impossible to create user %s in the JCR", email), ex);
        }
        return false;
    }

    private Properties initialProperties(SAML2Profile saml2Profile) {
        Properties properties = new Properties();
        properties.setProperty(JCRConstants.USER_PROPERTY_EMAIL, this.getProfileAttribute(SAML2Constants.SAML2_USER_PROPERTY_EMAIL, saml2Profile));
        properties.setProperty(JCRConstants.USER_PROPERTY_LASTNAME, this.getProfileAttribute(SAML2Constants.SAML2_USER_PROPERTY_LASTNAME, saml2Profile));
        properties.setProperty(JCRConstants.USER_PROPERTY_FIRSTNAME, this.getProfileAttribute(SAML2Constants.SAML2_USER_PROPERTY_FIRSTNAME, saml2Profile));
        return properties;
    }

    private String getProfileAttribute(String name, SAML2Profile saml2Profile) {
        ArrayList<String> strings = saml2Profile.getAttribute(name, ArrayList.class);
        if (Objects.nonNull(strings)) {
            return strings.get(0);
        }
        return "";
    }

    @Override
    public void start() {
        samlUserProviderManager.addProvider(SERVICE_NAME);
    }

    @Override
    public void stop() {
        samlUserProviderManager.removeProvider(SERVICE_NAME);
    }

    public void setSamlUserProviderManager(SamlUserProviderManager samlUserProviderManager) {
        this.samlUserProviderManager = samlUserProviderManager;
    }

    public SamlUserProviderManager getSamlUserProviderManager() {
        return samlUserProviderManager;
    }
}
