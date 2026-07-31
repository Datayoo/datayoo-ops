package org.datayoo.oyez.op.processing.r.grouping;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.*;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.r.grouping.AttachmentAggregateDescriptor;
import org.datayoo.sengee.op.util.ColumnSetMetadataLibrary;
import org.datayoo.sengee.opa.AttachmentEntry;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.datayoo.util.io.InputStreamUtils;
import org.datayoo.wangee.job.RunningState;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@OpDefiner(name = "AttachmentAggregate",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_STREAM_IN,
        flowDataType = SengeeConstants.FDT_DATA_STREAM,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ATTACHMENTED,
        option = true)
    },
    parameters = "",
    compoxes = {})
public class AttachmentAggregate extends BaseProcessOperator {

  private String groupColumn;

  private int groupIndex = -1;

  private Object group;

  private ColumnSetMetadata outputColumnSetMetadata;

  private List<AttachmentEntry> attachmentEntries = new LinkedList<>();

  private String lastWatermark = null;

  public AttachmentAggregate(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    ColumnSetMetadata outputColumnSetMetadata = ColumnSetMetadataLibrary.createAttachmentColumnSetMetadata();
    ColumnMetadata columnMetadata = columnSetMetadata.getColumn(groupColumn);
    outputColumnSetMetadata.getColumns().add(0, columnMetadata);
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
      Object v = row[groupIndex];
      AttachmentEntry attachmentEntry = toAttachmentEntry((String) row[0],
          (InputStream) row[1], (Map) row[5]);
      if (Objects.equals(this.group, v)) {
        attachmentEntries.add(attachmentEntry);
      } else {
        writeOut();
        this.group = v;
        attachmentEntries = new LinkedList<>();
        attachmentEntries.add(attachmentEntry);
      }
    }
    lastWatermark = inputPort.getWaterMark();
  }

  protected AttachmentEntry toAttachmentEntry(String name,
      InputStream inputStream, Map<String, Object> fileMetas) {
    AttachmentEntry attachmentEntry = new AttachmentEntry();
    attachmentEntry.setName(name);
    try {
      String mimeType = "application/octet-stream";
      if (fileMetas != null) {
        mimeType = (String) fileMetas.get(
            SengeeOperatorConstants.STREAM_META_TYPE);
        if (mimeType == null) {
          mimeType = "application/octet-stream";
        }
      }
      attachmentEntry.setMimeType(mimeType);
      attachmentEntry.setData(
          InputStreamUtils.readInputStream(inputStream, -1));
    } catch (IOException e) {
      throw new OperationRuntimeException(e);
    }
    return attachmentEntry;
  }

  protected void writeOut() {
    if (group != null) {
      PlRowSet outSet = new GeneralPlRowSet(outputPort.getName(),
          new GeneralColumnSetMetadata(outputColumnSetMetadata));
      Object[] r = new Object[2];
      r[0] = group;
      r[1] = attachmentEntries;
      outSet.addRow(r);
      outputPort.write(outSet, lastWatermark);
    }
  }

  protected void preBindColumnSetMetadata(ColumnSetMetadata columnSetMetadata) {
    if (groupIndex != -1)
      return;
    groupIndex = columnSetMetadata.getColumnIndex(groupColumn);
    outputColumnSetMetadata = createOutputColumnSetMetadata(columnSetMetadata);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    groupColumn = this.parameters.getParameterValue(
        AttachmentAggregateDescriptor.PARAM_GROUPING_COLUMN);
  }

  @Override
  protected void finishOperate(RunningState state, String errorMessage) {
    if (attachmentEntries.size() > 0) {
      writeOut();
    }
    super.finishOperate(state, errorMessage);
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
