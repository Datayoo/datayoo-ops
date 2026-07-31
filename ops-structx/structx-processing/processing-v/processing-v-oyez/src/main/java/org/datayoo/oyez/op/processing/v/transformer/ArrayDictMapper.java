package org.datayoo.oyez.op.processing.v.transformer;

import org.datayoo.base.types.ArrayType;
import org.datayoo.base.types.DataType;
import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.*;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.port.OyezInputPort;
import org.datayoo.oyez.port.OyezOutputPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationInterruptionException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.v.transformer.ArrayDictMapperDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author tangtadin
 * @version 1.0
 * @description: TODO
 * @date 2021/7/13 11:19 PM
 */
@OpDefiner(name = "ArrayDictMapper",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false), @Port(name = SengeeOperatorConstants.PORT_DICT_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class ArrayDictMapper extends BaseProcessOperator {

  protected OyezInputPort dictIn;

  protected Map<Object, Object> dictMap;

  protected String keyColumn;
  protected String colName;
  protected String valueColumn;
  protected DataType valueType;

  public ArrayDictMapper(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void innerOperate() {
    if (dictMap == null) {
      assembleDictMap();
    }
    PlRowSet rowSet = this.inputPort.read();
    int i = 0;
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    PlRowSet outSet = this.createRowSet(
        this.createOutputColumnSetMetadata(columnSetMetadata));

    for (Object[] row : rowSet.getRows()) {
      if (this.getEngineContext().isTermination()) {
        return;
      }
      try {
        Object[] out = this.innerOperate(columnSetMetadata, i++, row);
        if (out != null) {
          outSet.addRow(out);
        }
      } catch (Throwable var8) {
        if (!this.reportErrorAndRun(row, var8)) {
          throw new OperationInterruptionException(var8);
        }
      }
    }
    this.outputPort.write(outSet, this.inputPort.getWaterMark());
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    int colIndex = columnSetMetadata.getColumnIndex(colName);
    Object[] result = new Object[objects.length + 1];
    System.arraycopy(objects, 0, result, 0, objects.length);
    Object[] data = (Object[]) objects[colIndex];
    Object[] temp = new Object[data.length];
    for (int j = 0; j < data.length; j++) {
      if(dictMap.containsKey(data[j])){
        temp[j] = dictMap.get(data[j]);
      }else {
        temp[j] = data[j];
      }
    }
    result[objects.length] = temp;
    return result;
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    GeneralColumnSetMetadata outColumnSetMetadata = new GeneralColumnSetMetadata(
        columnSetMetadata);
    ColumnMetadata columnMetadata = new GeneralColumnMetadata(
        String.format("%s%s", colName, ArrayDictMapperDescriptor.PARAM_SUFFIX));
    columnMetadata.setType(new ArrayType(DataTypeUtils.OBJECT_TYPE));
    outColumnSetMetadata.addColumn(columnMetadata);
    return outColumnSetMetadata;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    this.colName = groupParameter.getParameterValue(
        ArrayDictMapperDescriptor.PARAM_COL_NAME);
    this.keyColumn = groupParameter.getParameterValue(
        ArrayDictMapperDescriptor.PARAM_KEY_COL);
    this.valueColumn = groupParameter.getParameterValue(
        ArrayDictMapperDescriptor.PARAM_VALUE_COL);
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
  protected void checkPorts() {
    if (this.inputPorts.size() != 2) {
      throw new IllegalArgumentException(
          String.format("Operator '%s' should has only 2 input port!",
              this.alias));
    } else {
      Iterator iterator = this.inputPorts.iterator();
      while (iterator.hasNext()) {
        OyezInputPort oyezInputPort = (OyezInputPort) iterator.next();
        if (oyezInputPort.getName()
            .equals(SengeeOperatorConstants.PORT_DICT_IN)) {
          dictIn = oyezInputPort;
        } else {
          inputPort = oyezInputPort;
        }
      }
      this.outputPort = (OyezOutputPort) this.outputPorts.iterator().next();
    }
  }

  protected void assembleDictMap() {
    dictMap = new HashMap<>();
    PlRowSet plRowSet = this.dictIn.read();
    List<Object[]> objects = plRowSet.getRows();
    ColumnSetMetadata columnSetMetadata = plRowSet.getColumnSetMetadata();
    int keyIndex = columnSetMetadata.getColumnIndex(keyColumn);
    int valueIndex = columnSetMetadata.getColumnIndex(valueColumn);
    valueType = columnSetMetadata.getColumn(valueColumn).getType();
    if (keyIndex != -1) {
      for (Object[] row : objects) {
        dictMap.put(row[keyIndex], row[valueIndex]);
      }
    }
  }

  @Override
  protected void innerDestroy() {
    if (dictMap != null) {
      dictMap.clear();
    }
    dictMap = null;
  }
}
