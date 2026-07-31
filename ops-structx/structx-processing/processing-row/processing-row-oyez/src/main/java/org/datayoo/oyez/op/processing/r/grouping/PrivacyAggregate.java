package org.datayoo.oyez.op.processing.r.grouping;

import com.google.gson.Gson;
import org.datayoo.base.types.DataType;
import org.datayoo.base.types.DataTypeName;
import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.funcx.*;
import org.datayoo.moql.DataSetMap;
import org.datayoo.moql.DataSetMapImpl;
import org.datayoo.moql.engine.MoqlEngine;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.util.MoqlUtils;
import org.datayoo.oyez.op.processing.util.ProcessOperatorHelper;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.colset.ColumnInfo;
import org.datayoo.sengee.datax.mapper.ColumnMappingEntry;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.r.grouping.PrivacyAggregateDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OpDefiner(name = "PrivacyAggregate",
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
public class PrivacyAggregate extends BaseProcessOperator {

  private ColumnInfo[] columnInfos;

  private String[] columns;

  private String[][] payloads;
  //分组列
  private String[] groupingParams;

  private String sql;

  protected static Map<String, String> funcsMapping = new HashMap<>();

  protected Map<String, Object> confuseParams = new HashMap<>();

  protected Gson gson = new Gson();

  static {
    MoqlEngine.registFunction(PrivacyCount.FUNCTION_NAME,
        PrivacyCount.class.getName());
    MoqlEngine.registFunction(PrivacyAvg.FUNCTION_NAME,
        PrivacyAvg.class.getName());
    MoqlEngine.registFunction(PrivacySum.FUNCTION_NAME,
        PrivacySum.class.getName());
    MoqlEngine.registFunction(PrivacyVariance.FUNCTION_NAME,
        PrivacyVariance.class.getName());
    MoqlEngine.registFunction(PrivacyQuantiles.FUNCTION_NAME,
        PrivacyQuantiles.class.getName());
    funcsMapping.put("avg", PrivacyAvg.FUNCTION_NAME);
    funcsMapping.put("count", PrivacyCount.FUNCTION_NAME);
    funcsMapping.put("quantiles", PrivacyQuantiles.FUNCTION_NAME);
    funcsMapping.put("sum", PrivacySum.FUNCTION_NAME);
    funcsMapping.put("variance", PrivacyVariance.FUNCTION_NAME);
  }

  public PrivacyAggregate(FlowNodeMetadata operatorMetadata, FlowNode parent,
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
      DataTypeName dataTypeName = PrivacyAggregateDescriptor.dataTypeMap.getOrDefault(
          payload[0], columnMetadata.getType().getName());
      DataType dataType = DataTypeUtils.createDataType(dataTypeName, null,
          null);
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
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    List<Object[]> rows = rowSet.getRows();
    if (rows.size() > 0) {
      buildSql(columnSetMetadata);
      PlRowSet outSet = createRowSet(
          this.createOutputColumnSetMetadata(columnSetMetadata));
      DataSetMap dataSetMap = new DataSetMapImpl();
      dataSetMap.putDataSet("ROWS", rows);
      try{
        outSet.addRows(MoqlUtils.execute(sql, dataSetMap));
      }catch (Exception e){
        throw new OperationRuntimeException(
            String.format("[%s]data aggregate failed! %s", this.alias,e), e);
      }
      outputPort.write(outSet, inputPort.getWaterMark());
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
      sb.append(funcsMapping.get(payloads[0])).append("(row[")
          .append(columnInfo.getColumnIndex()).append("],'").append(gson.toJson(confuseParams)).append("') ")
          .append(payloads[1]);
    }
    sb.append(" from ROWS row");
    sb.append(condition);
    sql = sb.toString();
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        PrivacyAggregateDescriptor.PARAM_COLUMN_SET);
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    int i = 0;
    columns = new String[rowParameters.size()];
    payloads = new String[rowParameters.size()][2];
    for (RowParameter rowParameter : rowParameters) {
      columns[i] = rowParameter.getParameterValue(
          PrivacyAggregateDescriptor.PARAM_COLUMN_NAME);
      payloads[i] = new String[] { rowParameter.getParameterValue(
          PrivacyAggregateDescriptor.PARAM_FUNCTION),
          rowParameter.getParameterValue(PrivacyAggregateDescriptor.PARAM_ALIAS,
              rowParameter.getParameterValue(
                  PrivacyAggregateDescriptor.PARAM_COLUMN_NAME))
      };
      i++;
    }
    TableParameter groupingParameter = (TableParameter) parameters.getParameter(
        PrivacyAggregateDescriptor.PARAM_GROUPING_PARAMS);
    List<RowParameter> groupingParameters = groupingParameter.getParameters();
    i = 0;
    groupingParams = new String[groupingParameters.size()];
    for (RowParameter rowParameter : groupingParameters) {
      groupingParams[i] = rowParameter.getParameterValue(
          PrivacyAggregateDescriptor.PARAM_COLUMN_NAME);
      i++;
    }
    GroupParameter confuseParameters = (GroupParameter) parameters.getParameter(
        PrivacyAggregateDescriptor.PARAM_CONFUSE_PARAMS);
    confuseParams.put(PrivacyAggregateDescriptor.PARAM_DELTA,
        confuseParameters.getParameterValueAsDouble(
            PrivacyAggregateDescriptor.PARAM_DELTA));
    confuseParams.put(PrivacyAggregateDescriptor.PARAM_EPSILON,
        confuseParameters.getParameterValueAsDouble(
            PrivacyAggregateDescriptor.PARAM_EPSILON));
    confuseParams.put(PrivacyAggregateDescriptor.PARAM_NOISE,
        confuseParameters.getParameterValue(
            PrivacyAggregateDescriptor.PARAM_NOISE));
    confuseParams.put(PrivacyAggregateDescriptor.PARAM_LOWER,
        confuseParameters.getParameterValueAsInt(
            PrivacyAggregateDescriptor.PARAM_LOWER));
    confuseParams.put(PrivacyAggregateDescriptor.PARAM_UPPER,
        confuseParameters.getParameterValueAsInt(
            PrivacyAggregateDescriptor.PARAM_UPPER));
    confuseParams.put(
        PrivacyAggregateDescriptor.PARAM_MAX_CONTRIBUTIONS_PER_PARTITION,
        confuseParameters.getParameterValueAsInt(
            PrivacyAggregateDescriptor.PARAM_MAX_CONTRIBUTIONS_PER_PARTITION));
    confuseParams.put(
        PrivacyAggregateDescriptor.PARAM_MAX_PARTITIONS_CONTRIBUTED,
        confuseParameters.getParameterValueAsInt(
            PrivacyAggregateDescriptor.PARAM_MAX_PARTITIONS_CONTRIBUTED));
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

  @Override
  protected void finalize() throws Throwable {
    MoqlEngine.unregistFunction(PrivacyCount.FUNCTION_NAME);
    MoqlEngine.unregistFunction(PrivacyAvg.FUNCTION_NAME);
    MoqlEngine.unregistFunction(PrivacySum.FUNCTION_NAME);
    MoqlEngine.unregistFunction(PrivacyVariance.FUNCTION_NAME);
    MoqlEngine.unregistFunction(PrivacyQuantiles.FUNCTION_NAME);
  }
}
