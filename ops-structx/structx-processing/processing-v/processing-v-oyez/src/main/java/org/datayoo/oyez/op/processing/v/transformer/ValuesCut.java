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
import org.datayoo.sengee.op.processing.v.transformer.ValuesCutDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.List;

/**
 * 字符串剪切
 *
 * @author hhn
 */
@OpDefiner(name = "ValuesCut",
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
public class ValuesCut extends BaseProcessOperator {
  //索引二维数组，0为beginIndex,1为endIndex
  private int[][] indexes;
  //字段参数数组
  private String[] columns;

  private ColumnInfo[] columnInfos;

  public ValuesCut(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) parameters
        .getParameter(ValuesCutDescriptor.PARAM_COLUMN_SET);
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    columns = new String[rowParameters.size()];
    indexes = new int[rowParameters.size()][2];
    int i = 0;
    for (RowParameter rowParameter : rowParameters) {
      columns[i] = rowParameter
          .getParameterValue(ValuesCutDescriptor.PARAM_COLUMN_NAME);
      indexes[i] = new int[] { rowParameter.getParameterValueAsInt(
          ValuesCutDescriptor.PARAM_BEGIN_INDEX),
          rowParameter.getParameterValueAsInt(
              ValuesCutDescriptor.PARAM_END_INDEX)
      };
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
        int[] indexes = (int[]) columnInfo.getPayload();
        String value = (String) objects[columnInfo.getColumnIndex()];
        objects[columnInfo.getColumnIndex()] = cutString(value, indexes[0],
            indexes[1]);
      }
    }
    return objects;
  }

  //endIndex一定为负
  protected String cutString(String value, int beginIndex, int endIndex) {
    if (value == null || value.isEmpty())
      return value;
    //开始位置超出总长度
    if (beginIndex > value.length() - 1) {
      return "";
    }
    if(endIndex > 0){
      if(beginIndex > endIndex){
        return "";
      }
      if(endIndex > value.length()-1){
        return value.substring(beginIndex);
      }
      return value.substring(beginIndex,endIndex);
    }else{
      if (beginIndex - endIndex >= value.length()) {
        return "";
      } else {
        return value.substring(beginIndex, value.length() + endIndex);
      }
    }
  }

  //获取字段所在index
  private void loadColumnIndex(ColumnSetMetadata columnSetMetadata) {
    columnInfos = ProcessOperatorHelper
        .bindColumnInfos(columnSetMetadata, columns, indexes);
    columns = null;
    indexes = null;
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

}
