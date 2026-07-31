package org.datayoo.sengee.op.processing.r.grouping;

import org.datayoo.base.types.ArrayType;
import org.datayoo.base.types.DataType;
import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.footstone.sightx.annotation.Compox;
import org.datayoo.footstone.sightx.annotation.DataDesc;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.mapper.ColumnMappingEntry;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 分组统计
 *
 * @author hhn
 */
@OpDefiner(name = "Aggregate",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,grouping")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parametertable name=\"columnSet\" c_Alias=\"聚合属性\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"true\" c_Alias=\"列名\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "<parameter name=\"function\" c_Option=\"true\" c_Alias=\"函数\" c_Compox=\"sengee-funcCtl\"></parameter>"
            + "<parameter name=\"alias\" c_Option=\"true\" c_Alias=\"重命名\" c_Compox=\"sightx-input\"></parameter>"
            + "</head></parametertable>"
            + "<parametertable name=\"groupingParams\" c_Alias=\"分组列\" c_Compox=\"sengee-columns-selector\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"列名\"></parameter>"
            + "</head></parametertable>" + "</parametergroup></parameters>",
    compoxes = { @Compox(id = "sengee-funcCtl",
        parentId = "sightx-combox",
        visibleDescriptors = {},
        dataDescriptor = @DataDesc(defaultValue = "avg",
            data = "[{\"label\": \"平均值\", \"value\": \"avg\"}, "
                + "{\"label\": \"计数\", \"value\": \"count\"},{\"label\": \"去重计数\", \"value\": \"distinctCount\"},"
                + "{\"label\": \"峰度\", \"value\": \"kurtosis\"}, {\"label\": \"首值\", \"value\": \"first\"},"
                + "{\"label\": \"末值\", \"value\": \"last\"}, {\"label\": \"最大值\", \"value\": \"max\"},"
                + "{\"label\": \"中位数\", \"value\": \"median\"}, {\"label\": \"最小值\", \"value\": \"min\"},"
                + "{\"label\": \"Mode\", \"value\": \"mode\"}, {\"label\": \"非空\", \"value\": \"notNull\"},"
                + "{\"label\": \"百分位\", \"value\": \"percentile\"}, {\"label\": \"范围\", \"value\": \"range\"},"
                + "{\"label\": \"半方差\", \"value\": \"semiVariance\"}, {\"label\": \"偏度\", \"value\": \"skewness\"},"
                + "{\"label\": \"标准差\", \"value\": \"standardDeviation\"}, {\"label\": \"总和\", \"value\": \"sum\"},"
                + "{\"label\": \"方差\", \"value\": \"variance\"},{\"label\": \"字符串连接\", \"value\": \"joint\"},"
                + "{\"label\": \"字符串分隔连接\", \"value\": \"jointByComma\"},{\"label\": \"聚合为数组\", \"value\": \"group2Array\"}]"))
    })
public class AggregateDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_GROUPING_PARAMS = "groupingParams";
  public static final String PARAM_FUNCTION = "function";
  public static final String PARAM_ALIAS = "alias";

  public static final String PARAM_DISTINCT_COUNT = "distinctCount";
  public static final String PARAM_JOINT = "joint";
  public static final String PARAM_JOINT_BY_COMMA = "jointByComma";
  public static final String PARAM_FIRST = "first";
  public static final Map<String, DataType> dataTypeMap = new HashMap<>();

  static {
    dataTypeMap.put("avg", DataTypeUtils.DOUBLE_TYPE);
    dataTypeMap.put("count", DataTypeUtils.LONG_TYPE);
    dataTypeMap.put("kurtosis", DataTypeUtils.DOUBLE_TYPE);
    dataTypeMap.put("semiVariance", DataTypeUtils.DOUBLE_TYPE);
    dataTypeMap.put("skewness", DataTypeUtils.DOUBLE_TYPE);
    dataTypeMap.put("standardDeviation", DataTypeUtils.DOUBLE_TYPE);
    dataTypeMap.put("variance", DataTypeUtils.DOUBLE_TYPE);
    dataTypeMap.put("group2Array", new ArrayType(DataTypeUtils.OBJECT_TYPE));
  }

  //0为字段名，1为函数名，2为重命名
  //分组列在前，统计列在后
  private String[][] columns;

  public AggregateDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    ColumnSetMetadata columnSetMetadata = getInputColumnSetMetadata();
    List<ColumnMetadata> columnMetadataList = new ArrayList<>();
    for (int i = 0; i < columns.length; i++) {
      String[] column = columns[i];
      if (null == column[1] && null != column[0] && null != column[2]) {
        //分组列,1为null，这里判断另外两列是因为聚合属性有可能添加了一列空的情况下查询输出列级
        ColumnMetadata columnMetadata = columnSetMetadata.getColumn(column[0]);
        columnMetadataList.add(columnMetadata);
      } else {
        if (null == column[0]) {
          continue;
        }
        ColumnMetadata columnMetadata = columnSetMetadata.getColumn(column[0]);
        DataType dataType = dataTypeMap.getOrDefault(column[1],
            columnMetadata.getType());
        ColumnMappingEntry mappingEntry = new ColumnMappingEntry(column[2],
            dataType);
        //这里用mappingEntry自行生成，是因为分组列和统计列里有重复字段，修改
        //columnMetadata会影响其他列
        columnMetadataList.add(mappingEntry.toColumnMetadata());
      }
    }
    columnSetMetadata.setColumns(columnMetadataList);
    return columnSetMetadata;
  }

  @Override
  protected void readParameters() {
    loadColumnSetFromParameters();
  }

  @Override
  protected void innerInitialize() {

  }

  protected void loadColumnSetFromParameters() {
    TableParameter columnSetParameter = getColumnSetParameter();
    TableParameter groupingParameter = (TableParameter) parameters.getParameter(
        PARAM_GROUPING_PARAMS);
    List<RowParameter> groupingParameters = groupingParameter.getParameters();
    columns = new String[columnSetParameter.getParameters().size()
        + groupingParameters.size()][3];
    int i = 0;
    for (RowParameter rowParameter : groupingParameters) {
      columns[i] = new String[] { rowParameter.getParameterValue(
          AbstractProcessingDescriptor.PARAM_COLUMN_NAME), null,
          rowParameter.getParameterValue(
              AbstractProcessingDescriptor.PARAM_COLUMN_NAME)
      };
      i++;
    }
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      //重命名，如果为空，默认使用函数名+括号+字段名,如count(name)
      columns[i] = new String[] { rowParameter.getParameterValue(
          AbstractProcessingDescriptor.PARAM_COLUMN_NAME),
          rowParameter.getParameterValue(PARAM_FUNCTION),
          rowParameter.getParameterValue(PARAM_ALIAS,
              rowParameter.getParameterValue(
                  AbstractProcessingDescriptor.PARAM_COLUMN_NAME))
      };
      i++;
    }
  }

  @Override
  protected void innerDestroy() {

  }

  @Override
  protected void validateParameters() {
    //校验聚合的重命名不可包含function
    for (int i = 0; i < columns.length; i++) {
      String[] columnParameters = columns[i];
      String regex = columnParameters[1] + "\\(.*\\)$";
      if (null != columnParameters[1]) {
        String rename = String.valueOf(columnParameters[2]);
        Pattern pattern = Pattern.compile(regex);
        if (pattern.matcher(rename).find()) {
          throw new OperationRuntimeException(
              OperatorsI18nMessageResource.format(
                  OperatorsExceptionConstants.ECM_PARAM_INVALID_FORMAT, this.alias,
                  PARAM_ALIAS));
        }
      }
    }
    TableParameter groupingParameter = (TableParameter) parameters.getParameter(
        PARAM_GROUPING_PARAMS);
    List<RowParameter> groupingParameters = groupingParameter.getParameters();
    if (groupingParameters.size() == 0) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_GROUPING_PARAMS));
    }
    TableParameter columnSetParameter = getColumnSetParameter();
    if (columnSetParameter.getParameters().size() == 0) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_COLUMN_SET));
    }
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      String column = rowParameter.getParameterValue(
          AbstractProcessingDescriptor.PARAM_COLUMN_NAME, "");
      if (column.isEmpty()) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
            AbstractProcessingDescriptor.PARAM_COLUMN_NAME));
      }
      String function = rowParameter.getParameterValue(PARAM_FUNCTION, "");
      if (function.isEmpty()) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
            PARAM_FUNCTION));
      }
    }
  }
}
