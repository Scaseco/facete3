package org.aksw.facete3.app.vaadin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.PermitAll;

import org.aksw.facete3.app.vaadin.components.ExplorerTabs;
import org.aksw.facete3.app.vaadin.plugin.ComponentPlugin;
import org.aksw.facete3.app.vaadin.session.UserSession;
import org.aksw.jenax.model.foaf.domain.api.FoafAgent;
import org.aksw.jenax.model.foaf.domain.api.FoafOnlineAccount;
import org.aksw.vaadin.common.component.tab.RouteTabs;
import org.aksw.vaadin.common.provider.util.TaskControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route("")
@PWA(name = "Facete3 Vaadin Application", shortName = "Facete3",
        description = "This is an example Vaadin application.") // , enableInstallPrompt = true)
@CssImport(value = "./styles/shared-styles.css", include = "lumo-badge")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@CssImport(value = "./styles/vaadin-grid-styles.css", themeFor = "vaadin-grid")
@CssImport(value = "./styles/vaadin-tab-styles.css", themeFor = "vaadin-tab")
@CssImport(value = "./styles/vaadin-select-text-field-styles.css", themeFor = "vaadin-select-text-field")
@CssImport(value = "./styles/vaadin-select-styles.css", themeFor = "vaadin-select")
@CssImport(value = "./styles/vaadin-text-area-styles.css", themeFor = "vaadin-text-area")
@CssImport(value = "./styles/flow-component-renderer-styles.css", themeFor = "flow-component-renderer")
@CssImport(value = "./styles/vaadin-grid-tree-toggle-styles.css", themeFor = "vaadin-grid-tree-toggle")
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
// @CssImport(value = "./styles/vstepper-styles.css", themeFor = "v-stepper")
//@Theme(themeClass = Lumo.class)
@Theme(value = Lumo.class)
@PermitAll
@Push(PushMode.AUTOMATIC)
@EnableAsync
//@HtmlImport(value="frontend://bower_components/vaadin-lumo-styles/badge.html")
public class AppLayoutFacete3 extends AppLayout {

    // Ensure Jena plugins are fully loaded before
    // beans are passed to components
//    static { JenaSystem.init(); }

    protected static final long serialVersionUID = 1;
//    protected Config config;

    protected DrawerToggle drawerToggle;

    protected MenuBar menuBar;
    protected UserSession userSession;
    protected TaskControlRegistryImpl taskControlRegistry;

    @Autowired
    public AppLayoutFacete3(ConfigFaceteVaadin config, UserSession userSession, TaskControlRegistryImpl taskControlRegistry) {
//        VaadinSession.getCurrent().setErrorHandler(eh -> {
//            Notification.show(ExceptionUtils.getRootCauseMessage(eh.getThrowable()));
//        });
        this.userSession = userSession;
        this.taskControlRegistry = taskControlRegistry;

        UI ui = UI.getCurrent();
        taskControlRegistry.setUi(ui);
        
        Facete3Wrapper.initJena();

        HorizontalLayout navbarLayout = new HorizontalLayout();
        
        drawerToggle = new DrawerToggle();
        // navbarLayout.add(drawerToggle);
        
        navbarLayout.setWidthFull();
        navbarLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button appSettingsBtn = new Button(new Icon(VaadinIcon.COG));
        navbarLayout.add(appSettingsBtn);


        Dialog dialog = new Dialog();
        // DatasetSelectorComponent datasetManager = new DatasetSelectorComponent();

        // dialog.add(datasetManager);
        dialog.setSizeFull();

        appSettingsBtn.addClickListener(event -> {
            dialog.open();
//          input.focus();
        });


        Button themeToggleButton = new Button(new Icon(VaadinIcon.LIGHTBULB), click -> {
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
            if (themeList.contains(Lumo.DARK)) {
                themeList.remove(Lumo.DARK);
            } else {
                themeList.add(Lumo.DARK);
            }
        });
        navbarLayout.add(themeToggleButton);


//        Div div = new Div();
//        div.setText("Hello " + userSession.getUser().getFirstName() + " " + userSession.getUser().getLastName());
//        div.getElement().getStyle().set("font-size", "xx-large");

        menuBar = new MenuBar();

        refreshMenuBar();

        // setAlignItems(Alignment.CENTER);
        navbarLayout.add(menuBar);
        //add(div, image, logoutButton);

//        Span item = new Span();
//        Span icon = new Span();
//        icon.addClassName("color-swatch");
//        icon.getStyle().set("background-color", "var(--lumo-success-text-color)");
//
//        icon.setText("");
//        item.add(icon);
//        item.add("online");
//        //item.add(new Span("online"));
//
////        icon.getStyle().set("background-color", "var(--lumo-success-text-color)");
//        navbarLayout.add(item);
//
//        Dialog dialog = new Dialog();
//        SparqlEndpointForm input = new SparqlEndpointForm();
//
//        dialog.add(input);
//
//        appSettingsBtn.addClickListener(event -> {
//            dialog.open();
////            input.focus();
//        });

        addToNavbar(drawerToggle, navbarLayout);
        setContent(getAppContent(config));
        if (System.getProperty("UI.DISABLE.NAVBAR") != null) {
            navbarLayout.setVisible(false);
        }
        
        addToDrawer(getTabs());
        setDrawerOpened(false);
    }

    private Tabs getTabs() {

        RouteTabs tabs = new RouteTabs();
        tabs.add(
                RouteTabs.newTab(VaadinIcon.HOME, "Home", AppLayoutFacete3.class)
//                createTab(VaadinIcon.EYE, "Browse", BrowseRepoView.class),
//                createTab(VaadinIcon.CONNECT, "Connections", ConnectionMgmtView.class),
//                createTab(VaadinIcon.DATABASE, "Catalogs", CatalogMgmtView.class)
        );
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
    //  Tabs tabs = new Tabs();
    //  tabs.add(
    //    createTab(VaadinIcon.HOME, "Home", DmanLandingPageView.class),
    //    createTab(VaadinIcon.FOLDER_ADD, "New Data Project", NewDataProjectView.class),
    //    createTab(VaadinIcon.EYE, "Browse", BrowseRepoView.class),
    //    createTab(VaadinIcon.CONNECT, "Connections", ConnectionMgmtView.class)
    //  );
    //  tabs.setOrientation(Tabs.Orientation.VERTICAL);
      return tabs;
    }
    
    
    protected void refreshMenuBar() {
        menuBar.removeAll();
        menuBar.setOpenOnHover(true);

        Span actionMenuArea = new Span();
        Button progressBarBtn = new Button(VaadinIcon.PROGRESSBAR.create());
        
        Span taskCountPendingSpan = new Span();
        taskCountPendingSpan.getElement().getThemeList().add("badge pill");

        Span taskCountSuccessSpan = new Span();
        taskCountSuccessSpan.getElement().getThemeList().add("badge success pill");

        Span taskCountErrorSpan = new Span();
        taskCountErrorSpan.getElement().getThemeList().add("badge error pill");

        actionMenuArea.add(progressBarBtn, taskCountPendingSpan, taskCountSuccessSpan, taskCountErrorSpan);

        MenuItem actionMenu = menuBar.addItem(actionMenuArea);
        
        SubMenu actionSubMenu = actionMenu.getSubMenu();

        HierarchicalDataProvider<TaskControl<?>, ?> actionTdp = taskControlRegistry.getTreeDataProvider();

        TreeGrid<TaskControl<?>> actionGrid = new TreeGrid<>();
        actionGrid.setWidth("300px");
        actionGrid.setAllRowsVisible(true);

		actionGrid.addComponentColumn(o -> {

			Div progressBarLabel = new Div();
			progressBarLabel.setText("Task [" + o.getName() + "]");

			ProgressBar progressBar = new ProgressBar();

			Div progressBarWrapper = new Div(progressBarLabel, progressBar);
			progressBarWrapper.setWidthFull();

			HorizontalLayout r = new HorizontalLayout();
			r.add(progressBarWrapper);
			r.setFlexGrow(1, progressBarWrapper);

			if (o.isComplete()) {
				Throwable throwable = o.getThrowable();
				boolean isSuccess = throwable == null;

				progressBar.setMin(0f);
				progressBar.setMax(1f);
				progressBar.setValue(1f);
				if (isSuccess) {
					progressBar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
					Icon icon = VaadinIcon.CHECK_CIRCLE_O.create();
					icon.getElement().getThemeList().add("badge success pill");
					r.add(icon);
				} else {
					progressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
					Icon icon = VaadinIcon.CLOSE_CIRCLE_O.create();
					icon.getElement().getThemeList().add("badge error pill");
					r.add(icon);
				}
			} else {
				progressBar.setIndeterminate(true);
				Icon icon = VaadinIcon.STOP.create();
				icon.getElement().getThemeList().add("badge error pill");
				Button cancelBtn = new Button(icon);
				r.add(cancelBtn);
				cancelBtn.addClickListener(ev -> {
					o.abort();
				});
			}

			return r;
		}).setKey("value");
        
        actionGrid.setDataProvider(actionTdp);
        actionTdp.addDataProviderListener(ev -> {
        	// pending success error
        	long[] pse = {0, 0, 0};
        	
        	try (Stream<TaskControl<?>> stream = actionTdp.fetchChildren(new HierarchicalQuery<>(null, null))) {        		
        		List<TaskControl<?>> tasks = stream.collect(Collectors.toList());
        		for (TaskControl<?> task: tasks) {
        			int classify = !task.isComplete() ? 0 : task.getThrowable() == null ? 1 : 2;
        			++pse[classify];
        		}
        	}
        	
        	taskCountPendingSpan.setVisible(pse[0] != 0);
        	taskCountPendingSpan.setText(Long.toString(pse[0]));

        	taskCountSuccessSpan.setVisible(pse[1] != 0);
        	taskCountSuccessSpan.setText(Long.toString(pse[1]));

        	taskCountErrorSpan.setVisible(pse[2] != 0);
        	taskCountErrorSpan.setText(Long.toString(pse[2]));
        });
        

        actionSubMenu.add(actionGrid);

        //actionList.add(new HorizontalLayout(new Span("Action 1"), new Button(VaadinIcon.STOP.create())));

        Optional<FoafOnlineAccount> user = Optional.ofNullable(userSession.getUser());

        String accountName = user.map(FoafOnlineAccount::getAccountName).orElse("anonymous");
        String avatarUrl = user.map(FoafOnlineAccount::getOwner).map(FoafAgent::getDepiction).orElse(null);

        Avatar avatar = new Avatar(accountName, avatarUrl);
        MenuItem userOptions = menuBar.addItem(avatar);

        SubMenu subMenu = userOptions.getSubMenu();
        subMenu.removeAll();

        if (user.isPresent()) { //
            subMenu.addItem("Logged in as: " + user.get().getAccountName()).setEnabled(false);
            subMenu.add(new Hr());
        }

        subMenu.addItem("Login", click -> {
            LoginForm loginForm = new LoginForm();
            Dialog dlg = new Dialog(loginForm);
            dlg.open();
        });

        subMenu.addItem("Create Account", click -> {
//          Dialog dlg = new Dialog(new LoginForm());
            Dialog dlg = new Dialog(new RegistrationForm());
            dlg.open();
        });

        if (user.isPresent()) { //
            subMenu.addItem("Logout", click -> {
                userSession.logout();
            });
        }
    }

    public Component getAppContent(ConfigFaceteVaadin config) {
        ComponentPlugin plugin = ComponentPlugin.createWithDefaultBase(
                appBuilder -> appBuilder
                        .parent(config.context)
                        .sources(ConfigRefresh.class)
//                .sources(ConfigSearchProviderNli.class)
//                .sources(ConfigSearchProviderSparql.class)
                        .sources(ConfigFacetedBrowserViewCord.class));

        VerticalLayout appContent = new VerticalLayout();
        ExplorerTabs tabs = new ExplorerTabs(plugin::newComponent);
        tabs.newTab();
        tabs.setWidthFull();
        appContent.add(tabs);
        return appContent;
    }

}
