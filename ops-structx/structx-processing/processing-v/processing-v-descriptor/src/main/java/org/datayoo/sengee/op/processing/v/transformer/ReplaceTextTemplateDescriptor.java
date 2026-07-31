package org.datayoo.sengee.op.processing.v.transformer;

import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "ReplaceTextTemplate",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "sengee",
    tags = { @TagPair(name = SengeeConstants.TAG_OP_CATS,
        value = "column,transformer")
    },
    inputPorts = { @Port(name = ReplaceTextTemplateDescriptor.PORT_TEMPLATE,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true), @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"模板列\" c_Compox=\"sengee-templateIn:string-column-selector\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class ReplaceTextTemplateDescriptor
    extends AbstractProcessingDescriptor {

  public static final String PORT_TEMPLATE = "templateIn";

  public ReplaceTextTemplateDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected void readParameters() {

  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return getPortColumnSetMetadata(PORT_TEMPLATE);
  }

  @Override
  public void validateParameters() {
    String columnName = parameters.getParameterValue(PARAM_COLUMN_NAME, null);
    if (columnName == null) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias, PARAM_COLUMN_NAME));
    }
  }

}
