package org.datayoo.oyez.op.processing.st;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.st.RenameColumnsByPatternDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.List;

@OpDefiner(name = "RenameColumnsByPattern",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    inputPorts = { @Port(name = "dataIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class RenameColumnsByPattern extends BaseProcessOperator {

  private String replacement;

  private String regex;

  public RenameColumnsByPattern(FlowNodeMetadata operatorMetadata,
      FlowNode parent, EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    GeneralColumnSetMetadata outputSetMetadata = new GeneralColumnSetMetadata(
        columnSetMetadata);
    for (ColumnMetadata columnMetadata : outputSetMetadata.getColumns()) {
      String column = columnMetadata.getName();
      columnMetadata.setName(column.replaceAll(regex, replacement));
    }
    return columnSetMetadata;
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    return objects;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    regex = parameters
        .getParameterValue(RenameColumnsByPatternDescriptor.PARAM_REGEX);
    replacement = parameters
        .getParameterValue(RenameColumnsByPatternDescriptor.PARAM_REPLACEMENT);
  }

  @Override
  protected void buildFeatureStates() {

  }

  @Override
  protected void operatorInitialize() {

  }

  @Override
  protected void operatorDestroy() {

  }
}
