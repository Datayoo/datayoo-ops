package org.datayoo.oyez.op.processing.st;

import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.*;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.util.ProcessOperatorHelper;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.st.RowTranspositionDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 字符串截取
 *
 * @author hhn
 */
@OpDefiner(name = "RowTransposition",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    replica = -1,
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
public class RowTransposition extends BaseProcessOperator {

  private String headColumn;
  private int headColumnIndex = -1;

  private String[] columns;
  private int[] columnsIndexes;
  private String[] groupColumns;
  private int[] groupColumnsIndexes;

  private String[] outputColumns;

  private Map<String, Object[]> result = new TreeMap<>();

  public RowTransposition(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    headColumn = groupParameter.getParameterValue(
        RowTranspositionDescriptor.PARAM_HEADER_COLUMN);
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        RowTranspositionDescriptor.PARAM_COLUMN_SET);
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    columns = new String[rowParameters.size()];
    columnsIndexes = new int[rowParameters.size()];
    int i = 0;
    for (RowParameter rowParameter : rowParameters) {
      columns[i] = rowParameter.getParameterValue(
          RowTranspositionDescriptor.PARAM_COLUMN_NAME);
      i++;
    }
    int j = 0;
    TableParameter groupTableParameter = (TableParameter) parameters.getParameter(
        RowTranspositionDescriptor.PARAM_GROUP_COLUMN_SET);
    List<RowParameter> groupParameters = groupTableParameter.getParameters();
    groupColumns = new String[groupParameters.size()];
    groupColumnsIndexes = new int[groupParameters.size()];
    for (RowParameter rowParameter : groupParameters) {
      groupColumns[j] = rowParameter.getParameterValue(
          RowTranspositionDescriptor.PARAM_COLUMN_NAME);
      j++;
    }
  }

  protected void innerOperate(ColumnSetMetadata outputColumnSetMetadata,
      Object[] row) {
    for (int j = 0; j < columnsIndexes.length; j++) {
      //输出列级是按照header列的值写
      //这里按照输出列级的名字来判断，比如header列是第0列，输出列级name是a,b，分组列为第1列，输出列为第3列
      //就需要遍历数据，读取header位置的值，该值为a时才会构造数据
      //数据为分组列的值【可能为多个分组字段】，第3列的名称【输出列可能为多列，每个列和分组字段构建一条数据】
      Object headValue = row[headColumnIndex];
      for (int i = 0; i < outputColumns.length; i++) {
        if (outputColumns[i].equals(String.valueOf(headValue))) {
          int groupSize = groupColumnsIndexes.length;
          Object[] groupObjects = new Object[groupSize + 1];
          StringBuilder key = new StringBuilder();
          //分组列
          for (int k = 0; k < groupSize; k++) {
            key.append(row[groupColumnsIndexes[k]]).append(k);
            groupObjects[k] = row[groupColumnsIndexes[k]];
          }
          //meta列
          groupObjects[groupSize] = columns[j];
          key.append(columns[j]);
          Object[] objects;
          if (result.containsKey(key.toString())) {
            objects = result.get(key.toString());
          } else {
            objects = new Object[outputColumnSetMetadata.getColumns().size()];
            System.arraycopy(groupObjects, 0, objects, 0, groupObjects.length);
          }
          objects[groupObjects.length + i] = row[columnsIndexes[j]];
          result.put(key.toString(), objects);
        }
      }
    }
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = readAll(inputPort);
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    ColumnSetMetadata outputColumnSetMetadata = this.createOutputColumnSetMetadata(
        columnSetMetadata);
    PlRowSet outSet = createRowSet(outputColumnSetMetadata);
    List<Object[]> rows = rowSet.getRows();
    if (rows.size() > 0) {
      loadColumnIndex(columnSetMetadata);
      for (Object[] row : rows) {
        innerOperate(outputColumnSetMetadata, row);
      }
      outSet.addRows(new ArrayList<>(result.values()));
      outputPort.write(outSet, inputPort.getWaterMark());
    }
  }

  //获取字段所在index
  private void loadColumnIndex(ColumnSetMetadata columnSetMetadata) {
    headColumnIndex = columnSetMetadata.getColumnIndex(headColumn);
    columnsIndexes = ProcessOperatorHelper.bindIndexes(columnSetMetadata,
        columns, null);
    groupColumnsIndexes = ProcessOperatorHelper.bindIndexes(columnSetMetadata,
        groupColumns, null);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    ColumnSetMetadata outputColumnSetMetadata = new GeneralColumnSetMetadata(
        "/");
    TableParameter outputTableParameter = (TableParameter) this.parameters.getParameter(
        RowTranspositionDescriptor.PARAM_OUTPUT_COLUMN_SET);
    TableParameter groupTableParameter = (TableParameter) this.parameters.getParameter(
        RowTranspositionDescriptor.PARAM_GROUP_COLUMN_SET);
    for (RowParameter rowParameter : groupTableParameter.getParameters()) {
      ColumnMetadata columnMetadata = new GeneralColumnMetadata(
          rowParameter.getParameterValue(
              RowTranspositionDescriptor.PARAM_COLUMN_NAME));
      columnMetadata.setType(DataTypeUtils.STRING_TYPE);
      outputColumnSetMetadata.addColumn(columnMetadata);
    }
    ColumnMetadata metaColumnMetadata = new GeneralColumnMetadata("META");
    metaColumnMetadata.setType(DataTypeUtils.STRING_TYPE);
    outputColumnSetMetadata.addColumn(metaColumnMetadata);
    outputColumns = new String[outputTableParameter.getParameters().size()];
    int i = 0;
    for (RowParameter rowParameter : outputTableParameter.getParameters()) {
      String columnName = rowParameter.getParameterValue(
          RowTranspositionDescriptor.PARAM_COLUMN_NAME);
      String columnType = rowParameter.getParameterValue(
          RowTranspositionDescriptor.PARAM_COLUMN_TYPE);
      ColumnMetadata columnMetadata = new GeneralColumnMetadata(columnName);
      columnMetadata.setType(
          DataTypeUtils.createDataTypeByTypeString(columnType));
      outputColumnSetMetadata.addColumn(columnMetadata);
      outputColumns[i] = columnName;
      i++;
    }
    return outputColumnSetMetadata;
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    return new Object[0];
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
