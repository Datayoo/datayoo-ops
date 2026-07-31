package org.datayoo.oyez.op.processing.r;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.Parameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.Operand;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.moxpr.MoxprException;
import org.datayoo.sengee.moxpr.main.Moxpr;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.r.AddRowsDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.LinkedList;
import java.util.List;

@OpDefiner(name = "AddRows",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class AddRows extends BaseProcessOperator {

  protected List<String[]> rowExprs = new LinkedList<>();

  public AddRows(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    throw new UnsupportedOperationException("");
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    throw new UnsupportedOperationException("");
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = read();
    List<Operand[]> rows = buildRows(rowSet);
    for (Operand[] row : rows) {
      int i = 0;
      Object[] outRow = new Object[row.length];
      for (Operand operand : row) {
        outRow[i++] = operand.operate((Object[]) null);
      }
      rowSet.addRow(outRow);
    }
  }

  protected List<Operand[]> buildRows(PlRowSet plRowSet) {
    List<Operand[]> rows = new LinkedList<>();
    for (String[] exprs : rowExprs) {
      int i = 0;
      Operand[] row = new Operand[exprs.length];
      for (String expr : exprs) {
        try {
          row[i++] = Moxpr.createAndPrepare(expr, plRowSet);
        } catch (MoxprException e) {
          throw new OperationRuntimeException(e);
        }
        rows.add(row);
      }
    }
    return rows;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter tableParameter = (TableParameter) groupParameter.getParameter(
        AddRowsDescriptor.PARAM_ROWS);
    for (RowParameter rowParameter : tableParameter.getParameters()) {
      String[] exprs = new String[rowParameter.getParameters().size()];
      int i = 0;
      for (Parameter parameter : rowParameter.getParameters()) {
        exprs[i] = parameter.getValue();
      }
      rowExprs.add(exprs);
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
