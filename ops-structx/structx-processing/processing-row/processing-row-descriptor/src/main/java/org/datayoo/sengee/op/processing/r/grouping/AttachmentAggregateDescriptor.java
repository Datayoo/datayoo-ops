package org.datayoo.sengee.op.processing.r.grouping;

import org.datayoo.datax.sd.ColumnMetadata;
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
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.ColumnSetMetadataLibrary;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

/**
 * 分组统计
 *
 * @author hhn
 */
@OpDefiner(name = "AttachmentAggregate",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,grouping")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_STREAM_IN,
        flowDataType = SengeeConstants.FDT_DATA_STREAM,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ATTACHMENTED,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"groupingColumn\" c_Compox=\"sengee-column-selector\" c_Option=\"false\" c_Alias=\"附件打包列\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class AttachmentAggregateDescriptor
    extends AbstractProcessingDescriptor {

  public static final String PARAM_GROUPING_COLUMN = "groupingColumn";

  protected String groupingColumn;

  public AttachmentAggregateDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata getPortDeclaredColumnSetMetadata(
      FlowPort<PlRowSet> flowPort) {
    if (flowPort.getFlowDataType().equals(SengeeConstants.FDT_DATA_STREAM))
      return ColumnSetMetadataLibrary.createDataStreamMetadata();
    if (flowPort.getFlowDataType().equals(SengeeConstants.FDT_ATTACHMENTED))
      return ColumnSetMetadataLibrary.createAttachmentColumnSetMetadata();
    return super.getPortDeclaredColumnSetMetadata(flowPort);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    ColumnSetMetadata columnSetMetadata = getInputColumnSetMetadata();
    if (groupingColumn == null) {
      return ColumnSetMetadataLibrary.createAttachmentColumnSetMetadata();
    }
    ColumnMetadata columnMetadata = columnSetMetadata.getColumn(groupingColumn);
    ColumnSetMetadata outputColumnSetMetadata = ColumnSetMetadataLibrary.createAttachmentColumnSetMetadata();
    outputColumnSetMetadata.getColumns().add(0, columnMetadata);
    return outputColumnSetMetadata;
  }

  @Override
  protected void readParameters() {
    groupingColumn = parameters.getParameterValue(PARAM_GROUPING_COLUMN, null);
  }

  @Override
  protected void innerInitialize() {

  }

  @Override
  protected void innerDestroy() {

  }

  @Override
  protected void validateParameters() {
    if (groupingColumn == null) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_GROUPING_COLUMN));
    }
  }
}
