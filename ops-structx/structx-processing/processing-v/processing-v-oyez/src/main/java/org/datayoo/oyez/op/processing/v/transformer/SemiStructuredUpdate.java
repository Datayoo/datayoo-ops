package org.datayoo.oyez.op.processing.v.transformer;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.MoqlException;
import org.datayoo.moql.Operand;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.util.ExpressionFactory;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.v.transformer.SemiStructuredUpdateDescriptor;
import org.datayoo.sengee.op.processing.v.transformer.ValuesCutDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tangtadin
 * @version 1.0
 * @description: TODO
 * @date 2021/7/13 11:19 PM
 */
@OpDefiner(name = "SemiStructuredUpdate",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class SemiStructuredUpdate extends BaseProcessOperator {

  //0是colName,1是path,2是value,3是index
  private Object[][] params;

  private boolean bind = false;
  private boolean auto = false;

  public SemiStructuredUpdate(FlowNodeMetadata operatorMetadata,
      FlowNode parent, EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    if (!bind) {
      bind(columnSetMetadata);
    }
    for (Object[] param : params) {
      int index = (int) param[3];
      Object src = objects[index];
      String path = (String) objects[(int) param[4]];
      Object dst = objects[(int) param[5]];
      update(src, path.split("\\."), dst, 0);
    }
    return objects;
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return columnSetMetadata;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    auto = groupParameter.getParameterValueAsBoolean(
        SemiStructuredUpdateDescriptor.PARAM_AUTO);
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        ValuesCutDescriptor.PARAM_COLUMN_SET);
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    this.params = new Object[rowParameters.size()][6];
    int i = 0;
    for (RowParameter rowParameter : rowParameters) {
      params[i][0] = rowParameter.getParameterValue(
          SemiStructuredUpdateDescriptor.PARAM_COLUMN_NAME);
      params[i][1] = rowParameter.getParameterValue(
          SemiStructuredUpdateDescriptor.PARAM_PATH);
      params[i][2] = rowParameter.getParameterValue(
          SemiStructuredUpdateDescriptor.PARAM_VALUE);
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

  protected void bind(ColumnSetMetadata columnSetMetadata) {
    for (int i = 0; i < params.length; i++) {
      params[i][3] = columnSetMetadata.getColumnIndex((String) params[i][0]);
      params[i][4] = columnSetMetadata.getColumnIndex((String) params[i][1]);
      params[i][5] = columnSetMetadata.getColumnIndex((String) params[i][2]);
    }
    bind = true;
  }

  protected void update(Object src, String[] paths, Object value, int i) {
    if (src instanceof Map) {
      Object temp = ((Map<String, Object>) src).get(paths[i]);
      if (i == paths.length - 1) {
        ((Map<String, Object>) src).put(paths[i], value);
      } else {
        if (temp == null && auto) {
          temp = new HashMap();
          ((Map<String, Object>) src).put(paths[i], temp);
        }
        update(temp, paths, value, i + 1);
      }
    }
  }
}
