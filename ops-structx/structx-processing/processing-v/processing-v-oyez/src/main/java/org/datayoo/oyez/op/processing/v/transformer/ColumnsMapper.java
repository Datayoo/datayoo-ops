package org.datayoo.oyez.op.processing.v.transformer;

import org.datayoo.base.types.DataType;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
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
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tangtadin
 * @version 1.0
 * @description: TODO
 * @date 2021/7/13 11:19 PM
 */
@OpDefiner(name = "ColumnsMapper",
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
public class ColumnsMapper extends BaseProcessOperator {

  protected org.datayoo.sengee.datax.mapper.ColumnsMapper columnsMapper;

  protected ColumnSetMetadata columnSetMetadata;

  public ColumnsMapper(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
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

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = this.inputPort.read();
    if(columnSetMetadata == null){
      Map<String, DataType> dataTypeMap = buildColTypes(
          rowSet.getColumnSetMetadata());
      columnsMapper = new org.datayoo.sengee.datax.mapper.ColumnsMapper(
          parameters, dataTypeMap);
      columnSetMetadata = columnsMapper.toColumnSetMetadata(
          this.outputPort.getFlowDataType());
    }
    // 构造数据集
    PlRowSet plRowSet = buildRowSet(columnSetMetadata, rowSet);
    // 输出
    outputPort.write(plRowSet,this.inputPort.getWaterMark());
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
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

  protected PlRowSet buildRowSet(ColumnSetMetadata columnSetMetadata,
      PlRowSet inputRowSet) {
    PlRowSet plRowSet = createRowSet(columnSetMetadata);
    for (int i = 0; i < inputRowSet.getRowsCount(); i++) {
      // 每行新数据
      Map<String, Object> map = inputRowSet.getRowAsMap(i);
      EntityMap entityMap = new EntityMapImpl(map);
      Object[] data = columnsMapper.operate(entityMap);
      plRowSet.addRow(data);
    }
    return plRowSet;
  }

  protected Map<String, DataType> buildColTypes(
      ColumnSetMetadata columnSetMetadata) {
    Map<String, DataType> colTypes = new HashMap<>();
    for (ColumnMetadata columnMetadata : columnSetMetadata.getColumns()) {
      colTypes.put(columnMetadata.getName(), columnMetadata.getType());
    }
    return colTypes;
  }
}
