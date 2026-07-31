package org.datayoo.oyez.op.processing.r.join;

import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralPlRowSet;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.oyez.op.AbstractOyezOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.port.OyezInputPort;
import org.datayoo.oyez.port.OyezOutputPort;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.service.CompliationDependentCsmService;

public abstract class BaseSetOperator extends AbstractOyezOperator {

  protected OyezInputPort leftInput;

  protected OyezInputPort rightInput;

  protected OyezOutputPort outputPort;

  protected ColumnSetMetadata outputColumnSetMetadata;

  protected String sql;

  public BaseSetOperator(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
    this.checkPorts();
  }

  protected void checkPorts() {
    if (this.inputPorts.size() != 2) {
      throw new IllegalArgumentException(
          String.format("Operator '%s' should has 2 input port!", this.alias));
    } else {
      for (FlowPort flowPort : this.inputPorts) {
        if (flowPort.getName().equals(SengeeOperatorConstants.PORT_LEFT_IN))
          leftInput = (OyezInputPort) flowPort;
        else if (flowPort.getName()
            .equals(SengeeOperatorConstants.PORT_RIGHT_IN))
          rightInput = (OyezInputPort) flowPort;
        else
          throw new IllegalArgumentException(
              String.format("Unsupported input port '%s'!",
                  flowPort.getName()));
      }
      if (this.outputPorts.size() != 1) {
        throw new IllegalArgumentException(
            String.format("Operator '%s' should has only 1 output port!",
                this.alias));
      } else {
        this.outputPort = (OyezOutputPort) this.outputPorts.iterator().next();
      }
    }
  }

  @Override
  protected void buildFeatureStates() {

  }

  @Override
  protected void operatorInitialize() {
    CompliationDependentCsmService columnSetMetadataService = getEngineContext().lookupService(
        CompliationDependentCsmService.class);
    ColumnSetMetadata leftMetadata = columnSetMetadataService.getColumnSetMetadata(
        getEngineContext().getProcessGraphId(), this.id, leftInput.getName());
    ColumnSetMetadata rightMetadata = columnSetMetadataService.getColumnSetMetadata(
        getEngineContext().getProcessGraphId(), this.id, rightInput.getName());
    sql = buildSetSql(leftMetadata, rightMetadata);
    outputColumnSetMetadata = buildOutputColumnSetMetadata(leftMetadata,
        rightMetadata);
  }

  protected abstract String buildSetSql(ColumnSetMetadata leftColumnSetMetadata,
      ColumnSetMetadata rightColumnSetMetadata);

  protected abstract ColumnSetMetadata buildOutputColumnSetMetadata(
      ColumnSetMetadata leftColumnSetMetadata,
      ColumnSetMetadata rightColumnSetMetadata);

  @Override
  protected void operatorDestroy() {

  }

  @Override
  protected boolean isReady() {
    if (leftInput.isReady()) {
      if (rightInput.isReady() || !hasMoreData(rightInput)) {
        return true;
      }
    }
    if (rightInput.isReady()) {
      return leftInput.isReady() || !hasMoreData(leftInput);
    }
    return false;
  }

  protected PlRowSet createRowSet(ColumnSetMetadata columnSetMetadata) {
    String rowSetName = String.format("%s.%s", this.alias,
        outputPort.getAlias());
    return new GeneralPlRowSet(rowSetName, columnSetMetadata);
  }
}
