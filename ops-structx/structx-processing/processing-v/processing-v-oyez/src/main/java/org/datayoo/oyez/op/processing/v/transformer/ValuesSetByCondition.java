package org.datayoo.oyez.op.processing.v.transformer;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.*;
import org.datayoo.moql.engine.MoqlEngine;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.util.ExpressionFactory;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.v.transformer.ValuesSetByConditionDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.Map;

/**
 * @author tangtadin
 * @version 1.0
 * @description: TODO
 * @date 2021/7/13 11:19 PM
 */
@OpDefiner(name = "ValuesSetByCondition",
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
public class ValuesSetByCondition extends BaseProcessOperator {

  Object[][] exps;

  String[] columnNames;

  String columnName;
  int columnIndex;

  public ValuesSetByCondition(FlowNodeMetadata operatorMetadata,
      FlowNode parent, EngineContext engineContext) {
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

  protected void innerOperate() {
    PlRowSet rowSet = this.inputPort.read();
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    PlRowSet outSet = this.createRowSet(
        this.createOutputColumnSetMetadata(columnSetMetadata));
    bindColumnIndex(columnSetMetadata);
    bindColumns(columnSetMetadata);
    for (int i = 0; i < exps.length; i++) {
      Filter filter = (Filter) exps[i][0];
      filter.bind(columnNames);
    }
    for (int j = 0; j < rowSet.getRows().size(); j++) {
      Object[] data = rowSet.getRow(j);
      Map<String, Object> map = rowSet.getRowAsMap(j);
      EntityMap entityMap = new EntityMapImpl(map);
      for (int i = 0; i < exps.length; i++) {
        Filter filter = (Filter) exps[i][0];
        Operand operand = (Operand) exps[i][1];
        if(filter.isMatch(entityMap)){
          data[columnIndex] = operand.operate(entityMap);
          break;
        }
      }
      outSet.getRows().add(data);
    }

    this.outputPort.write(outSet, this.inputPort.getWaterMark());
  }

  protected void bindColumnIndex(ColumnSetMetadata columnSetMetadata) {
    columnIndex = columnSetMetadata.getColumnIndex(columnName);
  }

  protected void bindColumns(ColumnSetMetadata columnSetMetadata) {
    int i = 0;
    columnNames = new String[columnSetMetadata.getColumns().size()];
    for (ColumnMetadata columnMetadata : columnSetMetadata.getColumns()) {
      columnNames[i++] = columnMetadata.getName();
    }
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        ValuesSetByConditionDescriptor.PARAM_COLUMN_SET);
    exps = new Object[columnSetParameter.getParameters().size()][2];
    columnName = groupParameter.getParameterValue(
        ValuesSetByConditionDescriptor.PARAM_COLUMN_NAME);
    int i = 0;
    String condition = "";
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      condition = rowParameter.getParameterValue(
          ValuesSetByConditionDescriptor.PARAM_CON_EXPR);
      try {
        exps[i][0] = MoqlEngine.createFilter(condition);
      } catch (MoqlException e) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_INVALID, this.alias,
            condition));
      }
      try {
        condition = rowParameter.getParameterValue(
            ValuesSetByConditionDescriptor.PARAM_COL_EXPR);
        exps[i][1] = ExpressionFactory.createExpression(
            rowParameter.getParameterValue(
                ValuesSetByConditionDescriptor.PARAM_COL_EXPR));
      } catch (MoqlException e) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_INVALID, this.alias,
            condition));
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
