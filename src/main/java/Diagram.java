import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.api.StructurizrClientException;
import com.structurizr.model.*;
import com.structurizr.view.*;

public class Diagram {

  public static final String SOFTWARE_SYSTEM_VIEW = "SoftwareSystemView";
  public static final String CONTAINER_VIEW = "ContainerView";
  public static final String COMPONENT_VIEW = "ComponentView";
  public static final String API_GW_BE_SVC = "API-GW-BE-SVC";
  public static final String BFF_BE_SVC = "BFF-BE-SVC";
  public static final String CL_BE_SVC = "CL-BE-SVC";
  public static final String CIL_BE_SVC = "CIL-BE-SVC";
  public static final String PO_BE_SVC = "PO-BE-SVC";
  public static final String CL_DB = "CL-DB";
  public static final String CIL_DB = "CIL-DB";
  public static final String PO_DB = "PO-DB";
  public static final String CL_FE_SVC = "CL-FE-SVC";
  public static final String CIL_FE_SVC = "CIL-FE-SVC";
  public static final String PO_FE_SVC = "PO-FE-SVC";
  public static final String BACKEND = "BACKEND";
  public static final String FRONTEND = "FRONTEND";
  public static final String DIGIPERF = "DIGIPERF";

  public static void main(String[] args) {

    Workspace workspace = buildContext();
    workspace = buildContainers(workspace);
    workspace = buildComponents(workspace);
    export(workspace);
  }

  private static Workspace buildContainers(Workspace workspace) {

    SoftwareSystem digiperf = workspace.getModel().getSoftwareSystemWithName(DIGIPERF);

    assert digiperf != null;
    Container apiGwSvc = digiperf.addContainer(API_GW_BE_SVC, "Authenticate & Authorize");
    Container bffSvc =
        digiperf.addContainer(BFF_BE_SVC, "Gather & merge data from multiple services");
    Container clSvc = digiperf.addContainer(CL_BE_SVC, "Provide CL data");
    Container cilSvc = digiperf.addContainer(CIL_BE_SVC, "Provide CIL data");
    Container poSvc = digiperf.addContainer(PO_BE_SVC, "Supply system with Process Order data");

    Container clDb = digiperf.addContainer(CL_DB, "Store CL data");
    Container cilDb = digiperf.addContainer(CIL_DB, "Store CIL data");
    Container poDb = digiperf.addContainer(PO_DB, "Store PO data");

    apiGwSvc.uses(bffSvc, "Gets data via BFF");
    bffSvc.uses(clSvc, "Gets CL data");
    bffSvc.uses(cilSvc, "Gets CIL data");
    clSvc.uses(poSvc, "Fetch info about PO");
    cilSvc.uses(poSvc, "Fetch info about PO");

    clSvc.uses(clDb, "Save data");
    cilSvc.uses(cilDb, "Save data");
    poSvc.uses(poDb, "Save data");

    Container clSvcFe = digiperf.addContainer(CL_FE_SVC, "Display CL data");
    Container cilSvcFe = digiperf.addContainer(CIL_FE_SVC, "Display CIL data");
    Container poSvcFe = digiperf.addContainer(PO_FE_SVC, "Display PO data");

    clSvcFe.uses(clSvc, "");
    cilSvcFe.uses(cilSvc, "");
    poSvcFe.uses(poSvc, "");

    apiGwSvc.setGroup(BACKEND);
    bffSvc.setGroup(BACKEND);
    clSvc.setGroup(BACKEND);
    cilSvc.setGroup(BACKEND);
    poSvc.setGroup(BACKEND);

    clDb.setGroup(BACKEND);
    cilDb.setGroup(BACKEND);
    poDb.setGroup(BACKEND);

    clSvcFe.setGroup(FRONTEND);
    cilSvcFe.setGroup(FRONTEND);
    poSvcFe.setGroup(FRONTEND);

    ContainerView containerView =
        workspace
            .getViews()
            .createContainerView(digiperf, CONTAINER_VIEW, "DIGIPERF CONTAINER VIEW");
    containerView.addAllContainers();

    return workspace;
  }

  private static Workspace buildContext() {
    Workspace workspace = new Workspace("DIGIPERF", "System for production support");
    Model model = workspace.getModel();

    Enterprise enterprise = model.getEnterprise();
    Person worker = model.addPerson("Worker");
    SoftwareSystem digiperf = model.addSoftwareSystem("DIGIPERF");
    worker.uses(digiperf, "Manage Production");

    SoftwareSystem spa = model.addSoftwareSystem("SPA");
    digiperf.uses(spa, "Fetch additional data from SPA");

    SoftwareSystem sap = model.addSoftwareSystem("SAP");
    digiperf.uses(sap, "Fetch additional data from SAP");

    ViewSet viewSet = workspace.getViews();
    SystemContextView digiperContextView =
        viewSet.createSystemContextView(digiperf, SOFTWARE_SYSTEM_VIEW, "DIGIPERF SYSTEM VIEW");
    digiperContextView.addAllElements();

    return workspace;
  }

  private static void addStyles(ViewSet viewSet) {
    Styles styles = viewSet.getConfiguration().getStyles();
    styles.addElementStyle(Tags.ELEMENT).color("#000000");
    styles.addElementStyle(Tags.PERSON).background("#ffbf00").shape(Shape.Person);
    styles.addElementStyle(Tags.CONTAINER).background("#facc2E");
    styles.addRelationshipStyle(Tags.RELATIONSHIP).routing(Routing.Orthogonal);

    styles.addRelationshipStyle(Tags.ASYNCHRONOUS).dashed(true);
    styles.addRelationshipStyle(Tags.SYNCHRONOUS).dashed(false);
  }

  private static void export(Workspace workspace) {

    try {
      StructurizrClient client =
          new StructurizrClient(
              "d9eeaa2a-b9ce-4d63-ab84-63da85e22dbc", "a7bce24e-7667-48bc-aab1-98ae412adf1b");
      client.putWorkspace(72484, workspace);
    } catch (StructurizrClientException e) {
      e.printStackTrace();
    }
  }

  private static Workspace buildComponents(Workspace workspace) {

    SoftwareSystem softwareSystem = workspace.getModel().getSoftwareSystemWithName(DIGIPERF);

    softwareSystem
        .getContainers()
        .forEach(
            container -> {
              ComponentView componentView =
                  workspace
                      .getViews()
                      .createComponentView(
                          container, COMPONENT_VIEW + container.getName(), COMPONENT_VIEW);

              if (container.getGroup().equals(BACKEND)) {

                if (container.getName().endsWith("SVC")) {
                  var spring = container.addComponent("SPRING");
                  var hibernate = container.addComponent("HIBERNATE");

                  spring.uses(hibernate,"Store using");

                } else {
                  container.addComponent("POSTGRES");
                }
              } else {
                container.addComponent("REACT");
              }
              componentView.addAllComponents();
            });
    return workspace;
  }
}
