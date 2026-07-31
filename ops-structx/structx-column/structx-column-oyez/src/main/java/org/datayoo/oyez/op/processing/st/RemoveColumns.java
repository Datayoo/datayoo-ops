package org.datayoo.oyez.op.processing.st;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.util.ProcessOperatorHelper;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.st.RemoveColumnsDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@OpDefiner(name = "RemoveColumns",
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
public class RemoveColumns extends BaseProcessOperator {

  private int[] indexes;

  private boolean loaded = false;

  private List<String> columns;

  public RemoveColumns(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    GeneralColumnSetMetadata outputSetMetadata = new GeneralColumnSetMetadata(
        "/");
    for (ColumnMetadata columnMetadata : columnSetMetadata.getColumns()) {
      if (!columns.contains(columnMetadata.getName())) {
        outputSetMetadata.addColumn(columnMetadata);
      }
    }
    return outputSetMetadata;
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    if (!loaded) {
      loadColumnInfo(columnSetMetadata);
    }
    Object[] result = new Object[objects.length - indexes.length];
    //需要移除的indexes数组的下标
    int index = 0;
    for (int j = 0; j < objects.length; j++) {
      //indexes需要排序，用户勾选column时不一定会按顺序
      //遍历objects,j != indexes[index]说明之后的object所在列都不需要移除
      //添加index == indexes.length是因为index到最大值时indexes会下标越界
      if (index == indexes.length || j != indexes[index]) {
        result[j - index] = objects[j];
      } else if (j == indexes[index]) {
        //indexes排序后j == indexes[index]表明当前行需要移除
        index++;
      }
    }
    return result;
  }

  private void loadColumnInfo(ColumnSetMetadata columnSetMetadata) {
    indexes = ProcessOperatorHelper.bindIndexes(columnSetMetadata,
        columns.toArray(new String[columns.size()]), null);
    Arrays.sort(indexes);
    loaded = true;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        RemoveColumnsDescriptor.PARAM_COLUMN_SET);
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    columns = new ArrayList<>(rowParameters.size());
    for (RowParameter rowParameter : rowParameters) {
      columns.add(rowParameter.getParameterValue(
          RemoveColumnsDescriptor.PARAM_COLUMN_NAME));
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
