package org.datayoo.oyez.op.processing.r.grouping;

import org.datayoo.base.types.DataType;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.DataSetMap;
import org.datayoo.moql.DataSetMapImpl;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.util.MoqlUtils;
import org.datayoo.oyez.op.processing.util.ProcessOperatorHelper;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.colset.ColumnInfo;
import org.datayoo.sengee.datax.mapper.ColumnMappingEntry;
import org.datayoo.sengee.op.processing.r.grouping.AggregateDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.List;

@OpDefiner(name = "Aggregate",
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
public class Aggregate extends BaseProcessOperator {

  public ColumnInfo[] columnInfos;

  private String[] columns;

  private String[][] payloads;
  //分组列
  private String[] groupingParams;

  private String sql;

  public Aggregate(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    List<ColumnMetadata> columnMetadataList = new ArrayList<>();
    for (int i = 0; i < groupingParams.length; i++) {
      String column = groupingParams[i];
      ColumnMetadata columnMetadata = columnSetMetadata.getColumn(column);
      columnMetadataList.add(columnMetadata);
    }
    //函数列
    for (int i = 0; i < columnInfos.length; i++) {
      ColumnInfo columnInfo = columnInfos[i];
      ColumnMetadata columnMetadata = columnInfo.getColumnMetadata();
      String[] payload = (String[]) columnInfo.getPayload();
      DataType dataType = AggregateDescriptor.dataTypeMap.getOrDefault(
          payload[0], columnMetadata.getType());
      ColumnMappingEntry mappingEntry = new ColumnMappingEntry(payload[1],
          dataType);
      //这里用mappingEntry自行生成，是因为分组列和统计列里有重复字段，修改
      //columnMetadata会影响其他列
      columnMetadataList.add(mappingEntry.toColumnMetadata());
    }
    columnSetMetadata.setColumns(columnMetadataList);
    return columnSetMetadata;
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    throw new UnsupportedOperationException("");
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = readAll(inputPort);
    List<Object[]> rows = rowSet.getRows();
    if (!rows.isEmpty()) {
      ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
      buildSql(columnSetMetadata);
      PlRowSet outSet = createRowSet(
          this.createOutputColumnSetMetadata(columnSetMetadata));
      DataSetMap dataSetMap = new DataSetMapImpl();
      dataSetMap.putDataSet("ROWS", rows);
      outSet.addRows(MoqlUtils.execute(sql, dataSetMap));
      outputPort.write(outSet,this.inputPort.getWaterMark());
    }
  }

  private void buildSql(ColumnSetMetadata columnSetMetadata) {
    columnInfos = ProcessOperatorHelper.bindColumnInfos(columnSetMetadata,
        columns, payloads);
    //分组列所在index
    int[] groupingIndexes = ProcessOperatorHelper.bindIndexes(columnSetMetadata,
        groupingParams, null);
    StringBuilder sb = new StringBuilder();
    sb.append("select ");
    StringBuilder condition = new StringBuilder();
    if (groupingIndexes.length > 0) {
      condition.append(" ").append("group by ");
      for (int i = 0; i < groupingIndexes.length; i++) {
        if (i > 0) {
          condition.append(",");
        }
        if (i > 0) {
          sb.append(",");
        }
        condition.append("row[").append(groupingIndexes[i]).append("]");
        sb.append("row[").append(groupingIndexes[i]).append("]");
      }
    }
    for (int i = 0; i < columnInfos.length; i++) {
      if (groupingIndexes.length > 0) {
        sb.append(",");
      }
      ColumnInfo columnInfo = columnInfos[i];
      String[] payloads = (String[]) columnInfo.getPayload();
      if (!"".equals(payloads[1])) {
        if (payloads[0].equals(AggregateDescriptor.PARAM_DISTINCT_COUNT)) {
          sb.append("count(row[").append(columnInfo.getColumnIndex())
              .append("],true) ").append(payloads[1]);
        } else if (payloads[0].equals(AggregateDescriptor.PARAM_JOINT)) {
          sb.append("joint(row[").append(columnInfo.getColumnIndex())
              .append("],'') ").append(payloads[1]);
        } else if (payloads[0].equals(AggregateDescriptor.PARAM_JOINT_BY_COMMA)) {
          sb.append("joint(row[").append(columnInfo.getColumnIndex())
              .append("],',') ").append(payloads[1]);
        } else {
          sb.append(payloads[0]).append("(row[")
              .append(columnInfo.getColumnIndex()).append("]) ")
              .append(payloads[1]);
        }
      }
    }
    sb.append(" from ROWS row");

    sb.append(condition);
    sql = sb.toString();
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        AggregateDescriptor.PARAM_COLUMN_SET);
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    int i = 0;
    columns = new String[rowParameters.size()];
    payloads = new String[rowParameters.size()][2];
    for (RowParameter rowParameter : rowParameters) {
      columns[i] = rowParameter.getParameterValue(
          AggregateDescriptor.PARAM_COLUMN_NAME);
      payloads[i] = new String[] {
          rowParameter.getParameterValue(AggregateDescriptor.PARAM_FUNCTION),
          rowParameter.getParameterValue(AggregateDescriptor.PARAM_ALIAS,
              rowParameter.getParameterValue(
                  AggregateDescriptor.PARAM_COLUMN_NAME)),
      };
      i++;
    }
    TableParameter groupingParameter = (TableParameter) parameters.getParameter(
        AggregateDescriptor.PARAM_GROUPING_PARAMS);
    List<RowParameter> groupingParameters = groupingParameter.getParameters();
    i = 0;
    groupingParams = new String[groupingParameters.size()];
    for (RowParameter rowParameter : groupingParameters) {
      groupingParams[i] = rowParameter.getParameterValue(
          AggregateDescriptor.PARAM_COLUMN_NAME);
      i++;
    }
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
