package org.datayoo.oyez.op.processing.flat;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralPlRowSet;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.util.ColumnSetMetadataLibrary;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "Table2COO",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "oyez",
    inputPorts = { @Port(name = "dataIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_FLAT_TABLE,
        option = true)
    },
    parameters = "<parameters></parameters>",
    compoxes = {})
public class Table2COO extends BaseProcessOperator {

  public Table2COO(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(flowNodeMetadata, parent, engineContext);
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

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return ColumnSetMetadataLibrary.createFlatTableMetadata();
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = this.inputPort.read();
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    this.preBindColumnSetMetadata(columnSetMetadata);
    ColumnSetMetadata outputColumnSetMetadata = this.createOutputColumnSetMetadata(
        columnSetMetadata);
    PlRowSet outSet = new GeneralPlRowSet(SengeeConstants.FDT_FLAT_TABLE,
        outputColumnSetMetadata);
    //添加header进数据
    for (int i = 0; i < columnSetMetadata.getColumnsCount(); i++) {
      Object[] header = new Object[outputColumnSetMetadata.getColumns().size()];
      header[0] = rowSet.getName();
      header[1] = 0;
      header[2] = i;
      header[3] = columnSetMetadata.getColumn(i).getName();
      outSet.addRow(header);
    }
    for (int i = 0; i < rowSet.getRowsCount(); i++) {
      Object[] row = rowSet.getRow(i);
      for (int j = 0; j < row.length; j++) {
        Object[] data = new Object[outputColumnSetMetadata.getColumns().size()];
        data[0] = rowSet.getName();
        data[1] = i + 1;
        data[2] = j;
        data[3] = row[j];
        outSet.addRow(data);
      }
    }
    this.outputPort.write(outSet, this.inputPort.getWaterMark());
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    return new Object[0];
  }

}
