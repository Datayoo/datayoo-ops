package org.datayoo.oyez.op.processing.v.transformer;

import org.apache.commons.lang3.ObjectUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.util.ProcessOperatorHelper;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.colset.ColumnInfo;
import org.datayoo.sengee.op.processing.v.transformer.ValuesExtractDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串截取
 *
 * @author hhn
 */
@OpDefiner(name = "ValuesExtract",
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
public class ValuesExtract extends BaseProcessOperator {
  //正则
  private Pattern[] regPatterns;
  //字段参数数组
  private String[] columns;
  //是否已获取了字段所在index

  private ColumnInfo[] columnInfos;

  public ValuesExtract(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        ValuesExtractDescriptor.PARAM_COLUMN_SET);
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    columns = new String[rowParameters.size()];
    regPatterns = new Pattern[rowParameters.size()];
    int i = 0;
    for (RowParameter rowParameter : rowParameters) {
      columns[i] = rowParameter.getParameterValue(
          ValuesExtractDescriptor.PARAM_COLUMN_NAME);
      regPatterns[i] = Pattern.compile(
          rowParameter.getParameterValue(ValuesExtractDescriptor.PARAM_REGEX));
      i++;
    }
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    if (ObjectUtils.isNotEmpty(objects)) {
      if (columnInfos == null) {
        loadColumnIndex(columnSetMetadata);
      }
      for (ColumnInfo columnInfo : columnInfos) {
        String value = (String) objects[columnInfo.getColumnIndex()];
        objects[columnInfo.getColumnIndex()] = extractString(value,
            (Pattern) columnInfo.getPayload());
      }
    }
    return objects;
  }

  protected String extractString(String value, Pattern pattern) {
    if (value == null || value.isEmpty())
      return value;
    Matcher m = pattern.matcher(value);
    if (m.find() && m.groupCount() > 0) {
      return m.group(1);
    }
    return null;
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return columnSetMetadata;
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

  //获取字段所在index
  private void loadColumnIndex(ColumnSetMetadata columnSetMetadata) {
    columnInfos = ProcessOperatorHelper.bindColumnInfos(columnSetMetadata,
        columns, regPatterns);
    columns = null;
    regPatterns = null;
  }
}
