package org.datayoo.oyez.op.processing.v.transformer;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.EntityMap;
import org.datayoo.moql.EntityMapImpl;
import org.datayoo.moql.MoqlException;
import org.datayoo.moql.Operand;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.util.ExpressionFactory;
import org.datayoo.sengee.exception.OperationInterruptionException;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.v.transformer.ValuesSetDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.Map;

/**
 * @author tangtadin
 * @version 1.0
 * @description: TODO
 * @date 2021/7/13 11:19 PM
 */
@OpDefiner(name = "ValuesSet",
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
public class ValuesSet extends BaseProcessOperator {

  Object[][] columns;

  String[] columnNames;
  int[] columnIndexes;

  public ValuesSet(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return columnSetMetadata;
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    throw new UnsupportedOperationException();
  }

  //考虑到部分func不支持数组
  protected void innerOperate() {
    PlRowSet rowSet = this.inputPort.read();
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    PlRowSet outSet = this.createRowSet(
        this.createOutputColumnSetMetadata(columnSetMetadata));
    bindColumnIndex(columnSetMetadata);
    for (int i = 0; i < rowSet.getRows().size(); i++) {
      Object[] data = rowSet.getRow(i);
      Map<String, Object> map = rowSet.getRowAsMap(i);
      EntityMap entityMap = new EntityMapImpl(map);
      for (int j = 0; j < columns.length; j++) {
        try{
          Operand operand = (Operand) columns[j][1];
          data[columnIndexes[j]] = operand.operate(entityMap);
        }catch (Exception e){
          if (!this.reportErrorAndRun(data, e)) {
            throw new OperationInterruptionException(e);
          }
        }
      }
      outSet.getRows().add(data);
    }
    this.outputPort.write(outSet, this.inputPort.getWaterMark());
  }

  protected void bindColumnIndex(ColumnSetMetadata columnSetMetadata) {
    columnIndexes = new int[columns.length];
    for (int i = 0; i < columnNames.length; i++) {
      columnIndexes[i] = columnSetMetadata.getColumnIndex(columnNames[i]);
    }
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        ValuesSetDescriptor.PARAM_COLUMN_SET);
    columns = new Object[columnSetParameter.getParameters().size()][2];
    columnNames = new String[columnSetParameter.getParameters().size()];
    int i = 0;
    String expression = "";
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      columns[i][0] = rowParameter.getParameterValue(
          ValuesSetDescriptor.PARAM_COLUMN_NAME);
      columnNames[i] = rowParameter.getParameterValue(
          ValuesSetDescriptor.PARAM_COLUMN_NAME);
      try {
        expression = rowParameter.getParameterValue(
            ValuesSetDescriptor.PARAM_COL_EXPR);
        columns[i][1] = ExpressionFactory.createExpression(
            rowParameter.getParameterValue(ValuesSetDescriptor.PARAM_COL_EXPR));
      } catch (MoqlException e) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_INVALID, this.alias,
            expression));
      }
      i++;
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
