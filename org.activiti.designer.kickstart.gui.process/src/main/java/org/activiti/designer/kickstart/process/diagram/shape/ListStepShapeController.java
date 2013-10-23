package org.activiti.designer.kickstart.process.diagram.shape;

import org.activiti.designer.kickstart.process.diagram.KickstartProcessFeatureProvider;
import org.activiti.designer.kickstart.process.features.REMOVEMEListStepDefinition;
import org.activiti.designer.kickstart.process.util.StepDefinitionStyles;
import org.activiti.workflow.simple.definition.ParallelStepsDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.AreaContext;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

/**
 * A {@link BusinessObjectShapeController} capable of creating and updating shapes for
 * {@link ParallelStepsDefinition} objects.
 *  
 * @author Frederik Heremans
 */
public class ListStepShapeController extends AbstractBusinessObjectShapeController {
  
  public ListStepShapeController(KickstartProcessFeatureProvider featureProvider) {
    super(featureProvider);
  }

  @Override
  public boolean canControlShapeFor(Object businessObject) {
    return businessObject instanceof REMOVEMEListStepDefinition;
  }

  @Override
  public ContainerShape createShape(Object businessObject, ContainerShape layoutParent, int width, int height) {
    final IPeCreateService peCreateService = Graphiti.getPeCreateService();
    final ContainerShape containerShape = peCreateService.createContainerShape(layoutParent, true);
    final IGaService gaService = Graphiti.getGaService();
    
    Diagram diagram = getFeatureProvider().getDiagramTypeProvider().getDiagram();
    
    REMOVEMEListStepDefinition definition = (REMOVEMEListStepDefinition) businessObject;

    // If no size has been supplied, revert to the default sizes
    if(width < 0) {
      width = StepDefinitionStyles.DEFAULT_COMPONENT_WIDTH;
    }
    
    height = StepDefinitionStyles.DEFAULT_LABEL_HEIGHT + StepDefinitionStyles.DEFAULT_PARALLEL_BOX_HEIGHT;

    RoundedRectangle rectangle; 
    final Rectangle invisibleRectangle = gaService.createInvisibleRectangle(containerShape);
    gaService.setLocationAndSize(invisibleRectangle, 0, 0, width, height);

    // Create and set visible rectangle inside invisible rectangle
    rectangle = gaService.createRoundedRectangle(invisibleRectangle, 2, 2);
    rectangle.setLineStyle(LineStyle.DASH);
    rectangle.setForeground(StepDefinitionStyles.getSubtleForegroundColor(diagram));
    rectangle.setBackground(StepDefinitionStyles.getSubtleBackgroundColor(diagram));
    rectangle.setLineWidth(1);
    rectangle.setFilled(true);
    rectangle.setParentGraphicsAlgorithm(invisibleRectangle);
    gaService.setLocationAndSize(rectangle, 0, 0, width, height);
    
    getFeatureProvider().link(containerShape, new Object[] {definition});
    
    // Check if the shape has any children. If so, create shapes for them as well
    if(definition.getSteps() != null && !definition.getSteps().isEmpty()) {
      AddContext addContext = null;
      AreaContext areaContext = new AreaContext();
      areaContext.setX(-1);
      areaContext.setY(-1);
      
      StepDefinition child = null;
      for(int i = definition.getSteps().size() -1; i>=0; i--) {
        child = definition.getSteps().get(i);
        addContext = new AddContext(areaContext, child);
        addContext.setTargetContainer(containerShape);
        featureProvider.getAddFeature(addContext).add(addContext);
      }
    }
    return containerShape;
  }

  @Override
  public void updateShape(ContainerShape shape, Object businessObject, int width, int height) {
    boolean updateWidth = (width > 0 && width != shape.getGraphicsAlgorithm().getWidth());
    boolean updateHeight = (height > 0 && height != shape.getGraphicsAlgorithm().getHeight());
    // Check if width needs to be altered
    if(updateHeight || updateWidth) {
      IGaService gaService = Graphiti.getGaService();
      
      // Resize main shape rectangle and box shape
      Rectangle invisibleRectangle = (Rectangle) shape.getGraphicsAlgorithm();
      GraphicsAlgorithm box = (GraphicsAlgorithm) invisibleRectangle.eContents().get(0);
      
      if(updateWidth) {
        gaService.setWidth(invisibleRectangle, width);
        gaService.setWidth(box, width);
      }
      
      if(updateHeight) {
        gaService.setHeight(invisibleRectangle, height);
        gaService.setHeight(box, height);
      }
      
    }
  }
  
  @Override
  public GraphicsAlgorithm getGraphicsAlgorithmForDirectEdit(ContainerShape container) {
    // No direct edit supported
    return null;
  }
  
  @Override
  public boolean isShapeUpdateNeeded(ContainerShape shape, Object businessObject) {
    // Model changes never trigger a shape-update, only layout changes
    return false;
  }
}
