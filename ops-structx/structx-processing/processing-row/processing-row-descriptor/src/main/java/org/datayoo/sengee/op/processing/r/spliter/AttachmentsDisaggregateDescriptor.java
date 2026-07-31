package org.datayoo.sengee.op.processing.r.spliter;

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
@OpDefiner(name = "AttachmentsDisaggregate",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,splitter")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ATTACHMENTED,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_STREAM_IN,
        flowDataType = SengeeConstants.FDT_DATA_STREAM,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"idColumn\" c_Compox=\"sengee-column-selector\" c_Option=\"false\" c_Alias=\"附件标识列\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class AttachmentsDisaggregateDescriptor
    extends AbstractProcessingDescriptor {

  public static final String PARAM_ID_COLUMN = "idColumn";

  protected String idColumn;

  public AttachmentsDisaggregateDescriptor(FlowNodeMetadata flowNodeMetadata,
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
    if (idColumn == null) {
      return ColumnSetMetadataLibrary.createDataStreamMetadata();
    }
    ColumnMetadata columnMetadata = columnSetMetadata.getColumn(idColumn);
    ColumnSetMetadata outputColumnSetMetadata = ColumnSetMetadataLibrary.createDataStreamMetadata();
    outputColumnSetMetadata.addColumn(columnMetadata);
    return outputColumnSetMetadata;
  }

  @Override
  protected void readParameters() {
    idColumn = parameters.getParameterValue(PARAM_ID_COLUMN, null);
  }

  @Override
  protected void innerInitialize() {

  }

  @Override
  protected void innerDestroy() {

  }

  @Override
  protected void validateParameters() {
    if (idColumn == null) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_ID_COLUMN));
    }
    ColumnSetMetadata columnSetMetadata = getPortColumnSetMetadata(
        SengeeOperatorConstants.PORT_DATA_IN);
    if (columnSetMetadata != null) {
      if (columnSetMetadata.getColumnIndex(
          SengeeOperatorConstants.COLN_ATTACHMENTS) == -1) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_OPD_COL_NOT_EXISTED, this.alias,
            SengeeOperatorConstants.COLN_ATTACHMENTS));
      }
    }
  }
}
