package org.aksw.facete3.app.vaadin.session;

import java.io.Serializable;

import org.aksw.jenax.model.foaf.domain.api.FoafOnlineAccount;
import org.aksw.jenax.model.foaf.domain.api.FoafPerson;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;

@Component
@SessionScope
public class UserSession implements Serializable {

    protected FoafOnlineAccount activeAccount;

    protected UserSession() {
    }

    public FoafOnlineAccount getUser() {
        activeAccount = activeAccount != null ? activeAccount : loadUser();

        // Check if the user is initialized
//        if (resources == null) {
//            initResources();
//        }


        return activeAccount;
    }

    protected FoafOnlineAccount loadUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object rawPrincipal = authentication.getPrincipal();

        Model model = ModelFactory.createDefaultModel();
        FoafOnlineAccount result = model.createResource().as(FoafOnlineAccount.class);
        if (rawPrincipal instanceof OAuth2AuthenticatedPrincipal) {
            OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal)rawPrincipal;


            FoafPerson agent = model.createResource().as(FoafPerson.class)
                .setFamilyName(principal.getAttribute("family_name"))
                .setMbox(principal.getAttribute("email"))
                .setName(principal.getAttribute("username"))
                .setDepiction(principal.getAttribute("avatar_url"))
                .asFoafPerson()
                ;

            result = model.createResource().as(FoafOnlineAccount.class)
                .setAccountName(principal.getAttribute("username"))

                // Is this a good fit for web_url? Value may be e.g. https://gitlab.com/Aklakan
                .setAccountServiceHomepage(principal.getAttribute("web_url"))
                ;
            result.setOwner(agent);
        } else if (rawPrincipal instanceof String) {
            String principal = (String)rawPrincipal;
            result.setAccountName(principal);
        } else if (rawPrincipal == null) {
            throw new NullPointerException("Principal was null");
        } else {
            throw new RuntimeException("Unknown principal type: " + rawPrincipal.getClass());
        }

        return result;
    }

    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null;
    }


    public void logout() {
        String LOGOUT_SUCCESS_URL = "/";
        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
                null);

        // resources.clear();
        activeAccount = null;
    }
}
