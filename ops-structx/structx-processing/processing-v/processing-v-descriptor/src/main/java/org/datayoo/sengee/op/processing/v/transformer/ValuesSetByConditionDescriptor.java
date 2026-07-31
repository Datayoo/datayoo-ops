package org.datayoo.sengee.op.processing.v.transformer;

import org.datayoo.base.types.DataType;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.moql.MoqlException;
import org.datayoo.moql.Operand;
import org.datayoo.moql.engine.MoqlEngine;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.util.ExpressionFactory;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.datayoo.sengee.util.OpParameterHelper;

import java.util.Map;

@OpDefiner(name = "ValuesSetByCondition",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "column,transformer")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"待赋值列\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "<parametertable name=\"columnSet\" c_Alias=\"赋值规则\"><head>"
            + "<parameter name=\"conExpr\" c_Alias=\"条件表达式\" c_Option=\"false\"></parameter>"
            + "<parameter name=\"colExpr\" c_Alias=\"赋值表达式\" c_Option=\"false\"></parameter>"
            + "</head>" + "</parametertable>" + "</parametergroup>"
            + "</parameters>",
    compoxes = {})
public class ValuesSetByConditionDescriptor extends AbstractProcessingDescriptor {
  public static final String PARAM_CON_EXPR = "conExpr";
  public static final String PARAM_COL_EXPR = "colExpr";

  protected String[][] exprArr;

  protected String columnName;



  public ValuesSetByConditionDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected void readParameters() {
    columnName = parameters.getParameterValue(PARAM_COLUMN_NAME);
    TableParameter columnSetParameter = getColumnSetParameter();
    exprArr = new String[columnSetParameter.getParameters().size()][2];
    int i = 0;
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      exprArr[i][0] = rowParameter.getParameterValue(PARAM_CON_EXPR);
      exprArr[i][1] = rowParameter.getParameterValue(PARAM_COL_EXPR);
      i++;
    }
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return getInputColumnSetMetadata();
  }

  @Override
  public void validateParameters() {
    ColumnSetMetadata columnSetMetadata = getInputColumnSetMetadata();
    Map<String, DataType> columnTypeMap = OpParameterHelper.toColumnTypeMap(
        columnSetMetadata);
    for (String[] param : exprArr) {
      try {
        MoqlEngine.createFilter(param[0]);
      } catch (MoqlException e) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_NOT_VALID_EXPR, this.alias,
            PARAM_CON_EXPR, param[0]), e);
      }
      try {
        Operand operand = ExpressionFactory.createExpression(
            param[1]);
      } catch (MoqlException e) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_NOT_VALID_EXPR, this.alias,
            PARAM_COL_EXPR, param[1]), e);
      }
    }
  }
}
