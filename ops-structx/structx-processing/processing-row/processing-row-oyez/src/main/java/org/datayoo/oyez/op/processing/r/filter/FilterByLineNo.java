package org.datayoo.oyez.op.processing.r.filter;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.r.filter.FilterByLineNoDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.List;

@OpDefiner(name = "FilterByLineNo",
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
public class FilterByLineNo extends BaseProcessOperator {

  private int lineNo;

  private int firstNo;

  private int lastNo;

  private boolean invert;

  public FilterByLineNo(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    this.lineNo = 0;
    this.firstNo = parameters.getParameterValueAsInt(
        FilterByLineNoDescriptor.PARAM_FIRST_NO);
    this.lastNo = parameters.getParameterValueAsInt(
        FilterByLineNoDescriptor.PARAM_LAST_NO);
    this.invert = parameters.getParameterValueAsBoolean(
        FilterByLineNoDescriptor.INVERT, false);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return columnSetMetadata;
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {

    throw new UnsupportedOperationException("");
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = null;
    if (lastNo > 0) {
      rowSet = read();
    } else {
      rowSet = readAll(this.inputPort);
      lastNo = rowSet.getRowsCount() + this.lastNo + 1; // -1表示最后一条
      if (lastNo < firstNo)
        lastNo = 0;
    }
    if (invert) {
      if (lineNo + rowSet.getRowsCount() < firstNo) {
        lineNo += rowSet.getRowsCount();
        return;
      }
      if (lineNo > lastNo) {
        outputPort.write(rowSet, inputPort.getWaterMark());
        lineNo += rowSet.getRowsCount();
        return;
      }
      PlRowSet filterRowSet = createRowSet(rowSet.getColumnSetMetadata());
      filterRowSet.addRows(filter(rowSet.getRows()));
      outputPort.write(filterRowSet, inputPort.getWaterMark());
    } else {
      if (lineNo + rowSet.getRowsCount() < firstNo) {
        lineNo += rowSet.getRowsCount();
        return;
      }
      if (lineNo > lastNo) {
        lineNo += rowSet.getRowsCount();
        return;
      }
      if (lineNo >= firstNo && lineNo + rowSet.getRowsCount() <= lastNo) {
        outputPort.write(rowSet, inputPort.getWaterMark());
        lineNo += rowSet.getRowsCount();
        return;
      }
      PlRowSet filterRowSet = createRowSet(rowSet.getColumnSetMetadata());
      filterRowSet.addRows(filter(rowSet.getRows()));
      outputPort.write(filterRowSet, inputPort.getWaterMark());
    }
  }

  protected List<Object[]> filter(List<Object[]> rows) {
    List<Object[]> result = new ArrayList<>(rows.size());

    for (Object[] row : rows) {
      lineNo++;
      if (lineNo >= firstNo && lineNo <= lastNo) {
        if (!invert) {
          result.add(row);
        }
      } else {
        if (invert) {
          result.add(row);
        }
      }
    }

    return result;
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
