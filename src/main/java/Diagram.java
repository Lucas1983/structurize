import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.api.StructurizrClientException;
import com.structurizr.model.Container;
import com.structurizr.model.Enterprise;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.model.Tags;
import com.structurizr.view.ContainerView;
import com.structurizr.view.Routing;
import com.structurizr.view.Shape;
import com.structurizr.view.Styles;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.ViewSet;

public class Diagram {

  public static final String SOFTWARE_SYSTEM_VIEW = "SoftwareSystemView";
  public static final String CONTAINER_VIEW = "ContainerView";
  public static final String COMPONENT_VIEW = "ComponentView";

  public static void main(String[] args) {

    Workspace workspace = buildContext();
    workspace = buildContainers(workspace);
    export(workspace);
  }

  private static Workspace buildContainers(Workspace workspace) {

    SoftwareSystem digiperf = workspace.getModel().getSoftwareSystemWithName("DIGIPERF");

    Container apiGwContainer = digiperf.addContainer("ApiGW-BE");
    Container bffContainer = digiperf.addContainer("BFF-BE");
    Container clContainer = digiperf.addContainer("CL-BE");
    Container cilContainer = digiperf.addContainer("CIL-BE");
    Container poContainer = digiperf.addContainer("PO-BE");

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
}
