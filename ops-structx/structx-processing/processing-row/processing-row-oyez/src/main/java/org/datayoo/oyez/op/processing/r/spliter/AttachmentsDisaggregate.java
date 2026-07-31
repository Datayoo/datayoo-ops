package org.datayoo.oyez.op.processing.r.spliter;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.*;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.r.spliter.AttachmentsDisaggregateDescriptor;
import org.datayoo.sengee.op.util.ColumnSetMetadataLibrary;
import org.datayoo.sengee.opa.AttachmentEntry;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.io.ByteArrayInputStream;
import java.util.List;

@OpDefiner(name = "AttachmentsDisaggregate",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ATTACHMENTED,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_STREAM_IN,
        flowDataType = SengeeConstants.FDT_DATA_STREAM,
        option = true)
    },
    parameters = "",
    compoxes = {})
public class AttachmentsDisaggregate extends BaseProcessOperator {

  private String idColumn;

  private int idColumnIndex = -1;
  private int attachementsIndex = -1;

  private ColumnSetMetadata outputColumnSetMetadata;

  public AttachmentsDisaggregate(FlowNodeMetadata operatorMetadata,
      FlowNode parent, EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    ColumnSetMetadata outputColumnSetMetadata = ColumnSetMetadataLibrary.createDataStreamMetadata();
    ColumnMetadata columnMetadata = columnSetMetadata.getColumn(idColumn);
    outputColumnSetMetadata.addColumn(columnMetadata);
    return outputColumnSetMetadata;
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = inputPort.read();
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    preBindColumnSetMetadata(columnSetMetadata);
    for (Object[] row : rowSet.getRows()) {
      if (getEngineContext().isTermination())
        break;
      Object id = row[idColumnIndex];
      if (attachementsIndex == -1)
        continue;
      List<AttachmentEntry> attachements = (List<AttachmentEntry>) row[attachementsIndex];
      if (attachements == null || attachements.size() == 0)
        continue;
      for (AttachmentEntry attachmentEntry : attachements) {
        writeOut(id, attachmentEntry);
      }
    }
  }

  protected void writeOut(Object id, AttachmentEntry attachmentEntry) {
    PlRowSet outSet = new GeneralPlRowSet(outputPort.getName(),
        new GeneralColumnSetMetadata(outputColumnSetMetadata));
    Object[] r = new Object[7];
    r[0] = attachmentEntry.getName();
    r[1] = new ByteArrayInputStream(attachmentEntry.getData());
    r[6] = id;
    outSet.addRow(r);
    outputPort.write(outSet, inputPort.getWaterMark());
  }

  protected void preBindColumnSetMetadata(ColumnSetMetadata columnSetMetadata) {
    if (idColumnIndex != -1)
      return;
    idColumnIndex = columnSetMetadata.getColumnIndex(idColumn);
    attachementsIndex = columnSetMetadata.getColumnIndex(
        SengeeOperatorConstants.COLN_ATTACHMENTS);
    outputColumnSetMetadata = createOutputColumnSetMetadata(columnSetMetadata);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    idColumn = this.parameters.getParameterValue(
        AttachmentsDisaggregateDescriptor.PARAM_ID_COLUMN);
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
