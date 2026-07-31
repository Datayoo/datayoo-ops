package org.datayoo.oyez.op.processing.v.transformer;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.v.transformer.ValuesReplaceDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "ValuesReplace",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "oyez",
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters = "",
    compoxes = {})
public class ValuesReplace extends BaseProcessOperator {

  private String[][] replaceParams;

  private String columnName;

  private int columnIndex = -1;

  public ValuesReplace(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) this.parameters
        .getParameter(ValuesReplaceDescriptor.PARAM_COLUMN_SET);
    int i = 0;
    columnName = parameters
        .getParameterValue(ValuesReplaceDescriptor.PARAM_COLUMN_NAME);
    replaceParams = new String[columnSetParameter.getParameters().size()][2];
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      String regex = rowParameter
          .getParameterValue(ValuesReplaceDescriptor.PARAM_REGEX);
      String replacement = rowParameter
          .getParameterValue(ValuesReplaceDescriptor.PARAM_REPLACEMENT,"");
      replaceParams[i][0] = regex;
      replaceParams[i][1] = replacement;
      i++;
    }
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

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    if(columnIndex == -1){
      loadColumnIndex(columnSetMetadata);
    }
    String data = (String) objects[columnIndex];
    for (int j = 0; j < replaceParams.length; j++) {
      data = data.replaceAll(replaceParams[j][0], replaceParams[j][1]);
    }
    objects[columnIndex] = data;
    return objects;
  }

  //获取字段所在index
  private void loadColumnIndex(ColumnSetMetadata columnSetMetadata) {
    columnIndex = columnSetMetadata.getColumnIndex(columnName);
  }
}
