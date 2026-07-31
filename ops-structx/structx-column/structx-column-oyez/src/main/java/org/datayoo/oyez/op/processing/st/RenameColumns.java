package org.datayoo.oyez.op.processing.st;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.*;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.st.RenameColumnsDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OpDefiner(name = "RenameColumns",
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
public class RenameColumns extends BaseProcessOperator {

  private Map<String,String> columns;

  public RenameColumns(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    GeneralColumnSetMetadata outPutcolumnSetMetadata = new GeneralColumnSetMetadata(
        this.outputPort.getFlowDataType());
    for (ColumnMetadata columnMetadata : columnSetMetadata.getColumns()) {
      if (columns.containsKey(columnMetadata.getName())) {
        GeneralColumnMetadata generalColumnMetadata = new GeneralColumnMetadata(
            columnMetadata);
        generalColumnMetadata.setName(columns.get(columnMetadata.getName()));
        outPutcolumnSetMetadata.addColumn(generalColumnMetadata);
      } else {
        outPutcolumnSetMetadata.addColumn(columnMetadata);
      }
    }
    return outPutcolumnSetMetadata;
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = this.inputPort.read();
    PlRowSet outSet = createRowSet(
        createOutputColumnSetMetadata(rowSet.getColumnSetMetadata()));
    outSet.addRows(rowSet.getRows());
    this.outputPort.write(outSet, inputPort.getWaterMark());
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    throw new UnsupportedOperationException("");
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        RenameColumnsDescriptor.PARAM_COLUMN_SET);
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    columns = new HashMap<>();
    for (RowParameter rowParameter : rowParameters) {
      columns.put(rowParameter.getParameterValue(
              RenameColumnsDescriptor.PARAM_COLUMN_NAME),
          rowParameter.getParameterValue(
              RenameColumnsDescriptor.PARAM_NEW_COLUMN_NAME));
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
