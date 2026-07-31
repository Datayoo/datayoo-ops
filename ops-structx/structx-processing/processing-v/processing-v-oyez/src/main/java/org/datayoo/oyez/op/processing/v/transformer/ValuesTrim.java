package org.datayoo.oyez.op.processing.v.transformer;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.util.ProcessOperatorHelper;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.v.transformer.ValuesTrimDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@OpDefiner(name = "ValuesTrim",
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
public class ValuesTrim extends BaseProcessOperator {

  protected String[] columns;

  protected int[] columnIndexes;

  public ValuesTrim(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    loadColumnSetFromParameters();
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return columnSetMetadata;
  }

  protected PlRowSet trimRowSet(ColumnSetMetadata columnSetMetadata,
      PlRowSet inputRowSet) {
    PlRowSet plRowSet = createRowSet(columnSetMetadata);
    int columnSize = columnSetMetadata.getColumns().size();
    for (int i = 0; i < inputRowSet.getRowsCount(); i++) {

      Map<String, Object> map = inputRowSet.getRowAsMap(i);
      for (String column : columns) {
        String value = (String) map.get(column);
        if (StringUtils.isNotEmpty(value)) {
          map.put(column, value.trim());
        }
      }
      plRowSet.addRowAsMap(map);
    }
    return plRowSet;
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    if (ObjectUtils.isNotEmpty(objects)) {
      for (int index : columnIndexes) {
        if (StringUtils.isNotEmpty((String) objects[index])) {
          objects[index] = ((String) objects[index]).trim();
        }
      }
    }
    return objects;
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

  protected void loadColumnSetFromParameters() {
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        ValuesTrimDescriptor.PARAM_COLUMN_SET);
    columns = new String[columnSetParameter.getParameters().size()];
    columnIndexes = new int[columns.length];
    int i = 0;
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      String colName = rowParameter.getParameterValue(
          ValuesTrimDescriptor.PARAM_COLUMN_NAME);
      columns[i++] = colName;
    }
  }

  @Override
  protected void preBindColumnSetMetadata(ColumnSetMetadata columnSetMetadata) {
    columnIndexes = ProcessOperatorHelper.bindIndexes(columnSetMetadata,
        columns, null);
  }
}
