package org.datayoo.oyez.op.processing.r.join;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.*;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.DataSetMap;
import org.datayoo.moql.DataSetMapImpl;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.util.MoqlUtils;
import org.datayoo.oyez.op.processing.util.ProcessOperatorHelper;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.r.join.JoinDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.*;

@OpDefiner(name = "Join",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    replica = -1,
    inputPorts = { @Port(name = "leftIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false), @Port(name = "rightIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class Join extends BaseSetOperator {
  private String[] leftColumns;
  private String[] rightColumns;
  //0为payload字段索引，1为所有索引
  private int[][] leftIndexes;
  //0为payload字段索引，1为剩余索引
  private int[][] rightIndexes;

  private String joinType;

  public Join(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void innerOperate() {
    PlRowSet leftPlRowSet = readAll(leftInput);
    PlRowSet rightPlRowSet = readAll(rightInput);
    if (leftPlRowSet == null && rightPlRowSet == null) {
      return;
    }
    if (leftPlRowSet == null && joinType.equals("left")) {
      return;
    } else if (rightPlRowSet == null && joinType.equals("right")) {
      return;
    }
    PlRowSet outSet = createRowSet(outputColumnSetMetadata);
    String watermark = getInputWaterMark();
    List<Object[]> leftRows = null;
    if (leftPlRowSet != null) {
      leftRows = leftPlRowSet.getRows();
    } else {
      leftRows = new LinkedList<>();
    }
    List<Object[]> rightRows = null;
    if (rightPlRowSet != null) {
      rightRows = rightPlRowSet.getRows();
    } else {
      rightRows = new LinkedList<>();
    }
    DataSetMap dataSetMap = new DataSetMapImpl();
    dataSetMap.putDataSet("ROW1", leftRows);
    dataSetMap.putDataSet("ROW2", rightRows);
    outSet.addRows(MoqlUtils.execute(sql, dataSetMap));
    outputPort.write(outSet, watermark);
  }

  @Override
  protected String buildSetSql(ColumnSetMetadata leftColumnSetMetadata,
      ColumnSetMetadata rightColumnSetMetadata) {
    //indexes[0]是leftcolumns的index,indexes[1]是根据是否排除计算后剩余字段的index
    leftIndexes = ProcessOperatorHelper.bindAllIndexes(leftColumnSetMetadata,
        leftColumns, true);
    rightIndexes = ProcessOperatorHelper.bindAllIndexes(rightColumnSetMetadata,
        rightColumns, true);
    StringBuilder sb = new StringBuilder();
    sb.append("select ");
    for (int i = 0; i < leftIndexes[1].length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("leftRow[").append(leftIndexes[1][i]).append("]");
    }
    for (int i = 0; i < rightIndexes[1].length; i++) {
      sb.append(",rightRow[").append(rightIndexes[1][i]).append("]");
    }

    sb.append(" from ROW1 leftRow ");
    sb.append(joinType).append(" join ROW2 rightRow").append(" on ");

    for (int i = 0; i < leftIndexes[0].length; i++) {
      if (i > 0) {
        sb.append(" and ");
      }
      sb.append("leftRow[").append(leftIndexes[0][i]).append("] = rightRow[")
          .append(rightIndexes[0][i]).append("]");
    }
    return sb.toString();
  }

  @Override
  protected ColumnSetMetadata buildOutputColumnSetMetadata(
      ColumnSetMetadata leftMetadata, ColumnSetMetadata rightMetadata) {
    Map<String, Integer> columnMap = new HashMap<>();
    List<ColumnMetadata> metadataList = new ArrayList<>();
    if (leftMetadata != null && rightMetadata != null) {
      for (ColumnMetadata metadata : leftMetadata.getColumns()) {
        columnMap.put(metadata.getName(), 1);
        metadataList.add(metadata);
      }
      for (ColumnMetadata metadata : rightMetadata.getColumns()) {
        if (metadataList.contains(metadata)) {
          int index = columnMap.get(metadata.getName());
          ColumnMetadata columnMetadata = new GeneralColumnMetadata(
              metadata.getName() + index);
          columnMap.put(metadata.getName(), index + 1);
          columnMetadata.setType(metadata.getType());
          metadataList.add(columnMetadata);
        } else {
          metadataList.add(metadata);
        }
      }
    }
    ColumnSetMetadata columnSetMetadata = new GeneralColumnSetMetadata("/");
    columnSetMetadata.setColumns(metadataList);
    return columnSetMetadata;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        JoinDescriptor.PARAM_COLUMN_SET);
    leftColumns = new String[columnSetParameter.getParameters().size()];
    rightColumns = new String[columnSetParameter.getParameters().size()];
    int i = 0;
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      leftColumns[i] = rowParameter.getParameterValue(
          JoinDescriptor.PARAM_LEFTCOLUMN);
      rightColumns[i] = rowParameter.getParameterValue(
          JoinDescriptor.PARAM_RIGHTCOLUMN);
      i++;
    }
    joinType = parameters.getParameterValue(JoinDescriptor.PARAM_JOIN_TYPE);
  }
}
