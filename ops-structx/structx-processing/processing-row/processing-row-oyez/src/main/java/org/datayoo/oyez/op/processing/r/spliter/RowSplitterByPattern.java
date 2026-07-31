package org.datayoo.oyez.op.processing.r.spliter;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationInterruptionException;
import org.datayoo.sengee.op.processing.r.spliter.RowSplitterByPatternDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.LinkedList;
import java.util.List;

/**
 * @author tangtadin
 * @version 1.0
 * @description: TODO
 * @date 2023/3/25 12:16
 */
@OpDefiner(name = "RowSplitterByPattern",
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
public class RowSplitterByPattern extends BaseProcessOperator {

  protected String pattern;

  protected String[] columns;

  protected int[] columnIndexes;

  public RowSplitterByPattern(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return columnSetMetadata;
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    return new Object[0];
  }

  protected void innerOperate() {
    PlRowSet rowSet = this.inputPort.read();
    int i = 0;
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    this.preBindColumnSetMetadata(columnSetMetadata);
    PlRowSet outSet = this.createRowSet(
        this.createOutputColumnSetMetadata(columnSetMetadata));
    for (Object[] row : rowSet.getRows()) {
      if (this.getEngineContext().isTermination()) {
        return;
      }
      try {
        List<Object[]> rows = parseRow(row);
        outSet.addRows(rows);
      } catch (Throwable t) {
        if (!reportErrorAndRun(row, t)) {
          throw new OperationInterruptionException(t);
        }
      }
    }
    this.outputPort.write(outSet, this.inputPort.getWaterMark());
  }

  @Override
  protected void preBindColumnSetMetadata(ColumnSetMetadata columnSetMetadata) {
    int i = 0;
    for (String column : columns) {
      columnIndexes[i++] = columnSetMetadata.getColumnIndex(column);
    }
  }

  protected List<Object[]> parseRow(Object[] row) {
    List<Object[]> rows = null;
    int size = 0;
    for (int i = 0; i < columnIndexes.length; i++) {
      String text = (String) row[columnIndexes[i]];
      if (text != null) {
        String[] segs = text.split(pattern);
        if (rows == null) {
          rows = buildRows(row, segs.length);
        }
        int j = 0;
        for (Object[] r : rows) {
          r[columnIndexes[i]] = segs[j++];
        }
      }
    }
    if (rows == null) {
      rows = new LinkedList<>();
      rows.add(row);
    }
    return rows;
  }

  protected List<Object[]> buildRows(Object[] row, int size) {
    List<Object[]> rows = new LinkedList<>();
    for (int i = 0; i < size; i++) {
      Object[] r = new Object[row.length];
      System.arraycopy(row, 0, r, 0, row.length);
      rows.add(r);
    }
    return rows;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        RowSplitterByPatternDescriptor.PARAM_COLUMN_SET);
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    columns = new String[rowParameters.size()];
    columnIndexes = new int[rowParameters.size()];
    int i = 0;
    for (RowParameter rowParameter : rowParameters) {
      columns[i] = rowParameter.getParameterValue(
          RowSplitterByPatternDescriptor.PARAM_COLUMN_NAME);
      i++;
    }
    pattern = parameters.getParameterValue(
        RowSplitterByPatternDescriptor.PARAM_PATTERN, "");
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
