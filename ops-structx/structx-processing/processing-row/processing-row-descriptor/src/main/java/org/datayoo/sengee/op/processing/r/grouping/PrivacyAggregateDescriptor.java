package org.datayoo.sengee.op.processing.r.grouping;

import org.datayoo.base.types.DataType;
import org.datayoo.base.types.DataTypeName;
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
 * 隐私统计
 *
 * @author hhn
 */
@OpDefiner(name = "PrivacyAggregate",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "domain,security,privacy")
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
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"列名\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "<parameter name=\"function\" c_Option=\"false\" c_Alias=\"函数\" c_Compox=\"sengee-funcCtl\"></parameter>"
            + "<parameter name=\"alias\" c_Option=\"true\" c_Alias=\"重命名\" c_Compox=\"sightx-input\"></parameter>"
            + "</head></parametertable>"
            + "<parametertable name=\"groupingParams\" c_Alias=\"分组属性\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"列名\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "</head></parametertable>"
            + "<parametergroup name=\"confuseParams\" c_Alias=\"混淆参数设置\">"
            + "<parameter name=\"epsilon\" c_Option=\"false\" c_Alias=\"epsilon变量\" c_Compox=\"sightx-double\">1.0</parameter>"
            + "<parameter name=\"delta\" c_Option=\"false\" c_Alias=\"delta变量\" c_Compox=\"sightx-double\">0.15</parameter>"
            + "<parameter name=\"noise\" c_Option=\"false\" c_Alias=\"混淆算法\" c_Compox=\"noiseType\">GAUSSIAN</parameter>"
            + "<parameter name=\"lower\" c_Option=\"false\" c_Alias=\"值阈下界\" c_Compox=\"sengee-uinteger\">0</parameter>"
            + "<parameter name=\"upper\" c_Option=\"false\" c_Alias=\"值阈上界\" c_Compox=\"sengee-uinteger\">500</parameter>"
            + "<parameter name=\"maxPartitionsContributed\" c_Option=\"false\" c_Alias=\"最大分区贡献\" c_Compox=\"sengee-uinteger\">1</parameter>"
            + "<parameter name=\"maxContributionsPerPartition\" c_Option=\"false\" c_Alias=\"每分区最大贡献\" c_Compox=\"sengee-uinteger\">1</parameter>"
            + "</parametergroup>" + "</parametergroup></parameters>",
    compoxes = { @Compox(id = "sengee-funcCtl",
        parentId = "sightx-combox",
        visibleDescriptors = {},
        dataDescriptor = @DataDesc(defaultValue = "avg",
            data = "[{\"label\": \"平均值\", \"value\": \"avg\"}, "
                + "{\"label\": \"总数\", \"value\": \"count\"},"
                + "{\"label\": \"分位数\", \"value\": \"quantiles\"},"
                + "{\"label\": \"总和\", \"value\": \"sum\"},"
                + "{\"label\": \"方差\", \"value\": \"variance\"}]")),
        @Compox(id = "noiseType",
            parentId = "sightx-combox",
            visibleDescriptors = {},
            dataDescriptor = @DataDesc(defaultValue = "GAUSSIAN",
                data = "[{\"label\": \"高斯混淆\", \"value\": \"GAUSSIAN\"}, "
                    + "{\"label\": \"离散拉普拉斯\", \"value\": \"DISCRETELAPLACE\"},"
                    + "{\"label\": \"拉普拉斯\", \"value\": \"LAPLACE\"}]"))
    })
public class PrivacyAggregateDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_GROUPING_PARAMS = "groupingParams";
  public static final String PARAM_FUNCTION = "function";
  public static final String PARAM_ALIAS = "alias";
  public static final String PARAM_EPSILON = "epsilon";
  public static final String PARAM_DELTA = "delta";
  public static final String PARAM_NOISE = "noise";
  public static final String PARAM_MAX_PARTITIONS_CONTRIBUTED = "maxPartitionsContributed";
  public static final String PARAM_MAX_CONTRIBUTIONS_PER_PARTITION = "maxContributionsPerPartition";
  // 当数值小于该值时，以该值替代
  public static final String PARAM_LOWER = "lower";
  // 当数值大于该值时，以该值替代
  public static final String PARAM_UPPER = "upper";

  public static final String PARAM_CONFUSE_PARAMS = "confuseParams";
  public static final Map<String, DataTypeName> dataTypeMap = new HashMap<>();

  static {
    dataTypeMap.put("privacyAvg", DataTypeName.Double);
    dataTypeMap.put("privacyVariance", DataTypeName.Double);
  }

  //0为字段名，1为函数名，2为重命名
  //分组列在前，统计列在后
  private String[][] columns;

  public PrivacyAggregateDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    ColumnSetMetadata columnSetMetadata = getInputColumnSetMetadata();
    List<ColumnMetadata> columnMetadataList = new ArrayList<>();
    for (int i = 0; i < columns.length; i++) {
      String[] column = columns[i];
      //1为null,存储的是分组列
      if (null == column[1]) {
        ColumnMetadata columnMetadata = columnSetMetadata.getColumn(column[0]);
        columnMetadataList.add(columnMetadata);
      } else {
        ColumnMetadata columnMetadata = columnSetMetadata.getColumn(column[0]);
        DataTypeName dataTypeName = dataTypeMap.getOrDefault(column[1],
            columnMetadata.getType().getName());
        DataType dataType = DataTypeUtils.createDataType(dataTypeName, null,
            null);
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
      columns[i] = new String[] {
          rowParameter.getParameterValue(
              AbstractProcessingDescriptor.PARAM_COLUMN_NAME), null,
          rowParameter.getParameterValue(
              AbstractProcessingDescriptor.PARAM_COLUMN_NAME)
      };
      i++;
    }
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      //重命名，如果为空，默认使用函数名+括号+字段名,如count(name)
      columns[i] = new String[] {
          rowParameter.getParameterValue(
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
    TableParameter columnSetParameter = getColumnSetParameter();
    if (columnSetParameter.getParameters().size() == 0) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          AbstractProcessingDescriptor.PARAM_COLUMN_SET));
    }
    TableParameter groupingParameter = (TableParameter) parameters.getParameter(
        PARAM_GROUPING_PARAMS);
    if (groupingParameter.getParameters().size() == 0) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_GROUPING_PARAMS));
    }
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
  }
}
