package org.datayoo.sengee.op.processing.generator;

import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "RandCellPhone",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "sengee",
    tags = { @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "generator")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"数据列\" c_Compox=\"sengee-string-column-selector\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class RandCellPhoneDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_COLUMN_NAME = "columnName";

  public RandCellPhoneDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected void readParameters() {
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    FlowPort inputPort = this.inputPorts.iterator().next();
    return this.getPortColumnSetMetadata(inputPort.getName());
  }

  @Override
  protected void validateParameters() {
  }
}
