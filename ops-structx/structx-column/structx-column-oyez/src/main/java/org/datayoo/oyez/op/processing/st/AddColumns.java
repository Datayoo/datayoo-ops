package org.datayoo.oyez.op.processing.st;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.EntityMap;
import org.datayoo.moql.EntityMapImpl;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.mapper.ColumnMappingEntry;
import org.datayoo.sengee.op.processing.st.AddColumnsDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@OpDefiner(name = "AddColumns",
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

public class AddColumns extends BaseProcessOperator {

  protected List<ColumnMappingEntry> mappingEntries;

  protected List<ColumnMetadata> addedColumns;

  public AddColumns(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    mappingEntries = AddColumnsDescriptor.loadMappingEntries(this.alias, parameters);
    addedColumns = new ArrayList<>(mappingEntries.size());
    for (ColumnMappingEntry mappingEntry : mappingEntries) {
      addedColumns.add(mappingEntry.toColumnMetadata());
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

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = this.inputPort.read();
    this.preBindColumnSetMetadata(rowSet.getColumnSetMetadata());
    // 创建列集
    ColumnSetMetadata columnSetMetadata = createColumnSetMetadata(
        rowSet.getColumnSetMetadata());
    // 构造数据集
    PlRowSet plRowSet = buildRowSet(columnSetMetadata, rowSet);
    // 输出
    outputPort.write(plRowSet,this.inputPort.getWaterMark());
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    throw new UnsupportedOperationException("");
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    throw new UnsupportedOperationException("");
  }

  protected ColumnSetMetadata createColumnSetMetadata(
      ColumnSetMetadata originalColumnSetMetadata) {
    ColumnSetMetadata columnSetMetadata = new GeneralColumnSetMetadata(
        originalColumnSetMetadata);
    columnSetMetadata.getColumns().addAll(addedColumns);
    return columnSetMetadata;
  }

  protected PlRowSet buildRowSet(ColumnSetMetadata columnSetMetadata,
      PlRowSet inputRowSet) {
    PlRowSet plRowSet = createRowSet(columnSetMetadata);
    int newSize = columnSetMetadata.getColumns().size();
    int oldSize = inputRowSet.getColumnSetMetadata().getColumnsCount();

    for (int i = 0; i < inputRowSet.getRowsCount(); i++) {
      Object[] data = new Object[newSize];
      // 每行新数据
      Map<String, Object> map = inputRowSet.getRowAsMap(i);
      EntityMap entityMap = new EntityMapImpl(map);
      System.arraycopy(inputRowSet.getRow(i), 0, data, 0, oldSize);
      int j = 0;
      for (ColumnMappingEntry mappingEntry : mappingEntries) {
        data[oldSize + j++] = mappingEntry.operate(entityMap);
      }
      plRowSet.addRow(data);
    }
    return plRowSet;
  }

}
