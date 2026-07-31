package org.datayoo.sengee.op.processing.flat;

import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowContext;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.footstone.sightx.annotation.Compox;
import org.datayoo.footstone.sightx.annotation.DataDesc;
import org.datayoo.footstone.sightx.annotation.VisibleDesc;
import org.datayoo.footstone.sightx.vis.VisibleType;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.util.ColumnSetMetadataLibrary;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.AbstractOperatorDescriptor;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "Json2KVTable",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "sengee",
    tags = { @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,flat")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_KV_TABLE,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"dataColumn\" c_Alias=\"Json数据列\" c_Compox=\"sengee-string-column-selector\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class Json2KVTableDescriptor extends AbstractOperatorDescriptor {

  public static final String PARAM_DATA_COLUMN = "dataColumn";

  public Json2KVTableDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  public Json2KVTableDescriptor(String id, FlowNode parent,
      FlowContext compilationContext) {
    super(id, parent, compilationContext);
  }

  @Override
  protected void readParameters() {

  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return ColumnSetMetadataLibrary.createKVTableMetadata();
  }

  @Override
  protected void validateParameters() {
    String dataColumn = parameters.getParameterValue(PARAM_DATA_COLUMN, "");
    if (dataColumn.isEmpty()) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_DATA_COLUMN));
    }
  }
}
